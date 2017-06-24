package com.lastmile.notification.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.*;
import com.lastmile.KafkaEventsService;
import com.lastmile.MatchService;
import com.lastmiles.TransferDecline;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferOfferAccept;
import com.lastmiles.TransferRequest;
import cz.atlascon.travny.records.CustomRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by ondrej on 24.6.17.
 */
@Service
public class NotificationService {

    AmazonSNSClient service;
    KafkaEventsService kafkaEventsService;
    MatchService matchService;

    @Autowired
    public NotificationService(KafkaEventsService kafkaEventsService, MatchService matchService) {
        service = new AmazonSNSClient(new ProfileCredentialsProvider());
        service.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.kafkaEventsService = kafkaEventsService;
        this.matchService = matchService;
    }

    @PostConstruct
    public <E extends CustomRecord> void setup() throws Exception {
        kafkaEventsService.listenForNewOnly(TransferRequest.class, this::sendCreate);
        kafkaEventsService.listenForNewOnly(TransferOfferAccept.class, this::sendAccept);
        kafkaEventsService.listenForNewOnly(TransferDecline.class, this::sendDecline);
    }

    private void sendCreate(TransferRequest transferRequest) {
        String msg = "You have created request for with id ";
        // Create a topic
        CreateTopicRequest createReq = new CreateTopicRequest().withName("CreateRequest");
        CreateTopicResult createRes = service.createTopic(createReq);
        String phoneNumber = transferRequest.getPhoneNumber();
        SubscribeRequest subReques = new SubscribeRequest(createRes.getTopicArn(), "sms", phoneNumber);
        service.subscribe(subReques);

        PublishRequest publishReq = new PublishRequest()
                .withTopicArn(createRes.getTopicArn())
                .withMessage(msg + transferRequest.getRequestId());
        service.publish(publishReq);
        DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(createRes.getTopicArn());
        service.deleteTopic(deleteTopicRequest);
    }

    private void sendAccept(TransferOfferAccept transferOfferAccept) {
        TransferOffer transferOffer = matchService.getOffer(transferOfferAccept.getOfferId());
        String msg = "Your offer to ride with id ";
        String msg2 = " has been accepted!";
        // Create a topic
        CreateTopicRequest createReq = new CreateTopicRequest().withName("OfferAccepted");
        CreateTopicResult createRes = service.createTopic(createReq);
        String phoneNumber = transferOffer.getPhoneNumber();
        SubscribeRequest subReques = new SubscribeRequest(createRes.getTopicArn(), "sms", phoneNumber);
        service.subscribe(subReques);

        PublishRequest publishReq = new PublishRequest()
                .withTopicArn(createRes.getTopicArn())
                .withMessage(msg + transferOffer.getOfferId() + msg2);
        service.publish(publishReq);
        DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(createRes.getTopicArn());
        service.deleteTopic(deleteTopicRequest);
    }

    private void sendDecline(TransferDecline transferDecline) {
        TransferOffer transferOffer = matchService.getOffer(transferDecline.getOfferId());
        String msg = "Your offer to ride with id ";
        String msg2 = " has been declined!";
        // Create a topic
        CreateTopicRequest createReq = new CreateTopicRequest().withName("OfferDeclined");
        CreateTopicResult createRes = service.createTopic(createReq);
        String phoneNumber = transferOffer.getPhoneNumber();
        SubscribeRequest subReques = new SubscribeRequest(createRes.getTopicArn(), "sms", phoneNumber);
        service.subscribe(subReques);

        PublishRequest publishReq = new PublishRequest()
                .withTopicArn(createRes.getTopicArn())
                .withMessage(msg + transferOffer.getOfferId() + msg2);
        service.publish(publishReq);
        DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(createRes.getTopicArn());
        service.deleteTopic(deleteTopicRequest);
    }

}

package com.lastmile.transport.service;

import com.lastmile.KafkaEventsService;
import com.lastmile.MatchService;
import com.lastmile.transport.utils.UuidGen;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferOfferCancel;
import com.lastmiles.TransferOfferState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by ondrej on 24.6.17.
 */
@Service
public class TransportService {

    final private KafkaEventsService kafkaEventsService;
    private MatchService matchService;

    @Autowired
    public TransportService(KafkaEventsService kafkaEventsService,
                            MatchService matchService) throws IOException {
        this.kafkaEventsService = kafkaEventsService;
        this.matchService = matchService;
    }

    public TransferOffer postOffer(TransferOffer transferOffer) throws IOException {
        transferOffer.setOfferId(UuidGen.generateUUID());
        kafkaEventsService.produce(transferOffer);
        return transferOffer;
    }

    public TransferOffer cancelOffer(String offerId) throws IOException {
        TransferOffer offer = matchService.getOffer(offerId);
        if (offer != null) {
            kafkaEventsService.produce(new TransferOfferCancel()
                    .setOfferId(offerId)
                    .setRequestId(offer.getRequestId()));
        }
        offer.setState(TransferOfferState.CANCELLED);
        return offer;
    }
}

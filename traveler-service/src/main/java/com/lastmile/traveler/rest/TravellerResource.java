package com.lastmile.traveler.rest;

import com.google.common.util.concurrent.Futures;
import com.lastmile.KafkaEventsService;
import com.lastmile.MatchService;
import com.lastmiles.*;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by trehak on 24.6.17.
 */
@Path("/traveller")
public class TravellerResource {

    private KafkaEventsService kafkaEventsService;
    private final MatchService matchService;

    @Autowired
    public TravellerResource(KafkaEventsService kafkaEventsService,
                             MatchService matchService) {
        this.kafkaEventsService = kafkaEventsService;
        this.matchService = matchService;
    }

    @Path("/request")
    @PUT
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest newRequest(TransferRequest request) throws IOException {
        request.setState(TransferRequestState.NEW);
        request.setRequestId(UUID.randomUUID().toString());
        kafkaEventsService.produce(request);
        return request;
    }

    @Path("/{requestId}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest getRequest(@PathParam("requestId") String requestId) throws IOException {
        return matchService.getRequest(requestId);
    }

    @Path("/{requestId}")
    @DELETE
    public void deleteRequest(@PathParam("requestId") String requestId) throws IOException {
        Future<RecordMetadata> future = kafkaEventsService.produce(new TransferRequestCancel().setRequestId(requestId));
        RecordMetadata unchecked = Futures.getUnchecked(future);
        System.out.println(unchecked.offset());
    }

    @Path("/offers/{requestId}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public List<TransferOffer> getOffers(@PathParam("requestId") String requestId) throws IOException {
        List<TransferOffer> offers = matchService.getOffers(requestId);
        return offers;
    }

    @Path("/accept/{offerId}")
    @POST
    public void accept(@PathParam("offerId") String offerId) throws IOException, InterruptedException {
        TransferOffer offer = matchService.getOffer(offerId);
        if (offer == null) {
            return;
        }
        kafkaEventsService.produce(new TransferOfferAccept().setRequestId(offer.getRequestId()).setOfferId(offerId));
    }

    @Path("/decline/{offerId}")
    @POST
    public void decline(@PathParam("offerId") String offerId) throws IOException, InterruptedException {
        TransferOffer offer = matchService.getOffer(offerId);
        if (offer == null) {
            return;
        }
        kafkaEventsService.produce(new TransferDecline().setRequestId(offer.getRequestId()).setOfferId(offerId));
    }


}

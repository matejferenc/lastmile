package com.lastmile.transport.resource;

import com.lastmile.MatchService;
import com.lastmile.transport.service.TransportService;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferOfferState;
import com.lastmiles.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by ondrej on 24.6.17.
 */
@Singleton
@Path("/transport")
public class TransportResource {

    private final TransportService transportService;
    private final MatchService matchService;

    @Autowired
    public TransportResource(TransportService transportService,
                             MatchService matchService) {
        this.transportService = transportService;
        this.matchService = matchService;
    }

    @Path("/request/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest getRequest(@PathParam("id") String id) {
        return matchService.getRequest(id);
    }

    @Path("/offer")
    @PUT
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferOffer postOffer(TransferOffer transferOffer) {
        transferOffer.setState(TransferOfferState.NEW);
        return transportService.postOffer(transferOffer);
    }

    @Path("/offer/{offerId}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferOffer getOffer(@PathParam("offerId") String offerId) {
        return matchService.getOffer(offerId);
    }


}

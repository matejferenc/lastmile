package com.lastmile.transport.resource;

import com.lastmile.transport.service.TransportService;
import com.lastmiles.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by ondrej on 24.6.17.
 */
@Singleton
@Path("/transport")
public class TransportResource {

    private final TransportService transportService;

    @Autowired
    public TransportResource(TransportService transportService) {
        this.transportService = transportService;
    }

    @Path("/requests")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public List<TransferRequest> getRequests() {
        return transportService.getAllRequests();
    }

    @Path("/requests/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest getRequest(@PathParam("id") String id) {
        return transportService.getRequest(id);
    }
}

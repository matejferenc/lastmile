/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lastmiles.traveler;

import com.lastmile.traveler.service.ServiceHandler;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferRequest;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author David
 */

@Path("/requests")


public class ApiController {
    
    @Autowired
    public ServiceHandler travelerService;
    
    @Path("/request")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest getAddressRequest(@RequestBody TransferRequest tr){ 
        return travelerService.processRequest(tr);        
	}    
    
    @Path("/request/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferRequest getAddressRequest(@PathParam("id") String id){ 
        return travelerService.getRequestById(id);          
	}  
    
    @Path("/offer")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public List<TransferOffer> getOffer(){         
        return travelerService.getListOffer();    
	}    
    
    @Path("/offer/{id}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public TransferOffer getOfferById(@PathParam("id") String id){ 
        return travelerService.getOfferById(id);
	}  

    @Path("/offer/new")
    @POST
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public void postOffer(@RequestBody TransferOffer to){ 
        
	}  
}


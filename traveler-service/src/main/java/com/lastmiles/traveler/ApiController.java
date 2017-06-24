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
import javax.ws.rs.PathParam;

import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author David
 */
@RestController
public class ApiController {
    
    @Autowired
    public ServiceHandler travelerService;
    
    @Produces("application/json")    
    @RequestMapping(value= "/request", method = RequestMethod.GET)
    public TransferRequest getAddressRequest(@RequestBody TransferRequest tr){ 
        return travelerService.processRequest(tr);        
	}    
    
    @Produces("application/json")
    @RequestMapping(value= "/request/{id}", method = RequestMethod.GET)
    public TransferRequest getAddressRequest(@PathParam("id") String id){ 
        return travelerService.getRequestById(id);          
	}  
    
    @Produces("application/json")
    @RequestMapping(value= "/offer", method = RequestMethod.GET)
    public List<TransferOffer> getOffer(){         
        return travelerService.getListOffer();    
	}    
    
    @Produces("application/json")
    @RequestMapping(value= "/offer/{id}", method = RequestMethod.GET)
    public TransferOffer getOfferById(@PathParam("id") String id){ 
        return travelerService.getOfferById(id);
	}  

    @Produces("application/json")
    @RequestMapping(value= "/offer/new", method = RequestMethod.POST)
    public void getOfferById(@RequestBody TransferOffer to){ 
        
	}  
}


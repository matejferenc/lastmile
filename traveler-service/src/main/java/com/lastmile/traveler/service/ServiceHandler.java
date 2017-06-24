/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lastmile.traveler.service;

import com.lastmile.KafkaEventsService;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferRequest;
import com.lastmiles.traveler.utils.UuidGen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author David
 */
@Service
public class ServiceHandler {
    
            
    @Autowired
    public KafkaEventsService kafkaEventsService;
    
    ConcurrentMap<String,TransferRequest> cmr = new ConcurrentHashMap();   
    ConcurrentMap<String,TransferOffer> cmo = new ConcurrentHashMap();
    
    TransferRequest transferRequest;
    
        
    private TransferOffer to;
    private TransferRequest tr;
    
    public void checkForOffers() {       
        kafkaEventsService.listen(TransferRequest.class, transferRequest ->
                System.out.println(transferRequest.getRequestId()));        
    }
    
    public TransferOffer getOfferById(String id) {       
        to = new TransferOffer();
        return to;
    }
    
    public TransferRequest getRequestById(String id) {       
        tr = new TransferRequest();
        return tr;
    }
    
    public List<TransferOffer> getListOffer() {
        List<TransferOffer> list= new ArrayList();
        return list;
    }
    
    public TransferRequest processRequest(TransferRequest tr) {
        this.tr=tr;
        tr.setRequestId(UuidGen.generateUUID());
        addToMapRequest(tr.getRequestId(),tr);
        
        try {
            kafkaEventsService.produce(tr);
            return tr;
        } catch (IOException ex) {
            Logger.getLogger(ServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }    
    public void addOffer(TransferOffer to) {
        addToMapOffer(to.getOfferId(), to);        
    }
    
    private TransferRequest assignRequest(TransferOffer to) {
            this.cmo = cmo;
            
            for (Map.Entry<String, TransferRequest> entry : cmr.entrySet())
            {
                if (entry.getKey().equals(to.getOfferId())) {
                return entry.getValue();
                }
            }
        return null;
    }
    
    private void addToMapRequest(String s,TransferRequest tr) {       
            this.cmr = cmr;
            if(!cmr.containsKey(s))
                cmr.put(s,tr);
             }
    
    private void addToMapOffer(String s,TransferOffer to) {       
            this.cmo = cmo;
            if(!cmo.containsKey(s))
                cmo.put(s,to);
             }
}

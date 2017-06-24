package com.lastmile.transport.service;

import com.lastmile.KafkaEventsService;
import com.lastmile.transport.utils.UuidGen;
import com.lastmiles.TransferOffer;
import com.lastmiles.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ondrej on 24.6.17.
 */
@Service
public class TransportService {

    private static ConcurrentMap<String, TransferRequest> requestsStorageMap = new ConcurrentHashMap<>();

    final private KafkaEventsService kafkaEventsService;

    @Autowired
    public TransportService(KafkaEventsService kafkaEventsService) {
        this.kafkaEventsService = kafkaEventsService;
        kafkaEventsService.listen(TransferRequest.class, transferRequest ->
                requestsStorageMap.put(transferRequest.getRequestId(), transferRequest));
    }

    public List<TransferRequest> getAllRequests() {
        return new ArrayList<>(requestsStorageMap.values());
    }

    public TransferRequest getRequest(String id) {
        return requestsStorageMap.get(id);
    }

    public TransferOffer postOffer(TransferOffer transferOffer) {
        transferOffer.setOfferId(UuidGen.generateUUID());
        try {
            kafkaEventsService.produce(transferOffer);
            return transferOffer;
        } catch (IOException e) {
            Logger.getLogger(TransportService.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}

package com.lastmile.transport.service;

import com.lastmile.KafkaEventsService;
import com.lastmiles.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
}

package com.lastmile.transport.service;

import com.lastmile.KafkaEventsService;
import com.lastmile.transport.utils.UuidGen;
import com.lastmiles.TransferOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ondrej on 24.6.17.
 */
@Service
public class TransportService {

    final private KafkaEventsService kafkaEventsService;

    @Autowired
    public TransportService(KafkaEventsService kafkaEventsService) throws IOException {
        this.kafkaEventsService = kafkaEventsService;
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

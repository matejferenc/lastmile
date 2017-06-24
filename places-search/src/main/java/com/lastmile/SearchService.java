package com.lastmile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lastmiles.PlaceSearchRequest;
import com.lastmiles.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by trehak on 24.6.17.
 */
@Service
public class SearchService {

    private final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
    private final Map<String, TransferRequest> requests = Maps.newConcurrentMap();
    private final KafkaEventsService kafkaEventsService;

    @Autowired
    public SearchService(KafkaEventsService kafkaEventsService) throws IOException {
        this.kafkaEventsService = kafkaEventsService;
        kafkaEventsService.listen(TransferRequest.class, new Consumer<TransferRequest>() {
            @Override
            public void accept(TransferRequest transferRequest) {
                LOGGER.info("Received {} ", transferRequest);
                requests.put(transferRequest.getRequestId(), transferRequest);
            }
        });
    }

    public List<TransferRequest> search(PlaceSearchRequest request) {
        List<TransferRequest> matched = Lists.newArrayList();
        for (TransferRequest r : requests.values()) {
            if (matchesSearch(request, r)) {
                matched.add(r);
            }
        }
        return matched;
    }

    private boolean matchesSearch(PlaceSearchRequest request, TransferRequest r) {
        Place from = r.getPlaceFrom();
        return match(request, from);
    }

    private boolean match(PlaceSearchRequest request, Place from) {
        double lat = from.getLat().doubleValue();
        double lon = from.getLon().doubleValue();
        return request.getLatNorth() >= lat && request.getLatSouth() <= lat &&
                request.getLonEast() >= lon && request.getLonWest() <= lon;
    }


}

package com.lastmile;

import com.google.common.collect.Lists;
import com.lastmiles.PlaceSearchRequest;
import com.lastmiles.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by trehak on 24.6.17.
 */
@Service
public class SearchService {

    private final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
    private final MatchService matchService;

    @Autowired
    public SearchService(MatchService matchService, KafkaEventsService kafkaEventsService) throws Exception {
        this.matchService = matchService;
    }

    public List<TransferRequest> search(PlaceSearchRequest request) {
        List<TransferRequest> matched = Lists.newArrayList();
        for (TransferRequest r : matchService.getPendingRequests()) {
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

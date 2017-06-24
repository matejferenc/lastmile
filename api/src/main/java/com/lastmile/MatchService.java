package com.lastmile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lastmiles.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by trehak on 24.6.17.
 */
@Service
public class MatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchService.class);
    private final KafkaEventsService kafkaEventsService;

    // request id -> request
    private final Map<String, TransferRequest> requests = Maps.newConcurrentMap();
    // offer id -> offer
    private final Map<String, TransferOffer> offers = Maps.newConcurrentMap();
    // request id -> offer ids
    private final Map<String, Set<String>> offersForPendingRequests = Maps.newConcurrentMap();


    @Autowired
    public MatchService(KafkaEventsService kafkaEventsService) {
        this.kafkaEventsService = kafkaEventsService;
    }

    @PostConstruct
    public void setup() throws IOException {
        // new request
        kafkaEventsService.listen(TransferRequest.class, transferRequest -> {
            requests.put(transferRequest.getRequestId(), transferRequest);
        });
        // new offer
        kafkaEventsService.listen(TransferOffer.class, transferOffer -> {
            offers.put(transferOffer.getOfferId(), transferOffer);
            offersForPendingRequests.computeIfAbsent(transferOffer.getRequestId(), s ->
                    Sets.newConcurrentHashSet()).add(transferOffer.getOfferId());
        });
        // take offer down
        kafkaEventsService.listen(TransferOfferCancel.class, cancel -> {
            TransferOffer offer = offers.get(cancel.getOfferId());
            if (offer != null) {
                offer.setState(TransferOfferState.CANCELLED);
            }
        });
        // decline offer
        kafkaEventsService.listen(TransferDecline.class, decline -> {
            TransferOffer offer = offers.get(decline.getOfferId());
            if (offer != null) {
                offer.setState(TransferOfferState.DECLINED);
            }
        });
        // accept offer
        kafkaEventsService.listen(TransferOfferAccept.class, accept -> {
            TransferOffer offer = offers.get(accept.getOfferId());
            if (offer != null) {
                offer.setState(TransferOfferState.ACCEPTED);
            }
            TransferRequest request = requests.get(accept.getRequestId());
            if (request != null) {
                request.setState(TransferRequestState.MATCHED);
                request.setMatchedOfferId(accept.getOfferId());
            }
            Set<String> offers = offersForPendingRequests.getOrDefault(accept.getRequestId(), Sets.newHashSet());
            if (offers != null) {
                offers.forEach(oid -> decline(oid, "RequestCanceled"));
            }
        });
        // decline request
        kafkaEventsService.listen(TransferRequestCancel.class, cancel -> {
            TransferRequest request = requests.get(cancel.getRequestId());
            if (request != null) {
                if (request.getMatchedOfferId() != null) {
                    TransferOffer offer = offers.get(request.getMatchedOfferId());
                    if (offer != null) {
                        offer.setState(TransferOfferState.DECLINED);
                    }
                }
                request.setState(TransferRequestState.CANCELLED);
            }
            Set<String> offers = offersForPendingRequests.remove(cancel.getRequestId());
            if (offers != null) {
                offers.forEach(oid -> decline(oid, "RequestCanceled"));
            }
        });
    }

    private void decline(String offerId, String reason) {
        TransferOffer offer = offers.get(offerId);
        offer.setState(TransferOfferState.DECLINED);
    }

    public TransferRequest getRequest(String requestId) {
        return requests.get(requestId);
    }

    public List<TransferOffer> getOffers(String requestId) {
        TransferRequest request = requests.get(requestId);
        if (request == null || request.getState() == TransferRequestState.CANCELLED) {
            return Lists.newArrayList();
        }

        if (request.getState() == TransferRequestState.MATCHED) {
            TransferOffer offer = getOffer(request.getMatchedOfferId());
            return Lists.newArrayList(offer);
        } else {
            return offersForPendingRequests.getOrDefault(requestId, Sets.newHashSet()).stream()
                    .map(offers::get)
                    .filter(Objects::nonNull)
                    .filter(transferOffer -> transferOffer.getState() == TransferOfferState.NEW || transferOffer.getState() == TransferOfferState.ACCEPTED)
                    .collect(Collectors.toList());
        }
    }


    public TransferOffer getOffer(String offerId) {
        return offers.get(offerId);
    }
}

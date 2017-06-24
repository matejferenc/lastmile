package com.lastmile;

import com.google.common.collect.Lists;
import com.lastmiles.PlaceSearchRequest;
import com.lastmiles.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by trehak on 24.6.17.
 */
@Singleton
@Path("/places")
public class SearchResource {

    private final SearchService searchService;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @Path("/search")
    @POST
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public List<TransferRequest> search(PlaceSearchRequest request) {
        return Lists.newArrayList(new TransferRequest().setMyName("abc"));
//        return searchService.search(request);
    }

}

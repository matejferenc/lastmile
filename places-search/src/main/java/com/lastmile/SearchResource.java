package com.lastmile;

import com.google.common.collect.Lists;
import com.lastmiles.PlaceSearchRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by trehak on 24.6.17.
 */
@Path("/places")
public class SearchResource {

    @Path("/search")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public List<Place> search(PlaceSearchRequest request) {
        return Lists.newArrayList(new Place().setLat(12d).setLon(13d).setName("name"));
    }

}

package com.lastmile.traveler.app;

import com.lastmile.traveler.rest.ApiController;
import com.lastmile.traveler.rest.TravellerResource;
import cz.atlascon.travny.jax.TravnyCollectionMessageBodyReaderWriter;
import cz.atlascon.travny.jax.TravnyMessageBodyReaderWriter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;


/**
 * Created by trehak on 11.3.2016.
 */

@Component
@ApplicationPath("/rest")
public class Config extends ResourceConfig {

    @PostConstruct
    public void registerEndpoints() {
        // jersey
        register(MultiPartFeature.class);
        register(TravnyMessageBodyReaderWriter.class);
        register(TravnyCollectionMessageBodyReaderWriter.class);
        register(TravellerResource.class);
        register(ApiController.class);
    }
}

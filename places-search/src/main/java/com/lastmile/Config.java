package com.lastmile;

import cz.atlascon.travny.jax.TravnyMessageBodyReader;
import cz.atlascon.travny.jax.TravnyMessageBodyWriter;
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
        register(TravnyMessageBodyReader.class);
        register(TravnyMessageBodyWriter.class);
        register(SearchResource.class);

    }
}

package com.alibaba.fastjson.support.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

@Priority(1999)
public class FastJsonAutoDiscoverable implements AutoDiscoverable {
    public static volatile boolean autoDiscover = true;

    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(FastJsonFeature.class) && autoDiscover) {
            context.register(FastJsonFeature.class);
        }
    }
}

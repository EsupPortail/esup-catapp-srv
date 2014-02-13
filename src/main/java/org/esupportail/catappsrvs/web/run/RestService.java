package org.esupportail.catappsrvs.web.run;

import org.esupportail.catappsrvs.web.DomaineResource;
import org.esupportail.catappsrvs.web.config.jerseyspring.SpringComponentProvider;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public final class RestService extends ResourceConfig {
    public RestService() {
        register(SpringComponentProvider.class);
        register(DomaineResource.class);
        register(JacksonFeature.class);
        property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, false);
        property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
    }
}

package org.esupportail.catappsrvs.web.run;

import org.esupportail.catappsrvs.web.DomaineResource;
import org.esupportail.catappsrvs.web.config.jerseyspring.SpringComponentProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.core.Application;
import java.util.*;

public final class RestService extends ResourceConfig {//extends Application {

    public RestService() {
        register(SpringComponentProvider.class);
        register(DomaineResource.class);
        property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, false);
    }

//    @Override
//    public Map<String, Object> getProperties() {
//        return Collections.unmodifiableMap(new HashMap<String, Object>() {{
//            put("jersey.config.server.tracing", "ALL");
//        }});
//    }
//
//    @Override
//    public Set<Class<?>> getClasses() {
//        return Collections.unmodifiableSet(new HashSet<Class<?>>() {{
//            add(DomaineResource.class);
//        }});
//    }
}

package org.esupportail.catappsrvs.web;

import lombok.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/domaine")
@Component
public enum DomaineResource {_;

    @Inject @Named("testObj")
    Object testObj;

    @GET
    @Produces("text/plain")
    public String getHello() {
        return "Hello ! " + testObj;
    }
}

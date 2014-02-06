package org.esupportail.catappsrvs.web;

import lombok.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static org.esupportail.catappsrvs.web.DomaineResource.TestBean.*;

@Path("/domaine")
@Component
public enum DomaineResource {
    DomainResource;

    @Inject @Named("testObj")
    Object testObj;

    @GET
    @Produces("application/json")
    public TestBean getHello() {
        return testBean(testObj.toString());
    }

    @Value(staticConstructor = "testBean")
    static final class TestBean {
        String hello = "Hello !";
        String test;
    }
}

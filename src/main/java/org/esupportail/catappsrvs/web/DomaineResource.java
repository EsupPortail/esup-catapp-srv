package org.esupportail.catappsrvs.web;

import com.google.common.collect.ImmutableList;
import lombok.Value;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domaine;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.esupportail.catappsrvs.model.Application.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Titre.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.web.DomaineResource.TestBean.*;

@Path("/domaine")
@Component
public enum DomaineResource {
    DomainResource;

    @Inject @Named("testObj")
    Object testObj;

    @PersistenceContext
    EntityManager entityManager;

    @GET
    @Produces("application/json")
    public TestBean getHello() throws MalformedURLException {

//        entityManager.persist(domaine(
//                0L,
//                code("TEST1"),
//                libelle("Test 1"),
//                null,
//                Collections.<Domaine>emptyList(),
//                ImmutableList.of(Application.application(
//                        0L,
//                        code("AP1"),
//                        titre("Ap 1"),
//                        libelle("Ap 1"),
//                        description("Ap 1"),
//                        new URL("http://toto.fr"),
//                        Collections.<Domaine>emptyList()))));

        return testBean(testObj.toString());
    }

    @Value(staticConstructor = "testBean")
    static final class TestBean {
        String hello = "Hello !";
        String test;
    }
}

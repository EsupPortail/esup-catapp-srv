package org.esupportail.catappsrvs.web;

import com.google.common.collect.ImmutableList;
import fj.Effect;
import fj.data.Option;
import lombok.Value;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domaine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
import java.util.List;

import static java.util.Arrays.asList;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Titre.*;
import static org.esupportail.catappsrvs.web.DomaineResource.TestBean.*;

@Path("/domaine")
@Component
public enum DomaineResource {
    DomainResource;

    @Inject @Named("testObj")
    Object testObj;

    @Inject
    PlatformTransactionManager txMgr;

    @PersistenceContext
    EntityManager entityManager;

    @GET
    @Produces("application/json")
    public TestBean getHello() {

        final Option<Long> domPk = new TransactionTemplate(txMgr).execute(new TransactionCallback<Option<Long>>() {
            public Option<Long> doInTransaction(TransactionStatus status) {
                try {
                    final Application application = Application.application(
                            code("AP1"),
                            titre("Ap 1"),
                            libelle("Ap 1"),
                            description("Ap 1"),
                            new URL("http://toto.fr"),
                            Collections.<Domaine>emptyList());
                    final Domaine domaine = Domaine.domaine(
                            code("DOM1"),
                            libelle("Dom 1"),
                            null,
                            Collections.<Domaine>emptyList(),
                            asList(application));
                    entityManager.persist(application);
                    entityManager.persist(domaine);

                    return Option.some(domaine.pk());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return Option.none();
                }
            }
        });

        new TransactionTemplate(txMgr).execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                domPk.foreach(new Effect<Long>() {
                    public void e(Long pk) {
                        final Domaine domaine = entityManager.find(Domaine.class, pk);
                        final List<Application> apps = domaine.applications();

                        System.out.println(apps.size());
                    }
                });
                return null;
            }
        });

        return testBean(testObj.toString());
    }

    @Value(staticConstructor = "testBean")
    static final class TestBean {
        String hello = "Hello !";
        String test;
    }
}

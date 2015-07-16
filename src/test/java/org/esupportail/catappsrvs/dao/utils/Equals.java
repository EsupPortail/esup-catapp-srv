package org.esupportail.catappsrvs.dao.utils;

import fj.*;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;

import java.net.URL;

import static fj.Bottom.error;
import static fj.P.p;
import static org.esupportail.catappsrvs.model.Application.Activation;
import static org.esupportail.catappsrvs.model.CommonTypes.*;

public final class Equals {
    private Equals() { throw error("Unsupported"); }

    public static final Equal<Application> applicationCompleteEq =
            Equal.p2Equal(
                    Equal.p4Equal(
                            Equal.<Code>anyEqual(),
                            Equal.<Title>anyEqual(),
                            Equal.<Caption>anyEqual(),
                            Equal.<Description>anyEqual()),
                    Equal.p4Equal(
                            Equal.<URL>anyEqual(),
                            Equal.<Activation>anyEqual(),
                            Equal.<LdapGroup>anyEqual(),
                            Equal.listEqual(Equal.<Domain>anyEqual())
                    )).contramap(application -> p(p(application.code(),
                                                    application.title(),
                                                    application.caption(),
                                                    application.description()),
                                                  p(application.url(),
                                                    application.activation(),
                                                    application.group(),
                                                    application.domains())));

    public static final Equal<Domain> domaineCompleteEq =
        Equal.p5Equal(Code.eq,
                      Caption.eq,
                      Equal.optionEqual(Equals.domaineCompleteEq),
                      Equal.listEqual(Domain.eq),
                      Equal.listEqual(Equal.<Application>anyEqual()))
             .contramap(domain -> p(domain.code(),
                                    domain.caption(),
                                    domain.parent(),
                                    domain.domains(),
                                    domain.applications()));

}

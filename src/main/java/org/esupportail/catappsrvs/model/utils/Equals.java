package org.esupportail.catappsrvs.model.utils;

import fj.*;
import fj.data.List;
import fj.data.Option;
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
                    )).comap(new F<Application,
                    P2<P4<Code, Title, Caption, Description>,
                            P4<URL, Activation, LdapGroup, List<Domain>>>>() {
                public P2<P4<Code, Title, Caption, Description>,
                        P4<URL, Activation, LdapGroup, List<Domain>>> f(Application application) {
                    return p(
                            p(application.code(),
                                    application.title(),
                                    application.caption(),
                                    application.description()),
                            p(application.url(),
                                    application.activation(),
                                    application.group(),
                                    application.domains()));
                }
            });

    public static final Equal<Domain> domaineCompleteEq =
            Equal.p5Equal(
                    Equal.<Code>anyEqual(),
                    Equal.<Caption>anyEqual(),
                    Equal.optionEqual(Equals.domaineCompleteEq),
                    Equal.listEqual(Equal.<Domain>anyEqual()),
                    Equal.listEqual(Equal.<Application>anyEqual()))
            .comap(new F<Domain, P5<Code, Caption, Option<Domain>, List<Domain>, List<Application>>>() {
                public P5<Code, Caption, Option<Domain>, List<Domain>, List<Application>> f(Domain domain) {
                    return p(domain.code(),
                            domain.caption(),
                            domain.parent(),
                            domain.domains(),
                            domain.applications());
                }
            });

}

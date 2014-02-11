package org.esupportail.catappsrvs.model.utils;

import fj.*;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.Versionned;

import java.net.URL;

import static fj.Bottom.error;
import static fj.Function.curry;
import static fj.P.p;
import static org.esupportail.catappsrvs.model.Application.Accessibilite;
import static org.esupportail.catappsrvs.model.CommonTypes.*;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public final class Equals {
    private Equals() { throw error("Unsupported"); }

    public static final Equal<Application> applicationCompleteEq =
            Equal.p2Equal(
                    Equal.p4Equal(
                            Equal.<Version>anyEqual(),
                            Equal.<Code>anyEqual(),
                            Equal.<Titre>anyEqual(),
                            Equal.<Libelle>anyEqual()),
                    Equal.p5Equal(
                            Equal.<Description>anyEqual(),
                            Equal.<URL>anyEqual(),
                            Equal.<Accessibilite>anyEqual(),
                            Equal.<LdapGroup>anyEqual(),
                            Equal.listEqual(Equal.<Domaine>anyEqual())
                    )).comap(new F<Application,
                    P2<P4<Version, Code, Titre, Libelle>,
                            P5<Description, URL, Accessibilite, LdapGroup, List<Domaine>>>>() {
                public P2<P4<Version, Code, Titre, Libelle>,
                        P5<Description, URL, Accessibilite, LdapGroup, List<Domaine>>> f(Application application) {
                    return p(
                            p(application.version(),
                                    application.code(),
                                    application.titre(),
                                    application.libelle()),
                            p(application.description(),
                                    application.url(),
                                    application.accessibilite(),
                                    application.groupe(),
                                    application.domaines()));
                }
            });

    public static final Equal<Domaine> domaineCompleteEq =
            Equal.p6Equal(
                    Equal.<Version>anyEqual(),
                    Equal.<Code>anyEqual(),
                    Equal.<Libelle>anyEqual(),
                    Equal.optionEqual(Equals.domaineCompleteEq),
                    Equal.listEqual(Equal.<Domaine>anyEqual()),
                    Equal.listEqual(Equal.<Application>anyEqual()))
            .comap(new F<Domaine, P6<Version, Code, Libelle, Option<Domaine>, List<Domaine>, List<Application>>>() {
                public P6<Version, Code, Libelle, Option<Domaine>, List<Domaine>, List<Application>> f(Domaine domaine) {
                    return p(domaine.version(),
                            domaine.code(),
                            domaine.libelle(),
                            domaine.parent(),
                            domaine.sousDomaines(),
                            domaine.applications());
                }
            });

}

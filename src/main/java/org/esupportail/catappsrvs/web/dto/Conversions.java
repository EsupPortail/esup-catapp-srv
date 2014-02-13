package org.esupportail.catappsrvs.web.dto;

import fj.F;
import fj.F4;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;

import static org.esupportail.catappsrvs.model.Application.Accessibilite.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Titre.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Conversions {

    private static final Application emptyApp =
            Application.application(
                    version(-1),
                    code(""),
                    titre(""),
                    libelle(""),
                    description(""),
                    null,
                    Accessible,
                    ldapGroup(""),
                    List.<Domaine>nil());

    public static final F<String, Application> appWithCode = new F<String, Application>() {
        public Application f(String code) {
            return emptyApp.withCode(code(code));
        }
    };

    public static final F<Application, String> appCode = new F<Application, String>() {
        public String f(Application application) {
            return application.code().value();
        }
    };

    public static final Domaine emptyDom =
            domaine(version(-1),
                    code(""),
                    libelle(""),
                    Option.<Domaine>none(),
                    List.<Domaine>nil(),
                    List.<Application>nil());

    public static final F<String,Domaine> domWithCode =new F<String, Domaine>() {
                        public Domaine f(String code) {
                            return emptyDom.withCode(code(code));
                        }
                    };

    public static final F<Domaine,String> domCode = new F<Domaine, String>() {
        public String f(Domaine domaine) {
            return domaine.code().value();
        }
    };

    public static DomaineDTO domaineToDTO(Domaine domaine) {
        return DomaineDTO.domaineDTO(
                domaine.code().value(),
                domaine.libelle().value(),
                domaine.parent().option("", domCode),
                domaine.sousDomaines().map(domCode).array(String[].class),
                domaine.applications().map(appCode).array(String[].class));
    }

    public static Either<Exception, ApplicationDTO> applicationToDTO(Application application) {
        return null;
    }
}
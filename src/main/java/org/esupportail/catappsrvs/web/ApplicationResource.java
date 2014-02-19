package org.esupportail.catappsrvs.web;

import fj.F;
import fj.F2;
import fj.F8;
import fj.data.List;
import fj.data.NonEmptyList;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.ICrud;
import org.esupportail.catappsrvs.web.dto.JsApp;
import org.esupportail.catappsrvs.web.utils.Functions;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URL;

import static fj.data.Validation.fail;
import static fj.data.Validation.failNEL;
import static fj.data.Validation.success;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.web.dto.JsApp.Acces;
import static org.esupportail.catappsrvs.web.dto.Conversions.*;
import static org.esupportail.catappsrvs.web.dto.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.arrayToList;
import static org.esupportail.catappsrvs.model.Application.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Titre.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;

@Slf4j @Getter(AccessLevel.NONE) // lombok
@Path("applications") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public final class ApplicationResource extends CrudResource<Application, IApplication, JsApp> {
    private ApplicationResource(IApplication srv) {
        super(srv);
    }

    public static ApplicationResource applicationResource(IApplication srv) {
        return new ApplicationResource(srv);
    }

    @Override
    protected Validation<Exception, Application> validAndBuild(JsApp app) {
        return validDomaines(app.domaines()).map(arrayToList).nel()
                .accumulate(sm,
                        validGroupe(app.groupe()).nel(),
                        validDescr(app.description()).nel(),
                        validAccess(app.acces()).nel(),
                        validUrl(app.url()).nel().bind(buildUrl),
                        validLibelle(app.libelle()).nel(),
                        validTitre(app.titre()).nel(),
                        validCode(app.code()).nel(),
                        buildApp)
                .f().map(Functions.fieldsException);
    }

    @Override
    protected Validation<Exception, Response> readResp(Application app, final UriInfo uriInfo) {
        try {
            final ResponseBuilder domsBuilder =
                    app.domaines().foldLeft(
                            new F2<ResponseBuilder, Domaine, ResponseBuilder>() {
                                public ResponseBuilder f(ResponseBuilder rb, Domaine dom) {
                                    final String code = dom.code().value();
                                    return rb.link(
                                            uriInfo.getBaseUriBuilder()
                                                    .path(DomaineResource.class)
                                                    .path(code)
                                                    .build(),
                                            "dom:" + code);
                                }
                            },
                            Response.ok(applicationToDTO(app)));
            return success(domsBuilder.build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    @Override
    protected Validation<Exception, Response> createResp(Application app, UriInfo uriInfo) {
        try {
            final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            final URI location = uriBuilder.path(app.code().value()).build();
            return success(Response.created(location).build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    private final F8<List<String>, String, String, Acces, URL, String, String, String, Application> buildApp =
            new F8<List<String>, String, String, Acces, URL, String, String, String, Application>() {
                public Application f(List<String> doms, String grp, String descr, Acces access, URL url, String lib, String titre, String code) {
                    return Application.application(
                            version(-1),
                            code(code),
                            titre(titre),
                            libelle(lib),
                            description(descr),
                            url,
                            Accessibilite.valueOf(access.name()),
                            ldapGroup(grp),
                            doms.map(domWithCode));
                }
            };

    private final F<String,Validation<NonEmptyList<String>,URL>> buildUrl =
            new F<String, Validation<NonEmptyList<String>, URL>>() {
                public Validation<NonEmptyList<String>, URL> f(String url) {
                    try {
                        return success(new URL(url));
                    } catch (Exception e) {
                        return failNEL(e.getMessage());
                    }
                }
            };
}

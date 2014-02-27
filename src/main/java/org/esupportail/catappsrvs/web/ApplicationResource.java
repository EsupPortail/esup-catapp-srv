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
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.web.json.JsApp;
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
import static org.esupportail.catappsrvs.model.CommonTypes.Caption.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Title.*;
import static org.esupportail.catappsrvs.web.json.Conversions.*;
import static org.esupportail.catappsrvs.web.json.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.arrayToList;
import static org.esupportail.catappsrvs.model.Application.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.*;

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
        return validDomaines(app.domains()).map(arrayToList).nel()
                .accumulate(sm,
                        validGroupe(app.group()).nel(),
                        validDescr(app.description()).nel(),
                        validAccess(app.activation()).nel(),
                        validUrl(app.url()).nel().bind(buildUrl),
                        validLibelle(app.caption()).nel(),
                        validTitre(app.title()).nel(),
                        validCode(app.code()).nel(),
                        buildApp)
                .f().map(Functions.fieldsException);
    }

    @Override
    protected Validation<Exception, Response> readResp(Application app, final UriInfo uriInfo) {
        try {
            final ResponseBuilder domsBuilder =
                    app.domains().foldLeft(
                            new F2<ResponseBuilder, Domain, ResponseBuilder>() {
                                public ResponseBuilder f(ResponseBuilder rb, Domain dom) {
                                    final String code = dom.code().value();
                                    return rb.link(
                                            uriInfo.getBaseUriBuilder()
                                                    .path(DomainResource.class)
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

    private final F8<List<String>, String, String, JsApp.JsActivation, URL, String, String, String, Application> buildApp =
            new F8<List<String>, String, String, JsApp.JsActivation, URL, String, String, String, Application>() {
                public Application f(List<String> doms, String grp, String descr, JsApp.JsActivation activ, URL url, String lib, String titre, String code) {
                    return Application.application(
                            code(code),
                            title(titre),
                            caption(lib),
                            description(descr),
                            url,
                            Activation.valueOf(activ.name()),
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

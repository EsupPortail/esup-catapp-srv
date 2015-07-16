package org.esupportail.catappsrvs.web;

import fj.F;
import fj.F8;
import fj.data.List;
import fj.data.NonEmptyList;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes.*;
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

import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.model.Application.Activation;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;
import static org.esupportail.catappsrvs.web.json.Conversions.applicationToJson;
import static org.esupportail.catappsrvs.web.json.Conversions.domWithCode;
import static org.esupportail.catappsrvs.web.json.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.arrayToList;

@Slf4j @Getter(AccessLevel.NONE) // lombok
@Path("applications") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public final class ApplicationResource extends CrudResource<Application, IApplication, JsApp> {
    private ApplicationResource(IApplication srv) {
        super(srv);
    }

    public static ApplicationResource of(IApplication srv) {
        return new ApplicationResource(srv);
    }

    @Override
    protected Validation<Exception, Application> validAndBuild(JsApp json) {
        return validDomaines(json.domains())
            .map(arrayToList).nel()
            .accumulate(sm,
                        validGroupe(json.group()).nel(),
                        validDescr(json.description()).nel(),
                        validAccess(json.activation()).nel(),
                        validUrl(json.url()).nel().bind(buildUrl),
                        validLibelle(json.caption()).nel(),
                        validTitre(json.title()).nel(),
                        validCode(json.code()).nel(),
                        buildApp)
            .f().map(Functions.fieldsException);
    }

    @Override
    protected Validation<Exception, Response> readResp(Application app, final UriInfo uriInfo) {
        try {
            final ResponseBuilder domsBuilder =
                app.domains().foldLeft(
                    (rb, dom) -> {
                        final String code = dom.code().value();
                        return rb.link(
                            uriInfo.getBaseUriBuilder()
                                   .path(DomainResource.class)
                                   .path(code)
                                   .build(),
                            "dom:" + code);
                    },
                    Response.ok(applicationToJson(app)));
            return Validation.success(domsBuilder.build());
        } catch (Exception e) {
            return Validation.fail(e);
        }
    }

    @Override
    protected Validation<Exception, Response> createResp(Application app, UriInfo uriInfo) {
        try {
            final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            final URI location = uriBuilder.path(app.code().value()).build();
            return Validation.success(Response.created(location).build());
        } catch (Exception e) {
            return Validation.fail(e);
        }
    }

    private final F8<List<String>, String, String, JsApp.JsActivation, URL, String, String, String, Application> buildApp =
        (doms, grp, descr, activ, url, lib, titre, code) ->
            Debug._(this, "buildApp", doms, grp, descr, activ, url, lib, titre, code)
                 .log(() -> Application.of(Code.of(code),
                                           Title.of(titre),
                                           Caption.of(lib),
                                           Description.of(descr),
                                           url,
                                           Activation.valueOf(activ.name()),
                                           LdapGroup.of(grp),
                                           doms.map(domWithCode)));

    private final F<String, Validation<NonEmptyList<String>,URL>> buildUrl =
        url -> Debug._(this, "buildUrl", url).<Validation<NonEmptyList<String>,URL>>log(() -> {
            try {
                return Validation.success(new URL(url));
            } catch (Exception e) {
                return Validation.<String,URL>fail(e.getMessage()).nel();
            }
        });
}

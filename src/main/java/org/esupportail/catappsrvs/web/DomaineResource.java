package org.esupportail.catappsrvs.web;

import fj.F2;
import fj.F6;
import fj.data.List;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.services.ICrud;
import org.esupportail.catappsrvs.services.IDomaine;
import org.esupportail.catappsrvs.web.dto.JsDom;
import org.esupportail.catappsrvs.web.utils.Functions;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static fj.Function.curry;
import static fj.data.Option.fromNull;
import static fj.data.Option.fromString;
import static fj.data.Validation.fail;
import static fj.data.Validation.success;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;
import static org.esupportail.catappsrvs.web.dto.Conversions.*;
import static org.esupportail.catappsrvs.web.dto.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.arrayToList;

@Slf4j @Getter(AccessLevel.NONE) // lombok
@Path("domain") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public final class DomaineResource extends CrudResource<Domaine, JsDom> {
    private DomaineResource(ICrud<Domaine> srv) {
        super(srv);
    }

    public static DomaineResource domaineResource(IDomaine srv) {
        return new DomaineResource(srv);
    }

    @Override
    protected Validation<Exception, Response> createResp(Domaine domaine, UriInfo uriInfo) {
        try {
            final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            final URI location = uriBuilder.path(domaine.code().value()).build();
            return success(Response.created(location).build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    @Override
    protected Validation<Exception, Response> readResp(Domaine domaine, final UriInfo uriInfo) {
        try {
            final ResponseBuilder sousDomsBuilder =
                    domaine.domaines().foldLeft(
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
                            Response.ok(domaineToDTO(domaine)));
            final ResponseBuilder appsBuilder =
                    domaine.applications().foldLeft(
                            new F2<ResponseBuilder, Application, ResponseBuilder>() {
                                public ResponseBuilder f(ResponseBuilder rb, Application app) {
                                    final String code = app.code().value();
                                    return rb.link(
                                            uriInfo.getBaseUriBuilder()
                                                    .path(ApplicationResource.class)
                                                    .path(code)
                                                    .build(),
                                            "app:" + code);
                                }
                            },
                            sousDomsBuilder);
            return success(appsBuilder.build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    @Override
    protected Validation<Exception, Domaine> validAndBuild(JsDom domaine) {
        return validApplications(domaine.applications()).map(arrayToList).nel()
                .accumapply(sm, validDomaines(domaine.domaines()).map(arrayToList).nel()
                        .accumapply(sm, validParent(domaine.parent()).nel()
                                .accumapply(sm, validLibelle(domaine.libelle()).nel()
                                        .accumapply(sm, validCode(domaine.code()).nel()
                                                .accumapply(sm, Validation.<String, Integer>success(-1).nel()
                                                        .map(curry(buildDomain)))))))
                .f().map(Functions.fieldsException);
    }

    private final F6<Integer, String, String, String, List<String>, List<String>, Domaine> buildDomain =
            new F6<Integer, String, String, String, List<String>, List<String>, Domaine>() {
                public Domaine f(Integer ver, String code, String lib, String parent, List<String> ssdoms, List<String> apps) {
                    return domaine(
                            version(ver),
                            code(code),
                            libelle(lib),
                            fromString(parent).map(domWithCode),
                            ssdoms.map(domWithCode),
                            apps.map(appWithCode));
                }
            };

}

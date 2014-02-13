package org.esupportail.catappsrvs.web;

import fj.F;
import fj.F2;
import fj.F6;
import fj.Function;
import fj.data.List;
import fj.data.NonEmptyList;
import fj.data.Option;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.services.IDomaine;
import org.esupportail.catappsrvs.web.dto.DomaineDTO;
import org.esupportail.catappsrvs.web.utils.Functions;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;

import static fj.Function.curry;
import static fj.data.Option.fromNull;
import static fj.data.Validation.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;
import static org.esupportail.catappsrvs.web.dto.Conversions.*;
import static org.esupportail.catappsrvs.web.dto.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.*;

@Slf4j @Value(staticConstructor = "domaineResource") @Getter(AccessLevel.NONE) // lombok
@Path("domaine") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public class DomaineResource {
    IDomaine domaineSrv;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response create(DomaineDTO domaine, @Context final UriInfo uriInfo) {
        return validAndBuild(domaine).nel()
                .bind(new F<Domaine, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(Domaine validDom) {
                        return validation(domaineSrv.create(validDom)).nel()
                                .bind(new F<Domaine, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(Domaine createdDom) {
                                        return createResp(createdDom, uriInfo).nel();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de création de Domaine"),
                        Function.<Response>identity());

    }

    @GET @Path("{code}")
    @Produces(APPLICATION_JSON)
    public Response read(@PathParam("code") String code, @Context final UriInfo uriInfo) {
        return validCode(code).nel()
                .f().map(Functions.fieldsException).nel()
                .bind(new F<String, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(String validCode) {
                        return validation(domaineSrv.read(code(validCode), Option.<Version>none())).nel()
                                .bind(new F<Domaine, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(Domaine domaine) {
                                        return readResp(domaine, uriInfo).nel();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur lors de la lecture d'un domaine"),
                        Function.<Response>identity());
    }

    private Validation<Exception, Response> createResp(Domaine domaine, UriInfo uriInfo) {
        try {
            final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            final URI location = uriBuilder.path(domaine.code().value()).build();
            return success(Response.created(location).build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    private Validation<Exception, Response> readResp(Domaine domaine, final UriInfo uriInfo) {
        try {
            final ResponseBuilder respBuilder =
                    domaine.sousDomaines().foldLeft(
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
            return success(respBuilder.build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    private Validation<Exception, Domaine> validAndBuild(DomaineDTO domaine) {
        return validApplications(domaine.applications()).map(arrayToList).nel()
                .accumapply(sm, validDomaines(domaine.domaines()).map(arrayToList).nel()
                        .accumapply(sm, Validation.<String, Option<String>>success(fromNull(domaine.parent())).nel()
                                .accumapply(sm, validLibelle(domaine.libelle()).nel()
                                        .accumapply(sm, validCode(domaine.code()).nel()
                                                .accumapply(sm, Validation.<String, Integer>success(-1).nel()
                                                        .map(curry(buildDomain)))))))
                .f().map(Functions.fieldsException);
    }

    private final F6<Integer,String,String,Option<String>,List<String>,List<String>,Domaine> buildDomain =
            new F6<Integer, String, String, Option<String>, List<String>, List<String>, Domaine>() {
                public Domaine f(Integer ver, String code, String lib, Option<String> parent, List<String> ssdoms, List<String> apps) {
                    return domaine(
                            version(ver),
                            code(code),
                            libelle(lib),
                            parent.map(domWithCode),
                            ssdoms.map(domWithCode),
                            apps.map(appWithCode));
                }
            };



}

package org.esupportail.catappsrvs.web;

import fj.F;
import fj.Function;
import fj.Unit;
import fj.data.NonEmptyList;
import fj.data.Option;
import fj.data.Validation;
import org.esupportail.catappsrvs.services.ICrud;
import org.esupportail.catappsrvs.web.dto.IDTO;
import org.esupportail.catappsrvs.web.utils.Functions;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static fj.data.$._;
import static fj.data.Validation.validation;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.Versionned.Version;
import static org.esupportail.catappsrvs.web.dto.Validations.validCode;
import static org.esupportail.catappsrvs.web.utils.Functions.treatErrors;

public abstract class CrudResource<T, D extends IDTO<T>> {
    protected final ICrud<T> srv;

    protected CrudResource(ICrud<T> srv) { this.srv = srv; }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public final Response create(D dto, @Context final UriInfo uriInfo) {
        return validAndBuild(dto).nel()
                .bind(new F<T, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(T validT) {
                        return validation(srv.create(validT)).nel()
                                .bind(new F<T, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(T createdT) {
                                        return createResp(createdT, uriInfo).nel();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de création d'une entité"),
                        Function.<Response>identity());
    }

    @GET @Path("{code}")
    @Produces(APPLICATION_JSON)
    public final Response read(@PathParam("code") String code, @Context final UriInfo uriInfo) {
        return validCode(code).nel()
                .f().map(Functions.fieldsException).nel()
                .bind(new F<String, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(String validCode) {
                        return validation(srv.read(code(validCode), Option.<Version>none())).nel()
                                .bind(new F<T, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(T t) {
                                        return readResp(t, uriInfo).nel();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de lecture d'une entité"),
                        Function.<Response>identity());
    }

    @PUT @Path("{code}")
    @Consumes(APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public final Response update(@PathParam("code") String code, final D dto) {
        return validCode(code).nel()
                .f().map(Functions.fieldsException).nel()
                .bind(new F<String, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(String validCode) {
                        return validAndBuild(dto.<D>withCode(validCode)).nel()
                                .bind(new F<T, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(T validT) {
                                        return validation(srv.update(validT)).nel()
                                                .map(new F<T, Response>() {
                                                    public Response f(T domaine) {
                                                        return Response.ok().build();
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de mise à jour d'une entité"),
                        Function.<Response>identity());
    }

    @DELETE @Path("{code}")
    @Produces(MediaType.TEXT_PLAIN)
    public final Response delete(@PathParam("code") String code) {
        return validCode(code).nel()
                .f().map(Functions.fieldsException).nel()
                .bind(new F<String, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(String validCode) {
                        return validation(srv.delete(code(validCode))).nel()
                                .map(_(Response.noContent().build()).<Unit>constant());
                    }
                })
                .validation(
                        treatErrors("Erreur d'effacement d'une entité"),
                        Function.<Response>identity());
    }


    protected abstract Validation<Exception, T> validAndBuild(D dto);

    protected abstract Validation<Exception, Response> createResp(T T, UriInfo uriInfo);

    protected abstract Validation<Exception, Response> readResp(T t, final UriInfo uriInfo);
}

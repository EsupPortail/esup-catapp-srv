package org.esupportail.catappsrvs.web;

import fj.*;
import fj.data.*;
import fj.data.List;
import org.esupportail.catappsrvs.services.ICrud;
import org.esupportail.catappsrvs.web.dto.JsHasCode;
import org.esupportail.catappsrvs.web.utils.Functions;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static fj.Function.curry;
import static fj.data.$._;
import static fj.data.Array.array;
import static fj.data.Array.single;
import static fj.data.List.iterableList;
import static fj.data.Set.iterableSet;
import static fj.data.Validation.validation;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.Versionned.Version;
import static org.esupportail.catappsrvs.web.dto.Validations.validCode;
import static org.esupportail.catappsrvs.web.utils.Functions.fieldsException;
import static org.esupportail.catappsrvs.web.utils.Functions.treatErrors;

@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class CrudResource<T, D extends JsHasCode<T>> {
    protected final ICrud<T> srv;

    protected CrudResource(ICrud<T> srv) { this.srv = srv; }

    private static final Ord<Link> linkOrd = Ord.stringOrd.comap(new F<Link, String>() {
        public String f(Link link) {
            return link.getRel();
        }
    });

    @GET @Path("{code}/exists")
    @Produces(APPLICATION_JSON)
    public final Response exists(@PathParam("code") String code) {
        return validCode(code).nel()
                .f().map(fieldsException).nel()
                .bind(new F<String, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(String validCode) {
                        return validation(srv.exists(code(validCode))).nel()
                                .map(new F<Boolean, Response>() {
                                    public Response f(final Boolean bool) {
                                        return Response.ok(new Object() {
                                            public boolean exists = bool;
                                        }).build();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors(""),
                        Function.<Response>identity());
    }

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

    @GET
    @Produces(APPLICATION_JSON)
    public final Response list(@Context final UriInfo uriInfo) {
        return validation(srv.list()).nel()
                .bind(new F<List<T>, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(List<T> ts) {
                        return validation(Either.sequenceRight(iterableList(ts)
                                .map(new F<T, Either<Exception, ResponseBuilder>>() {
                                    public Either<Exception, ResponseBuilder> f(T t) {
                                        return readResp(t, uriInfo).toEither()
                                                .right()
                                                .map(new F<Response, ResponseBuilder>() {
                                                    public ResponseBuilder f(Response response) {
                                                        return Response.fromResponse(response);
                                                    }
                                                });
                                    }
                                })))
                                .map(new F<fj.data.List<ResponseBuilder>, Response>() {
                                    public Response f(fj.data.List<ResponseBuilder> rbs) {
                                        return rbs.foldLeft(
                                                new F2<ResponseBuilder, ResponseBuilder, ResponseBuilder>() {
                                                    public ResponseBuilder f(ResponseBuilder target, ResponseBuilder orig) {
                                                        final Response oresp = orig.build();
                                                        final Response tresp = target.build();
                                                        final Link[] links =
                                                                iterableSet(linkOrd, tresp.getLinks())
                                                                        .union(iterableSet(linkOrd, oresp.getLinks()))
                                                                        .toList()
                                                                        .array(Link[].class);
                                                        final Object[] entities =
                                                                array((Object[]) tresp.getEntity())
                                                                        .append(single(oresp.getEntity()))
                                                                        .array(Object[].class);
                                                        return target.links(null).links(links).entity(entities);
                                                    }
                                                },
                                                Response.ok(new Object[]{}))
                                                .build();
                                    }
                                })
                                .nel();
                    }
                })
                .validation(
                        treatErrors(""),
                        Function.<Response>identity()
                );
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

package org.esupportail.catappsrvs.web;

import fj.Ord;
import fj.data.Either;
import fj.data.List;
import fj.data.Validation;
import org.esupportail.catappsrvs.model.CommonTypes.Code;
import org.esupportail.catappsrvs.services.ICrud;
import org.esupportail.catappsrvs.web.json.JsHasCode;
import org.esupportail.catappsrvs.web.utils.Functions;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static fj.data.Array.array;
import static fj.data.Array.single;
import static fj.data.List.iterableList;
import static fj.data.Set.iterableSet;
import static fj.data.Validation.validation;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.esupportail.catappsrvs.utils.logging.Log.Info;
import static org.esupportail.catappsrvs.web.json.Validations.validCode;
import static org.esupportail.catappsrvs.web.utils.Functions.errorResponse;
import static org.esupportail.catappsrvs.web.utils.Functions.fieldsException;

@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class CrudResource<T, S extends ICrud<T>, J extends JsHasCode<J>> {
    protected final S srv;

    protected CrudResource(S srv) { this.srv = srv; }

    private static final Ord<Link> linkOrd = Ord.stringOrd.contramap(Link::getRel);

    @GET @Path("{code}/exists")
    @Produces(APPLICATION_JSON)
    public final Response exists(@PathParam("code") final String code) {
        return Info._(this, "exists", code).log(() -> validCode(code)
            .nel().f()
            .map(fieldsException).nel()
            .bind(validCode -> validation(srv.exists(Code.of(validCode)))
                .nel()
                .map(bool -> Response.ok(new Object() {
                    public final boolean exists = bool;
                }).build()))
            .validation(errorResponse("Erreur de vérification d'existence d'une entité"),
                        __ -> __));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public final Response create(final J dto, @Context final UriInfo uriInfo) {
        return Info._(this, "create", dto, uriInfo).log(() -> validAndBuild(dto)
            .nel()
            .bind(validT -> validation(srv.create(validT))
                .nel()
                .bind(createdT -> createResp(createdT, uriInfo).nel()))
            .validation(errorResponse("Erreur de création d'une entité"),
                        __ -> __));
    }

    @GET @Path("{code}")
    @Produces(APPLICATION_JSON)
    public final Response read(@PathParam("code") final String code, @Context final UriInfo uriInfo) {
        return Info._(this, "read", code, uriInfo).log(() -> validCode(code)
            .nel().f()
            .map(Functions.fieldsException).nel()
            .bind(validCode -> validation(srv.read(Code.of(validCode)))
                .nel()
                .bind(t -> readResp(t, uriInfo).nel()))
            .validation(errorResponse("Erreur de lecture d'une entité"),
                        __ -> __));
    }

    @GET
    @Produces(APPLICATION_JSON)
    public final Response list(@Context final UriInfo uriInfo) {
        return Info._(this, "list", uriInfo).log(() -> validation(srv.list())
            .nel()
            .bind((List<T> ts) ->
                      validation(Either.sequenceRight(iterableList(ts).map(t -> readResp(t, uriInfo)
                          .toEither()
                          .right()
                          .map(Response::fromResponse))))
                          .map(rbs -> rbs
                              .foldLeft(target -> orig -> {
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
                                            // null demandé par jersey...
                                            return target.links(null).links(links).entity(entities);
                                        },
                                        Response.ok(new Object[]{})).build()).nel())
            .validation(errorResponse("Erreur de lecture d'une liste d'entités"),
                        __ -> __));
    }


    @PUT @Path("{code}")
    @Consumes(APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public final Response update(@PathParam("code") final String code, final J json) {
        return Info._(this, "update", code, json).log(() -> validCode(code)
            .nel().f()
            .map(Functions.fieldsException).nel()
            .bind(validCode -> validAndBuild(json.withCode(validCode))
                .nel()
                .bind(validT -> validation(srv.update(validT))
                    .nel()
                    .map(__ -> Response.ok().build())))
            .validation(errorResponse("Erreur de mise à jour d'une entité"),
                        __ -> __));
    }

    @DELETE @Path("{code}")
    @Produces(MediaType.TEXT_PLAIN)
    public final Response delete(@PathParam("code") final String code) {
        return Info._(this, "delete", code).log(() -> validCode(code)
            .nel().f()
            .map(Functions.fieldsException).nel()
            .bind(validCode -> validation(srv.delete(Code.of(validCode)))
                .nel()
                .map(__ -> Response.noContent().build()))
            .validation(errorResponse("Erreur d'effacement d'une entité"),
                        __ -> __));
    }


    protected abstract Validation<Exception, T> validAndBuild(J json);

    protected abstract Validation<Exception, Response> createResp(T T, UriInfo uriInfo);

    protected abstract Validation<Exception, Response> readResp(T t, final UriInfo uriInfo);
}

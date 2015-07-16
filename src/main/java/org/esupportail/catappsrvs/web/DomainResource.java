package org.esupportail.catappsrvs.web;

import fj.*;
import fj.data.List;
import fj.data.Option;
import fj.data.Tree;
import fj.data.Validation;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.CommonTypes.Caption;
import org.esupportail.catappsrvs.model.CommonTypes.Code;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.User;
import org.esupportail.catappsrvs.services.IDomain;
import org.esupportail.catappsrvs.web.json.JSDomTree;
import org.esupportail.catappsrvs.web.json.JsDom;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static fj.F1Functions.mapOption;
import static fj.Function.compose;
import static fj.Function.curry;
import static fj.data.Option.fromString;
import static fj.data.Validation.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.esupportail.catappsrvs.model.User.Uid;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;
import static org.esupportail.catappsrvs.utils.logging.Log.Info;
import static org.esupportail.catappsrvs.web.json.Conversions.*;
import static org.esupportail.catappsrvs.web.json.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.*;

@Slf4j // lombok
@Path("domains") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public final class DomainResource extends CrudResource<Domain, IDomain, JsDom> {
    private DomainResource(IDomain srv) {
        super(srv);
    }

    public static DomainResource of(IDomain srv) {
        return new DomainResource(srv);
    }

    @GET
    @Path("{code}/tree")
    @Produces(APPLICATION_JSON)
    public final Response findDomaines(@PathParam("code") final String code,
                                       @QueryParam("user") final String uid) {
        return Info._(this, "findDomaines", code, uid).log(() -> validCode(code)
            .nel()
            .accumulate(sm, validNotEmpty("le paramètre user", uid).nel(), P.p2())
            .f().map(fieldsException).nel()
            .bind(pair -> validation(srv.findDomaines(Code.of(pair._1()), User.of(Uid.of(pair._2()))))
                .nel()
                .map(domainesTree -> {
                    final Tree<Option<JSDomTree>> jsTree = domainesTree
                        .fmap(mapOption(compose(jsDom -> JSDomTree.of(jsDom, new JSDomTree[0]),
                                                domaineToJson)));

                    final Tree<Option<JSDomTree>> resultTree = Tree.<Option<JSDomTree>, Option<JSDomTree>>
                        bottomUp(jsTree, P2.tuple(parent -> children -> parent.map(jsDomTree -> jsDomTree
                        .withSubDomains(Option.somes(children).array(JSDomTree[].class)))));

                    return resultTree.root().option(Response.status(NOT_FOUND).build(),
                                                    jsDomTree -> Response.ok(jsDomTree).build());
                }))
            .validation(errorResponse("Erreur de lecture d'un arbre d'entités"),
                        __ -> __));
    }


    @Override
    protected Validation<Exception, Response> createResp(Domain domain, UriInfo uriInfo) {
        try {
            final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            final URI location = uriBuilder.path(domain.code().value()).build();
            return success(Response.created(location).build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    @Override
    protected Validation<Exception, Response> readResp(Domain domain, final UriInfo uriInfo) {
        try {
            final ResponseBuilder subDomsBuilder = domain
                .domains()
                .foldLeft((rb, dom) -> {
                              final String code = dom.code().value();
                              return rb.link(
                                  uriInfo.getBaseUriBuilder()
                                         .path(DomainResource.class)
                                         .path(code)
                                         .build(),
                                  "dom:" + code);
                          },
                          Response.ok(domainToJson(domain)));

            final ResponseBuilder appsBuilder = domain
                .applications()
                .foldLeft((rb, app) -> {
                              final String code = app.code().value();
                              return rb.link(
                                  uriInfo.getBaseUriBuilder()
                                         .path(ApplicationResource.class)
                                         .path(code)
                                         .build(),
                                  "app:" + code);
                          },
                          subDomsBuilder);
            return success(appsBuilder.build());
        } catch (Exception e) {
            return fail(e);
        }
    }

    @Override
    protected Validation<Exception, Domain> validAndBuild(JsDom json) {
        return Validation.<String, Integer>success(-1).nel()
                         .accumulate(sm,
                                     validCode(json.code()).nel(),
                                     validLibelle(json.caption()).nel(),
                                     validParent(json.parent()).nel(),
                                     validDomaines(json.domains()).map(arrayToList).nel(),
                                     validApplications(json.applications()).map(arrayToList).nel(),
                                     curry(buildDomain))
                         .f()
                         .map(fieldsException);
    }

    private final F6<Integer, String, String, String, List<String>, List<String>, Domain> buildDomain =
        (ver, code, lib, parent, ssdoms, apps) ->
            Debug._(this, "buildDomain", ver, code, lib, parent, ssdoms, apps)
                 .log(() -> Domain.of(Code.of(code),
                                      Caption.of(lib),
                                      fromString(parent).map(domWithCode),
                                      ssdoms.map(domWithCode),
                                      apps.map(appWithCode)));

}

package org.esupportail.catappsrvs.web;

import fj.*;
import fj.data.*;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.services.IDomain;
import org.esupportail.catappsrvs.web.json.JSDomTree;
import org.esupportail.catappsrvs.web.json.JsDom;
import org.esupportail.catappsrvs.web.json.Validations;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static fj.Function.curry;
import static fj.data.Option.*;
import static fj.data.Validation.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.esupportail.catappsrvs.model.CommonTypes.Caption.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.Domain.*;
import static org.esupportail.catappsrvs.model.User.Uid.*;
import static org.esupportail.catappsrvs.model.User.*;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;
import static org.esupportail.catappsrvs.utils.logging.Log.Info;
import static org.esupportail.catappsrvs.web.json.Conversions.*;
import static org.esupportail.catappsrvs.web.json.JSDomTree.jsDomTree;
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

    public static DomainResource domaineResource(IDomain srv) {
        return new DomainResource(srv);
    }

    @GET
    @Path("{code}/tree")
    @Produces(APPLICATION_JSON)
    public final Response findDomaines(@PathParam("code") final String code,
                                       @QueryParam("user") final String uid) {
        return Info._(this, "findDomaines", code, uid).log(new P1<Response>() {
            public Response _1() {
                return validCode(code).nel()
                        .accumulate(sm,
                                validNotEmpty("le paramètre user", uid).nel(),
                                P.<String, String>p2())
                        .f().map(fieldsException).nel()
                        .bind(new F<P2<String, String>, Validation<NonEmptyList<Exception>, Response>>() {
                            public Validation<NonEmptyList<Exception>, Response> f(P2<String, String> pair) {
                                return validation(srv.findDomaines(code(pair._1()), user(uid(pair._2())))).nel()
                                        .map(new F<Tree<Option<Domain>>, Response>() {
                                            public Response f(Tree<Option<Domain>> domainesTree) {
                                                final Tree<Option<JSDomTree>> jsTree = domainesTree
                                                        .fmap(domaineToJson
                                                                .andThen(new F<JsDom, JSDomTree>() {
                                                                    public JSDomTree f(JsDom jsDom) {
                                                                        return jsDomTree(jsDom, new JSDomTree[0]);
                                                                    }
                                                                })
                                                                .mapOption());

                                                final Option<JSDomTree> result =
                                                        Tree.bottomUp(jsTree, new F<P2<Option<JSDomTree>, Stream<Option<JSDomTree>>>, Option<JSDomTree>>() {
                                                            public Option<JSDomTree> f(P2<Option<JSDomTree>, Stream<Option<JSDomTree>>> pair) {
                                                                final Option<JSDomTree> parent = pair._1();
                                                                final Stream<Option<JSDomTree>> children = pair._2();
                                                                return parent.map(new F<JSDomTree, JSDomTree>() {
                                                                    public JSDomTree f(JSDomTree jsDomTree) {
                                                                        return jsDomTree
                                                                                .withSubDomains(somes(children).array(JSDomTree[].class));
                                                                    }
                                                                });
                                                            }
                                                        }).root();

                                                return result.option(
                                                        new P1<Response>() {
                                                            public Response _1() {
                                                                return Response.status(NOT_FOUND).build();
                                                            }
                                                        },
                                                        new F<JSDomTree, Response>() {
                                                            public Response f(JSDomTree jsDomTree) {
                                                                return Response.ok(jsDomTree).build();
                                                            }
                                                        }
                                                );
                                            }
                                        });
                            }
                        })
                        .validation(
                                errorResponse("Erreur de lecture d'un arbre d'entités"),
                                Function.<Response>identity());

            }
        });
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
            final ResponseBuilder sousDomsBuilder =
                    domain.domains().foldLeft(
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
                            Response.ok(domaineToJson(domain)));
            final ResponseBuilder appsBuilder =
                    domain.applications().foldLeft(
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
    protected Validation<Exception, Domain> validAndBuild(JsDom domaine) {
        return validApplications(domaine.applications()).map(arrayToList).nel()
                .accumapply(sm, validDomaines(domaine.domains()).map(arrayToList).nel()
                        .accumapply(sm, validParent(domaine.parent()).nel()
                                .accumapply(sm, validLibelle(domaine.caption()).nel()
                                        .accumapply(sm, validCode(domaine.code()).nel()
                                                .accumapply(sm, Validation.<String, Integer>success(-1).nel()
                                                        .map(curry(buildDomain)))))))
                .f().map(fieldsException);
    }

    private final F6<Integer, String, String, String, List<String>, List<String>, Domain> buildDomain =
            new F6<Integer, String, String, String, List<String>, List<String>, Domain>() {
                public Domain f(Integer ver, final String code, final String lib, final String parent, final List<String> ssdoms, final List<String> apps) {
                    return Debug._(this, "buildDomain", ver, code, lib, parent, ssdoms, apps).log(new P1<Domain>() {
                        public Domain _1() {
                            return domain(
                                    code(code),
                                    caption(lib),
                                    fromString(parent).map(domWithCode),
                                    ssdoms.map(domWithCode),
                                    apps.map(appWithCode));
                        }
                    });
                }
            };

}

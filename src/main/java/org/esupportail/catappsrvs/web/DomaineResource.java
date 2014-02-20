package org.esupportail.catappsrvs.web;

import fj.*;
import fj.data.*;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.services.IDomaine;
import org.esupportail.catappsrvs.web.dto.JSDomTree;
import org.esupportail.catappsrvs.web.dto.JsDom;
import org.esupportail.catappsrvs.web.dto.Validations;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static fj.Function.curry;
import static fj.data.Array.array;
import static fj.data.Array.single;
import static fj.data.Option.*;
import static fj.data.Validation.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ResponseBuilder;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.model.User.Uid.*;
import static org.esupportail.catappsrvs.model.User.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;
import static org.esupportail.catappsrvs.web.dto.Conversions.*;
import static org.esupportail.catappsrvs.web.dto.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.*;

@Slf4j // lombok
@Path("domains") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public final class DomaineResource extends CrudResource<Domaine, IDomaine, JsDom> {
    private DomaineResource(IDomaine srv) {
        super(srv);
    }

    public static DomaineResource domaineResource(IDomaine srv) {
        return new DomaineResource(srv);
    }

    @GET
    @Path("{code}/tree")
    @Produces(APPLICATION_JSON)
    public final Response findDomaines(@PathParam("code") String code,
                                       @QueryParam("user") String uid) {
        return validCode(code).nel()
                .accumulate(
                        Validations.sm,
                        Validations.validNotEmpty("le paramètre user", uid).nel(),
                        P.<String, String>p2())
                .f().map(fieldsException).nel()
                .bind(new F<P2<String, String>, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(P2<String, String> pair) {
                        return validation(srv.findDomaines(code(pair._1()), user(uid(pair._2())))).nel()
                                .map(new F<Tree<Option<Domaine>>, Response>() {
                                    public Response f(Tree<Option<Domaine>> domaines) {
                                        final Tree<Option<JsDom>> doms = domaines.fmap(domaineToDTO.mapOption());
                                        final Tree<Option<JSDomTree>> domTree =
                                                doms.fmap(new F<Option<JsDom>, Option<JSDomTree>>() {
                                                    public Option<JSDomTree> f(Option<JsDom> opt) {
                                                        return opt.map(new F<JsDom, JSDomTree>() {
                                                            public JSDomTree f(JsDom jsDom) {
                                                                return JSDomTree.jsDomTree(jsDom, new JSDomTree[0]);
                                                            }
                                                        });
                                                    }
                                                });
                                        final Stream<Stream<Option<JSDomTree>>> levels = domTree.levels();

                                        final Stream<Stream<Option<JSDomTree>>> trees = levels.length() == 1
                                                ? Stream.single(Stream.single(domTree.root()))
                                                : levels.zipWith(levels.tail()._1(), new F2<Stream<Option<JSDomTree>>, Stream<Option<JSDomTree>>, Stream<Option<JSDomTree>>>() {
                                                    public Stream<Option<JSDomTree>> f(Stream<Option<JSDomTree>> level1, final Stream<Option<JSDomTree>> level2) {
                                                        return level1.map(new F<Option<JSDomTree>, Option<JSDomTree>>() {
                                                            public Option<JSDomTree> f(final Option<JSDomTree> opt) {
                                                                final JSDomTree[] subDomains =
                                                                        somes(level2)
                                                                                .filter(new F<JSDomTree, Boolean>() {
                                                                                    public Boolean f(final JSDomTree child) {
                                                                                        return opt.exists(new F<JSDomTree, Boolean>() {
                                                                                            public Boolean f(JSDomTree parent) {
                                                                                                return parent.domain().code()
                                                                                                        .equals(child.domain().parent());
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                })
                                                                                .array(JSDomTree[].class);
                                                                return opt.map(new F<JSDomTree, JSDomTree>() {
                                                                    public JSDomTree f(JSDomTree jsDomTree) {
                                                                        return jsDomTree.withSubDomains(subDomains);
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });

                                        final Option<JSDomTree> result =
                                                trees.foldLeft(
                                                        new F2<Option<JSDomTree>, Stream<Option<JSDomTree>>, Option<JSDomTree>>() {
                                                            public Option<JSDomTree> f(Option<JSDomTree> acc, final Stream<Option<JSDomTree>> level) {
                                                                return acc.bind(new F<JSDomTree, Option<JSDomTree>>() {
                                                                    public Option<JSDomTree> f(JSDomTree jsDomTree) {
                                                                        return jsDomTree.equals(emptyJsDomTree)
                                                                                ? level.head()
                                                                                : some(jsDomTree.withSubDomains(somes(level).array(JSDomTree[].class)));
                                                                    }
                                                                });
                                                            }
                                                        },
                                                        some(emptyJsDomTree));

                                        return Response.ok(result.orSome(emptyJsDomTree)).build();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de lecture d'un arbre d'entités"),
                        Function.<Response>identity());
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
                                .accumapply(sm, validLibelle(domaine.wording()).nel()
                                        .accumapply(sm, validCode(domaine.code()).nel()
                                                .accumapply(sm, Validation.<String, Integer>success(-1).nel()
                                                        .map(curry(buildDomain)))))))
                .f().map(fieldsException);
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

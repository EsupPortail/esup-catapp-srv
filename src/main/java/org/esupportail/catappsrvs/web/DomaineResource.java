package org.esupportail.catappsrvs.web;

import fj.F;
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static fj.Function.curry;
import static fj.data.Option.fromNull;
import static fj.data.Validation.fail;
import static fj.data.Validation.success;
import static fj.data.Validation.validation;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle.*;
import static org.esupportail.catappsrvs.model.Domaine.*;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;
import static org.esupportail.catappsrvs.web.dto.Conversions.*;
import static org.esupportail.catappsrvs.web.dto.Validations.*;
import static org.esupportail.catappsrvs.web.utils.Functions.*;

@Slf4j @Value(staticConstructor = "domaineResource") @Getter(AccessLevel.NONE) // lombok
@Path("/domaine") // jaxrs
@Component // spring
@SuppressWarnings("SpringJavaAutowiringInspection") // intellij
public class DomaineResource {
    IDomaine domaineSrv;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(DomaineDTO domaine) {
        return validAndBuild(domaine).nel()
                .bind(new F<Domaine, Validation<NonEmptyList<Exception>, Response>>() {
                    public Validation<NonEmptyList<Exception>, Response> f(Domaine validDom) {
                        return validation(domaineSrv.create(validDom)).nel()
                                .bind(new F<Domaine, Validation<NonEmptyList<Exception>, Response>>() {
                                    public Validation<NonEmptyList<Exception>, Response> f(Domaine createdDom) {
                                        return buildResponse(createdDom).nel();
                                    }
                                });
                    }
                })
                .validation(
                        treatErrors("Erreur de création de Domaine"),
                        Function.<Response>identity());

    }

    private Validation<Exception, Response> buildResponse(Domaine domaine) {
        try {
            return success(Response.created(new URI("domaine/" + domaine.code().value())).build());
        } catch (URISyntaxException e) {
            return fail((Exception) e);
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

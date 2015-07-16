package org.esupportail.catappsrvs.web.json;

import fj.F;
import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.CommonTypes.*;
import org.esupportail.catappsrvs.model.Domain;

import static org.esupportail.catappsrvs.model.Application.Activation.Activated;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;
import static org.esupportail.catappsrvs.web.json.JsApp.JsActivation;
import static org.esupportail.catappsrvs.web.json.JsApp.jsApp;
import static org.esupportail.catappsrvs.web.json.JsDom.jsDom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Conversions {

    // ######## Applications

    private static final Application emptyApp =
        Application.of(Code.of(""),
                       Title.of(""),
                       Caption.of(""),
                       Description.of(""),
                       null, // TODO : ???
                       Activated,
                       LdapGroup.of(""),
                       List.nil());

    public static final F<String, Application> appWithCode = code -> emptyApp.withCode(Code.of(code));

    public static final F<Application, String> appCode = application -> application.code().value();

    public static JsApp applicationToJson(final Application app) {
        return Debug._(Conversions.class.getName(), "applicationToJson", app)
                    .log(() -> jsApp(app.code().value(),
                                     app.title().value(),
                                     app.caption().value(),
                                     app.url().toString(),
                                     JsActivation.valueOf(app.activation().name()),
                                     app.description().value(),
                                     app.group().value(),
                                     app.domains().map(domCode).array(String[].class)));
    }

    // ######## Domaines

    public static final Domain emptyDom =
        Domain.of(Code.of(""),
                  Caption.of(""),
                  Option.none(),
                  List.nil(),
                  List.nil());

    public static final F<String,Domain> domWithCode =
        code -> emptyDom.withCode(Code.of(code));

    public static final F<Domain,String> domCode = domain -> domain.code().value();

    public static final F<Domain, JsDom> domaineToJson = Conversions::domainToJson;

    public static JsDom domainToJson(final Domain domain) {
        return Debug._(Conversions.class.getName(), "domainToJson", domain)
                    .log(() -> jsDom(domain.code().value(),
                                     domain.caption().value(),
                                     domain.parent().option("", domCode),
                                     domain.domains().map(domCode).array(String[].class),
                                     domain.applications().map(appCode).array(String[].class)));
    }

    // ########## Domaines (arborescent)

    public static final JSDomTree emptyJsDomTree =
        JSDomTree.of(domainToJson(emptyDom), new JSDomTree[0]);

}

package org.esupportail.catappsrvs.web.json;

import fj.*;
import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.utils.logging.Log;

import static org.esupportail.catappsrvs.model.Application.Activation.Activated;
import static org.esupportail.catappsrvs.model.CommonTypes.Caption.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Description.*;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.*;
import static org.esupportail.catappsrvs.model.CommonTypes.Title.*;
import static org.esupportail.catappsrvs.model.Domain.*;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;
import static org.esupportail.catappsrvs.web.json.JsApp.JsActivation;
import static org.esupportail.catappsrvs.web.json.JsApp.jsApp;
import static org.esupportail.catappsrvs.web.json.JsDom.jsDom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Conversions {

    // ######## Applications

    private static final Application emptyApp =
            Application.application(
                    code(""),
                    title(""),
                    caption(""),
                    description(""),
                    null,
                    Activated,
                    ldapGroup(""),
                    List.<Domain>nil());

    public static final F<String, Application> appWithCode = new F<String, Application>() {
        public Application f(String code) {
            return emptyApp.withCode(code(code));
        }
    };

    public static final F<Application, String> appCode = new F<Application, String>() {
        public String f(Application application) {
            return application.code().value();
        }
    };

    public static JsApp applicationToJson(final Application app) {
        return Debug._(Conversions.class.getName(), "applicationToJson", app).log(new P1<JsApp>() {
            public JsApp _1() {
                return jsApp(
                        app.code().value(),
                        app.title().value(),
                        app.caption().value(),
                        app.url().toString(),
                        JsActivation.valueOf(app.activation().name()),
                        app.description().value(),
                        app.group().value(),
                        app.domains().map(domCode).array(String[].class));
            }
        });
    }

    // ######## Domaines

    public static final Domain emptyDom =
            domain(code(""),
                    caption(""),
                    Option.<Domain>none(),
                    List.<Domain>nil(),
                    List.<Application>nil());

    public static final F<String,Domain> domWithCode =
            new F<String, Domain>() {
                public Domain f(String code) {
                    return emptyDom.withCode(code(code));
                }
            };

    public static final F<Domain,String> domCode = new F<Domain, String>() {
        public String f(Domain domain) {
            return domain.code().value();
        }
    };

    public static final F<Domain, JsDom> domaineToJson = new F<Domain, JsDom>() {
        public JsDom f(Domain domain) {
            return domaineToJson(domain);
        }
    };

    public static JsDom domaineToJson(final Domain domain) {
        return Debug._(Conversions.class.getName(), "domaineToJson", domain).log(new P1<JsDom>() {
            public JsDom _1() {
                return jsDom(
                        domain.code().value(),
                        domain.caption().value(),
                        domain.parent().option("", domCode),
                        domain.domains().map(domCode).array(String[].class),
                        domain.applications().map(appCode).array(String[].class));
            }
        });
    }

    // ########## Domaines (arborescent)

    public static final JSDomTree emptyJsDomTree =
                JSDomTree.jsDomTree(domaineToJson(emptyDom), new JSDomTree[0]);

}

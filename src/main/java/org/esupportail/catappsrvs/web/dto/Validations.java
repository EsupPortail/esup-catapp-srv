package org.esupportail.catappsrvs.web.dto;

import fj.F;
import fj.Function;
import fj.Semigroup;
import fj.data.NonEmptyList;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static fj.data.Option.fromNull;
import static fj.data.Option.fromString;
import static fj.data.Validation.success;
import static fj.data.Validation.validation;
import static org.esupportail.catappsrvs.web.dto.JsApp.Acces;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validations {
    public static final Semigroup<NonEmptyList<String>> sm = Semigroup.nonEmptyListSemigroup();

    public static <T> Validation<String, T> validNotNull(String fieldName, T t) {
        return t != null
                ? Validation.<String, T>success(t)
                : Validation.<String, T>fail(fieldName + " est null");
    }

    public static Validation<String, String> validNotEmpty(String fieldName, String value) {
        return validation(fromString(value).toEither(fieldName + " est vide"));
    }

    public static Validation<String, String> validCode(String code) {
        return validNotNull("code", code)
                .apply(validNotEmpty("code", code).map(Function.<String, String>constant()));
    }

    public static Validation<String, String> validTitre(String titre) {
        return validNotNull("titre", titre)
                .apply(validNotEmpty("titre", titre).map(Function.<String, String>constant()));
    }

    public static Validation<String, String> validLibelle(String libelle) {
        return success(fromString(libelle).orSome(""));
    }

    public static Validation<String, String> validUrl(String url) {
        return validNotNull("url", url)
                .apply(validNotEmpty("url", url).map(Function.<String, String>constant()));
    }

    public static Validation<String, Acces> validAccess(Acces access) {
        return validNotNull("accessibilite", access)
                .bind(new F<Acces, Validation<String, Acces>>() {
                    public Validation<String, Acces> f(Acces acces) {
                        return validNotEmpty("accessibilite", acces.name()).map(new F<String, Acces>() {
                            public Acces f(String s) {
                                return Acces.valueOf(s);
                            }
                        });
                    }
                });
    }

    public static Validation<String, String> validDescr(String descr) {
        return success(fromString(descr).orSome(""));
    }

    public static Validation<String, String> validGroupe(String groupe) {
        return validNotNull("groupe", groupe)
                .apply(validNotEmpty("groupe", groupe).map(Function.<String, String>constant()));
    }

    public static Validation<String, String[]> validDomaines(String[] domaines) {
        return success(fromNull(domaines).orSome(new String[0]));
    }

    public static Validation<String, String[]> validApplications(String[] applications) {
        return success(fromNull(applications).orSome(new String[0]));
    }

    public static Validation<String, String> validParent(String parent) {
        return success(fromString(parent).orSome(""));
    }
}

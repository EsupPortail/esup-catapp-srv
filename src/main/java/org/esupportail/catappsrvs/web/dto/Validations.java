package org.esupportail.catappsrvs.web.dto;

import fj.Semigroup;
import fj.data.NonEmptyList;
import fj.data.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validations {
    public static final Semigroup<NonEmptyList<String>> sm = Semigroup.nonEmptyListSemigroup();

    public static <T> Validation<String, T> validNotNull(String fieldName, T t) {
        return t != null
                ? Validation.<String, T>success(t)
                : Validation.<String, T>fail(fieldName + " is null");
    }

    public static <T> Validation<String, T> validCode(T t) {
        return validNotNull("code", t);
    }

    public static <T> Validation<String, T> validTitre(T t) {
        return validNotNull("titre", t);
    }

    public static <T> Validation<String, T> validLibelle(T t) {
        return validNotNull("libelle", t);
    }

    public static <T> Validation<String, T> validUrl(T t) {
        return validNotNull("url", t);
    }

    public static <T> Validation<String, T> validAccess(T t) {
        return validNotNull("accessibilite", t);
    }

    public static <T> Validation<String, T> validDescr(T t) {
        return validNotNull("description", t);
    }

    public static <T> Validation<String, T> validGroupe(T t) {
        return validNotNull("groupe", t);
    }

    public static <T> Validation<String, T> validDomaines(T t) {
        return validNotNull("domaines", t);
    }

    public static <T> Validation<String, T> validApplications(T t) {
        return validNotNull("applications", t);
    }

}

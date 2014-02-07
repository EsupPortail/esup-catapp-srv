package org.esupportail.catappsrvs.model;

import lombok.Value;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static fj.Bottom.error;

public final class CommonTypes {
    private CommonTypes() { throw error("Unsupported"); }

    @Embeddable
    @Value(staticConstructor = "code")
    public static class Code { @Column(name = "code") String value; }

    @Embeddable
    @Value(staticConstructor = "libelle")
    public static class Libelle { @Column(name = "libelle") String value; }

    @Embeddable
    @Value(staticConstructor = "titre")
    public static class Titre { @Column(name = "titre") String value; }

    @Embeddable
    @Value(staticConstructor = "description")
    public static class Description { @Column(name = "description") String value; }

    @Embeddable
    @Value(staticConstructor = "ldapGroup")
    public static class LdapGroup { @Column(name = "groupe") String value; }
}

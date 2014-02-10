package org.esupportail.catappsrvs.model;

import fj.data.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.net.URL;
import java.util.ArrayList;

import static fj.data.List.iterableList;
import static org.esupportail.catappsrvs.model.CommonTypes.*;
import static org.esupportail.catappsrvs.model.Versionned.Version;

@EqualsAndHashCode(of = {"code", "version"}, doNotUseGetters = true)
@ToString @Getter @Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Immutable
public final class Application implements Versionned<Application> {
    public static enum Accessibility { Accessible, Unaccessible }

    @Getter(AccessLevel.NONE)
    @Id @Column(name = "pk", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long $pk;

    @NaturalId @Embedded @Wither
    @Column(nullable = false)
    final Version version;

    @NaturalId @Wither
    @Embedded @Column(nullable = false, length = 10)
    final Code code;

    @Embedded @Column(length = 200) @Wither
    final Titre titre;

    @Embedded @Column(length = 200) @Wither
    final Libelle libelle;

    @Embedded @Column(length = 3000) @Wither
    final Description description;

    @Column(length = 1000) @Wither
    final URL url;

    @Column @Enumerated @Wither
    final Accessibility accessibility;

    @Embedded @Column(length = 200) @Wither
    final LdapGroup groupe;

    @Getter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "applications")
    final java.util.List<Domaine> domaines;

    private Application() { // for hibernate
        version = null;
        code = null;
        titre = null;
        libelle = null;
        description = null;
        url = null;
        accessibility = null;
        groupe = null;
        domaines  = null;
    }

    private Application(Version version,
                        Code code,
                        Titre titre,
                        Libelle libelle,
                        Description description,
                        URL url,
                        Accessibility accessibility,
                        LdapGroup groupe,
                        java.util.List<Domaine> domaines) {
        this.version = version;
        this.code = code;
        this.titre = titre;
        this.libelle = libelle;
        this.description = description;
        this.url = url;
        this.accessibility = accessibility;
        this.groupe = groupe;
        this.domaines = domaines;
    }

    public static Application application(Version version,
                                          Code code,
                                          Titre titre,
                                          Libelle libelle,
                                          Description description,
                                          URL url,
                                          Accessibility accessibility,
                                          LdapGroup groupe,
                                          List<Domaine> domaines) {
        return new Application(version, code, titre, libelle, description, url, accessibility, groupe,
                new ArrayList<>(domaines.toCollection()));
    }

    public Application withDomaines(final List<Domaine> domaines) {
        return new Application(version, code, titre, libelle, description, url, accessibility, groupe,
                new ArrayList<>(domaines.toCollection()));
    }

    public Long pk() { return $pk; }

    public List<Domaine> domaines() { return iterableList(domaines); }
}

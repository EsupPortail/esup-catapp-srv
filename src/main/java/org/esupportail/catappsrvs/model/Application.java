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

@EqualsAndHashCode(of = {"code", "version"}, doNotUseGetters = true)
@ToString @Getter @Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Immutable
public final class Application implements Versionned<Application> {
    @Getter(AccessLevel.NONE)
    @Id @Column(name = "pk", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long $pk;

    @NaturalId @Embedded @Wither
    @Column(nullable = false)
    final Versionned.Version version;

    @NaturalId @Wither
    @Embedded @Column(nullable = false)
    final Code code;

    @Embedded @Column @Wither
    final Titre titre;

    @Embedded @Column @Wither
    final Libelle libelle;

    @Embedded @Column @Wither
    final Description description;

    @Column @Wither
    final URL url;

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
        domaines  = null;
    }

    private Application(Versionned.Version version,
                        Code code,
                        Titre titre,
                        Libelle libelle,
                        Description description,
                        URL url,
                        java.util.List<Domaine> domaines) {
        this.version = version;
        this.code = code;
        this.titre = titre;
        this.libelle = libelle;
        this.description = description;
        this.url = url;
        this.domaines = domaines;
    }

    public static Application application(Versionned.Version version,
                                          Code code,
                                          Titre titre,
                                          Libelle libelle,
                                          Description description,
                                          URL url,
                                          List<Domaine> domaines) {
        return new Application(version, code, titre, libelle, description, url,
                new ArrayList<>(domaines.toCollection()));
    }

    public Application withDomaines(final List<Domaine> domaines) {
        return application(version, code, titre, libelle, description, url, domaines);
    }

    public Long pk() { return $pk; }

    public List<Domaine> domaines() { return iterableList(domaines); }
}

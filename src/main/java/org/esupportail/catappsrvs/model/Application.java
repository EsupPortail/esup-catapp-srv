package org.esupportail.catappsrvs.model;

import fj.F2;
import fj.P4;
import fj.P5;
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
    public static enum Accessibilite { Accessible, Inaccessible }

    @Getter(AccessLevel.NONE)
    @Id @Column(name = "pk", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
    final Long pk;

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

    @Column @Enumerated(EnumType.STRING) @Wither
    final Accessibilite accessibilite;

    @Embedded @Column(length = 200) @Wither
    final LdapGroup groupe;

    @Getter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "applications")
    final java.util.List<Domaine> domaines;

    private Application() { // for hibernate
        pk = null;
        version = null;
        code = null;
        titre = null;
        libelle = null;
        description = null;
        url = null;
        accessibilite = null;
        groupe = null;
        domaines  = null;
    }

    private Application(Long pk,
                        Version version,
                        Code code,
                        Titre titre,
                        Libelle libelle,
                        Description description,
                        URL url,
                        Accessibilite accessibilite,
                        LdapGroup groupe,
                        java.util.List<Domaine> domaines) {
        this.pk = pk;
        this.version = version;
        this.code = code;
        this.titre = titre;
        this.libelle = libelle;
        this.description = description;
        this.url = url;
        this.accessibilite = accessibilite;
        this.groupe = groupe;
        this.domaines = domaines;
    }

    /**
     * TODO : trop d'arguments => refactorer
     */
    public static Application application(Version version,
                                          Code code,
                                          Titre titre,
                                          Libelle libelle,
                                          Description description,
                                          URL url,
                                          Accessibilite accessibility,
                                          LdapGroup groupe,
                                          List<Domaine> domaines) {
        return new Application(null, version, code, titre, libelle,
                description, url, accessibility, groupe,
                new ArrayList<>(domaines.toCollection()));
    }

    public static final F2<
            P4<Version, Code, Titre, Libelle>,
            P5<Description, URL, Accessibilite, LdapGroup, List<Domaine>>,
            Application> application =
            new F2<P4<Version, Code, Titre, Libelle>, P5<Description, URL, Accessibilite, LdapGroup, List<Domaine>>, Application>() {
                public Application f(P4<Version, Code, Titre, Libelle> p4, P5<Description, URL, Accessibilite, LdapGroup, List<Domaine>> p5) {
                    return application(
                            p4._1(), p4._2(), p4._3(), p4._4(),
                            p5._1(), p5._2(), p5._3(), p5._4(), p5._5());
                }
            };

    public Application withDomaines(final List<Domaine> domaines) {
        return new Application(pk, version, code, titre, libelle,
                description, url, accessibilite, groupe,
                new ArrayList<>(domaines.toCollection()));
    }

    public Long pk() { return pk; }

    public List<Domaine> domaines() { return iterableList(domaines); }
}

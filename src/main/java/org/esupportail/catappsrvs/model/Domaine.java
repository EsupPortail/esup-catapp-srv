package org.esupportail.catappsrvs.model;

import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;

import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle;

@EqualsAndHashCode(of = {"code", "version"}, doNotUseGetters = true)
@ToString @FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Immutable
public final class Domaine implements Versionned<Domaine> {
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
    final Libelle libelle;

    @ManyToOne
    final Domaine parent;

    @OneToMany(mappedBy = "parent")
    final java.util.List<Domaine> sousDomaines;

    @ManyToMany
    @JoinTable(name="DOMAINE_APPLICATION",
            joinColumns=
            @JoinColumn(name="domaine_pk", referencedColumnName="pk"),
            inverseJoinColumns=
            @JoinColumn(name="application_pk", referencedColumnName="pk"))
    final java.util.List<Application> applications;

    private Domaine() { // for hibernate
        version = null;
        code = null;
        libelle = null;
        parent = null;
        sousDomaines = null;
        applications = null;
    }

    private Domaine(Version version,
                    Code code,
                    Libelle libelle,
                    Domaine parent,
                    java.util.List<Domaine> sousDomaines,
                    java.util.List<Application> applications) {
        this.version = version;
        this.code = code;
        this.libelle = libelle;
        this.parent = parent;
        this.sousDomaines = sousDomaines;
        this.applications = applications;
    }

    public static Domaine domaine(Version version,
                                  Code code,
                                  Libelle libelle,
                                  Option<Domaine> parent,
                                  List<Domaine> sousDomaines,
                                  List<Application> applications) {
        return new Domaine(version, code, libelle, parent.toNull(),
                new ArrayList<>(sousDomaines.toCollection()),
                new ArrayList<>(applications.toCollection()));
    }

    public Domaine withParent(final Option<Domaine> parent) {
        return new Domaine(version, code, libelle, parent.toNull(), sousDomaines, applications);
    }

    public Domaine withSousDomaines(final List<Domaine> sousDomaines) {
        return new Domaine(version, code, libelle, parent, new ArrayList<>(sousDomaines.toCollection()), applications);
    }

    public Domaine withApplications(final List<Application> applications) {
        return new Domaine(version, code, libelle, parent, sousDomaines, new ArrayList<>(applications.toCollection()));
    }

    public Long pk() { return $pk; }

    public Version version() { return version; }

    public Code code() { return code; }

    public Libelle libelle() { return libelle; }

    public Option<Domaine> parent() { return fromNull(parent); }

    public List<Domaine> sousDomaines() { return iterableList(sousDomaines); }

    public List<Application> applications() { return iterableList(applications); }
}

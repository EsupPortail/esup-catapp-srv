package org.esupportail.catappsrvs.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle;

@EqualsAndHashCode(of = "code", doNotUseGetters = true)
@ToString @Getter @Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Audited
public final class Domaine {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
	Long pk;

    @NaturalId
    @Embedded @Column(nullable = false)
    final Code code;

    @Embedded @Column
    final Libelle libelle;

    @ManyToOne
    final Domaine parent;

    @OneToMany(mappedBy = "parent")
    final List<Domaine> sousDomaines;

    @ManyToMany
    @JoinTable(name="DOMAINE_APPLICATION",
            joinColumns=
            @JoinColumn(name="domaine_pk", referencedColumnName="pk"),
            inverseJoinColumns=
            @JoinColumn(name="application_pk", referencedColumnName="pk"))
    final List<Application> applications;

    private Domaine() { // for hibernate
        code = null;
        libelle = null;
        parent = null;
        sousDomaines = null;
        applications = null;
    }

    private Domaine(Code code,
                    Libelle libelle,
                    Domaine parent,
                    List<Domaine> sousDomaines,
                    List<Application> applications) {
        this.code = code;
        this.libelle = libelle;
        this.parent = parent;
        this.sousDomaines = sousDomaines;
        this.applications = applications;
    }

    public static Domaine domaine(Code code,
                                  Libelle libelle,
                                  Domaine parent,
                                  List<Domaine> sousDomaines,
                                  List<Application> applications) {
        return new Domaine(code, libelle, parent, sousDomaines, applications);
    }

    public Domaine withLibelle(final Libelle libelle) {
        return domaine(code, libelle, parent, sousDomaines, applications);
    }

    public Domaine withParent(final Domaine parent) {
        return domaine(code, libelle, parent, sousDomaines, applications);
    }

    public Domaine withSousDomaines(final List<Domaine> sousDomaines) {
        return domaine(code, libelle, parent, sousDomaines, applications);
    }

    public Domaine withApplications(final List<Application> applications) {
        return domaine(code, libelle, parent, sousDomaines, applications);
    }
}

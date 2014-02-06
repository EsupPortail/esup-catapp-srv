package org.esupportail.catappsrvs.model;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Collection;

@Value(staticConstructor = "domaine")
@Entity
@Audited
public class Domaine {
    @Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	Long pk;

    @NaturalId
    @Column(nullable = false)
    String code;

    @Column
    String libelle;

    @ManyToOne
    Domaine parent;

    @OneToMany(mappedBy = "parent")
    Collection<Domaine> sousDomaines;

    @ManyToMany
    @JoinTable(name="DOMAINE_APPLICATION",
            joinColumns=
            @JoinColumn(name="domaine_pk", referencedColumnName="pk"),
            inverseJoinColumns=
            @JoinColumn(name="application_pk", referencedColumnName="pk"))
    Collection<Application> applications;
}

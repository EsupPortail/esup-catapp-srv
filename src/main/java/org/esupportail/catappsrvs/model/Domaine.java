package org.esupportail.catappsrvs.model;

import lombok.Value;
import org.esupportail.catappsrvs.model.util.hibernate.ImList;
import org.hibernate.annotations.CollectionType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.Libelle;

@Value(staticConstructor = "domaine")
@Entity //@Audited
public class Domaine {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
	Long pk;

    @NaturalId
    @Embedded @Column(nullable = false)
    Code code;

    @Embedded @Column
    Libelle libelle;

    @ManyToOne
    Domaine parent;

    @OneToMany(mappedBy = "parent")
    List<Domaine> sousDomaines;

    @ManyToMany
    @JoinTable(name="DOMAINE_APPLICATION",
            joinColumns=
            @JoinColumn(name="domaine_pk", referencedColumnName="pk"),
            inverseJoinColumns=
            @JoinColumn(name="application_pk", referencedColumnName="pk"))
    @CollectionType(type = "org.esupportail.catappsrvs.model.util.hibernate.ImList")
    List<Application> applications;
}

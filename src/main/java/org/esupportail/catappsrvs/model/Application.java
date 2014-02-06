package org.esupportail.catappsrvs.model;

import lombok.Value;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Collection;

@Value(staticConstructor = "application")
@Entity
@Audited
public class Application {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long pk;

    @ManyToMany(mappedBy = "applications")
    Collection<Domaine> domaines;
}

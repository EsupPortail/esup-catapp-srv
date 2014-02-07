package org.esupportail.catappsrvs.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static org.esupportail.catappsrvs.model.CommonTypes.*;

@Entity //@Audited
@RequiredArgsConstructor(staticName = "application")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Application {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long pk;

    @NaturalId
    @Embedded @Column(nullable = false)
    Code code;

    @Embedded @Column
    Titre titre;

    @Embedded @Column
    Libelle libelle;

    @Embedded @Column
    Description description;

    @Column
    URL url;

    @ManyToMany(mappedBy = "applications")
    List<Domaine> domaines;


    private Application() {
        pk = null;
        code = null;
        titre = null;
        libelle = null;
        description = null;
        url = null;
        domaines  = null;
    }
}

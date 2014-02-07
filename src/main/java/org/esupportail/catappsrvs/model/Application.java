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
import java.net.URL;
import java.util.List;

import static org.esupportail.catappsrvs.model.CommonTypes.*;

@EqualsAndHashCode(of = "code", doNotUseGetters = true)
@ToString @Getter @Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Audited
public final class Application {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long pk;

    @NaturalId
    @Embedded @Column(nullable = false)
    final Code code;

    @Embedded @Column
    final Titre titre;

    @Embedded @Column
    final Libelle libelle;

    @Embedded @Column
    final Description description;

    @Column
    final URL url;

    @ManyToMany(mappedBy = "applications")
    final List<Domaine> domaines;

    private Application() { // for hibernate
        pk = null;
        code = null;
        titre = null;
        libelle = null;
        description = null;
        url = null;
        domaines  = null;
    }

    private Application(Code code,
                        Titre titre,
                        Libelle libelle,
                        Description description,
                        URL url,
                        List<Domaine> domaines) {
        this.code = code;
        this.titre = titre;
        this.libelle = libelle;
        this.description = description;
        this.url = url;
        this.domaines = domaines;
    }

    public static Application application(Code code,
                                          Titre titre,
                                          Libelle libelle,
                                          Description description,
                                          URL url,
                                          List<Domaine> domaines) {
        return new Application(code, titre, libelle, description, url, domaines);
    }

    public Application withDomaines(final List<Domaine> domaines) {
        return application(code, titre, libelle, description, url, domaines);
    }

    public Application withUrl(final URL url) {
        return application(code, titre, libelle, description, url, domaines);
    }

    public Application withDescription(final Description description) {
        return application(code, titre, libelle, description, url, domaines);
    }

    public Application withLibelle(final Libelle libelle) {
        return application(code, titre, libelle, description, url, domaines);
    }

    public Application withTitre(final Titre titre) {
        return application(code, titre, libelle, description, url, domaines);
    }

    public Application withCode(final Code code) {
        return application(code, titre, libelle, description, url, domaines);
    }
}

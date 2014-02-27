package org.esupportail.catappsrvs.model;

import fj.Equal;
import fj.F;
import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.net.URL;
import java.util.ArrayList;

import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.*;

@EqualsAndHashCode(of = "code", doNotUseGetters = true)
@ToString(of = "code", doNotUseGetters = true)
@Getter @Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public final class Application implements HasCode<Application> {
    public static enum Activation { Activated, Deactivated }

    @Getter(AccessLevel.NONE)
    @Id @Column(name = "pk", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
    final Long pk;

    @Wither
    @Embedded @Column(nullable = false, length = 10)
    final Code code;

    @Embedded @Column(length = 200) @Wither
    final Title title;

    @Embedded @Column(length = 200) @Wither
    final Caption caption;

    @Embedded @Column(length = 3000) @Wither
    final Description description;

    @Column(length = 1000) @Wither
    final URL url;

    @Column @Enumerated(EnumType.STRING) @Wither
    final Activation activation;

    @Embedded @Column(length = 200) @Wither
    final LdapGroup group;

    @Getter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "applications")
    final java.util.List<Domain> domains;

    private Application() { // for hibernate
        pk = null;
        code = null;
        title = null;
        caption = null;
        description = null;
        url = null;
        activation = null;
        group = null;
        domains = null;
    }

    private Application(Long pk,
                          Code code,
                          Title title,
                          Caption caption,
                          Description description,
                          URL url,
                          Activation activation,
                          LdapGroup group,
                          java.util.List<Domain> domains) {
        this.pk = pk;
        this.code = code;
        this.title = title;
        this.caption = caption;
        this.description = description;
        this.url = url;
        this.activation = activation;
        this.group = group;
        this.domains = domains;
    }

    /**
     * TODO : trop d'arguments => refactorer
     */
    public static Application application(Code code,
                                          Title title,
                                          Caption caption,
                                          Description description,
                                          URL url,
                                          Activation accessibility,
                                          LdapGroup group,
                                          List<Domain> domaines) {
        return new Application(null, code, title, caption,
                description, url, accessibility, group,
                new ArrayList<>(domaines.toCollection()));
    }

    public Application withDomains(final List<Domain> domaines) {
        return new Application(pk, code, title, caption,
                description, url, activation, group,
                new ArrayList<>(domaines.toCollection()));
    }

    public Option<Long> pk() { return fromNull(pk); }

    public List<Domain> domains() { return iterableList(domains); }
}

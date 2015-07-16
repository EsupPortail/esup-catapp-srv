package org.esupportail.catappsrvs.model;

import fj.Equal;
import fj.data.List;
import fj.data.Option;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

import javax.persistence.*;
import java.util.ArrayList;

import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static javax.persistence.CascadeType.ALL;
import static org.esupportail.catappsrvs.model.CommonTypes.Caption;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;

@EqualsAndHashCode(of = {"code"}, doNotUseGetters = true)
@ToString(of = {"code", "caption"}, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public final class Domain implements HasCode<Domain> {
    @Id @Column(name = "pk", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
	Long pk;

    @Wither
    @Embedded @Column(nullable = false, length = 10)
    Code code;

    @Embedded @Column(length = 200) @Wither
    Caption caption;

    @ManyToOne(optional = true)
    Domain parent;

    @OneToMany(mappedBy = "parent", cascade = ALL)
    java.util.List<Domain> domains;

    @ManyToMany
    @JoinTable(name="Domain_Application",
            joinColumns=
            @JoinColumn(name="domain_pk", referencedColumnName="pk"),
            inverseJoinColumns=
            @JoinColumn(name="application_pk", referencedColumnName="pk"))
    java.util.List<Application> applications;

    @SuppressWarnings("UnusedDeclaration")
    private Domain() { // for hibernate
        pk = null;
        code = null;
        caption = null;
        parent = null;
        domains = null;
        applications = null;
    }

    private Domain(Long pk,
                   Code code,
                   Caption caption,
                   Domain parent,
                   java.util.List<Domain> domains,
                   java.util.List<Application> applications) {
        this.pk = pk;
        this.code = code;
        this.caption = caption;
        this.parent = parent;
        this.domains = domains;
        this.applications = applications;
    }

    public static Domain of(Code code,
                            Caption caption,
                            Option<Domain> parent,
                            List<Domain> sousDomaines,
                            List<Application> applications) {
        return new Domain(null, code, caption, parent.toNull(),
                new ArrayList<>(sousDomaines.toCollection()),
                new ArrayList<>(applications.toCollection()));
    }

    public Domain withParent(final Option<Domain> parent) {
        return new Domain(pk, code, caption, parent.toNull(), domains, applications);
    }

    public Domain withDomains(final List<Domain> sousDomaines) {
        return new Domain(pk, code, caption, parent, new ArrayList<>(sousDomaines.toCollection()), applications);
    }

    public Domain withApplications(final List<Application> applications) {
        return new Domain(pk, code, caption, parent, domains, new ArrayList<>(applications.toCollection()));
    }

    public Option<Long> pk() { return fromNull(pk); }

    public Code code() { return code; }

    public Caption caption() { return caption; }

    public Option<Domain> parent() { return fromNull(parent); }

    public List<Domain> domains() { return iterableList(domains); }

    public List<Application> applications() { return iterableList(applications); }

    public static final Equal<Domain> eq = Code.eq.contramap(Domain::code);
 }

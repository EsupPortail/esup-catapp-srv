package org.esupportail.catappsrvs.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

public interface Versionned<T extends Versionned<T>> {

    Version version();

    T withVersion(Version version);

    @Embeddable
    @EqualsAndHashCode
    @ToString
    @RequiredArgsConstructor(staticName = "version")
    final class Version implements Comparable<Version> {
        @Column(name = "version") private final Integer value;
        private Version() { value = null; } // for hibernate
        public Integer value() { return value; }

        public Version plus(Integer i) { return version(value + i); }
        @Override
        public int compareTo(Version version) {
            return value.compareTo(version.value);
        }
    }
}

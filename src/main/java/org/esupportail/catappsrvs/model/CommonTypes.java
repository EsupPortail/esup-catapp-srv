package org.esupportail.catappsrvs.model;

import fj.Equal;
import fj.F;
import fj.Show;
import fr.ur.data.Read;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static fj.Bottom.error;
import static fj.Function.compose;
import static fj.data.Option.fromString;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
public final class CommonTypes {
    private CommonTypes() { throw error("Unsupported"); }

    @Embeddable @Value
    @RequiredArgsConstructor(staticName = "of")
    public static class Code {
        @Column(name = "code") String value;
        private Code() { value = null; }
        public static final Read<Code> read = repr -> fromString(repr).map(Code::of);
        public static final Show<Code> show = Show.stringShow.contramap(Code::value);
        public static final Equal<Code> eq = Equal.stringEqual.contramap(Code::value);
    }

    @Embeddable @Value
    @RequiredArgsConstructor(staticName = "of")
    public static class Caption {
        @Column(name = "caption") String value;
        private Caption() { value = null; }
        public static final Read<Caption> read = repr -> fromString(repr).map(Caption::of);
        public static final Show<Caption> show = Show.stringShow.contramap(Caption::value);
        public static final Equal<Caption> eq = Equal.stringEqual.contramap(Caption::value);
    }

    @Embeddable @Value
    @RequiredArgsConstructor(staticName = "of")
    public static class Title {
        @Column(name = "title") String value;
        private Title() { value = null; }
        public static final Read<Title> read = repr -> fromString(repr).map(Title::of);
        public static final Show<Title> show = Show.stringShow.contramap(Title::value);
        public static final Equal<Title> eq = Equal.stringEqual.contramap(Title::value);
    }

    @Embeddable @Value
    @RequiredArgsConstructor(staticName = "of")
    public static class Description {
        @Column(name = "description", length = 5000) String value;
        private Description() { value = null; }
        public static final Read<Description> read = repr -> fromString(repr).map(Description::of);
        public static final Show<Description> show = Show.stringShow.contramap(Description::value);
        public static final Equal<Description> eq = Equal.stringEqual.contramap(Description::value);
    }

    @Embeddable @Value
    @RequiredArgsConstructor(staticName = "of")
    public static class LdapGroup {
        @Column(name = "ldapgroup") String value;
        private LdapGroup() { value = null; } // hibernate
        private static final F<LdapGroup, String> lowerCase =
            compose(String::toLowerCase, LdapGroup::value);
        public static final Read<LdapGroup> read = repr -> fromString(repr).map(LdapGroup::of);
        public static final Show<LdapGroup> show = Show.stringShow.contramap(lowerCase);
        public static final Equal<LdapGroup> eq = Equal.stringEqual.contramap(lowerCase);
    }
}

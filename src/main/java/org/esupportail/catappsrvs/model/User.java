package org.esupportail.catappsrvs.model;

import fj.Equal;
import fj.Show;
import fr.ur.data.Read;
import lombok.Value;
import lombok.experimental.Accessors;

import static fj.data.Option.fromString;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public class User {
    Uid uid;

    public static final Equal<User> eq = Uid.eq.contramap(User::uid);

    @Value(staticConstructor = "of")
    public static class Uid {
        String value;
        public static final Read<Uid> read = repr -> fromString(repr).map(Uid::of);
        public static final Show<Uid> show = Show.stringShow.contramap(Uid::value);
        public static final Equal<Uid> eq = Equal.stringEqual.contramap(Uid::value);

    }
}

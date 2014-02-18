package org.esupportail.catappsrvs.model;

import lombok.Getter;
import lombok.Value;

import static lombok.AccessLevel.NONE;

@Value(staticConstructor = "user")
@Getter(NONE)
public class User {
    public Uid uid;

    @Value(staticConstructor = "uid")
    @Getter(NONE)
    public static class Uid { public String value; }
}

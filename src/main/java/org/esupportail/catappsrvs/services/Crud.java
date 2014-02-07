package org.esupportail.catappsrvs.services;

import fj.Effect;
import fj.data.Either;

public interface Crud<T> {
    Either<Exception, Effect<T>> create(T t);

    Either<Exception, T> read();

    Either<Exception, Effect<T>> delete(T t);

}

package org.esupportail.catappsrvs.services;

import fj.Unit;
import fj.data.Either;
import fj.data.List;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;

public interface ICrud<T> {

    Either<Exception, Boolean> exists(Code code);

    Either<Exception, T> create(T t);

    Either<Exception, T> read(Code code);

    Either<Exception, List<T>> list();

    Either<Exception, T> update(T t);

    Either<Exception, Unit> delete(Code code);
}

package org.esupportail.catappsrvs.services;

import fj.Unit;
import fj.data.Either;
import fj.data.Option;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public interface ICrud<T> {
    Either<Exception, T> create(T t);

    Either<Exception, T> read(Code code, Option<Version> version);

    Either<Exception, T> update(T t);

    Either<Exception, Unit> delete(Code code);
}

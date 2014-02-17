package org.esupportail.catappsrvs.dao;

import fj.Unit;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public interface ICrudDao<T> {
    Either<Exception, Boolean> exists(Code code);

    Either<Exception, T> create(T t);

    Either<Exception, T> read(Code code, Option<Version> version);

    Either<Exception, List<T>> list();

    Either<Exception, T> update(T t);

    Either<Exception, Unit> delete(Code code);
}

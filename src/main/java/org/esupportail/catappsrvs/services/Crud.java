package org.esupportail.catappsrvs.services;

import fj.Unit;
import fj.data.Either;
import fj.data.Option;
import org.esupportail.catappsrvs.dao.ICrudDao;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;

abstract class Crud<T, D extends ICrudDao<T>> implements ICrud<T> {
    protected final D dao;

    protected Crud(D dao) {
        this.dao = dao;
    }

    @Override
    public final Either<Exception, T> create(T t) {
        return dao.create(t);
    }

    @Override
    public final Either<Exception, T> read(Code code, Option<Version> version) {
        return dao.read(code, version);
    }

    @Override
    public final Either<Exception, T> update(T t) {
        return dao.update(t);
    }

    @Override
    public final Either<Exception, Unit> delete(Code code) {
        return dao.delete(code);
    }
}

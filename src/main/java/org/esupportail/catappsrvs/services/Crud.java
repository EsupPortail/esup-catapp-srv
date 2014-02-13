package org.esupportail.catappsrvs.services;

import fj.Unit;
import fj.data.Either;
import fj.data.Option;
import org.esupportail.catappsrvs.dao.ICrudDao;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static fj.data.Either.left;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;

abstract class Crud<T, D extends ICrudDao<T>> implements ICrud<T> {
    protected final D dao;
    protected final PlatformTransactionManager txManager;

    private final TransactionTemplate writeTemplate, readTemplate;

    protected Crud(D dao, PlatformTransactionManager txManager) {
        this.dao = dao;
        this.txManager = txManager;
        writeTemplate = new TransactionTemplate(txManager);
        readTemplate = new TransactionTemplate(txManager) {{
            setReadOnly(true);
            setPropagationBehavior(PROPAGATION_SUPPORTS);
        }};
    }

    @Override
    public final Either<Exception, T> create(final T t) {
        return inTransaction(writeTemplate, new TransactionCallback<Either<Exception, T>>() {
            public Either<Exception, T> doInTransaction(TransactionStatus status) {
                return dao.create(t);
            }
        });
    }

    @Override
    public final Either<Exception, T> read(final Code code, final Option<Version> version) {
       return inTransaction(readTemplate, new TransactionCallback<Either<Exception, T>>() {
           public Either<Exception, T> doInTransaction(TransactionStatus status) {
               return dao.read(code, version);
           }
       });
    }

    @Override
    public final Either<Exception, T> update(final T t) {
        return inTransaction(writeTemplate, new TransactionCallback<Either<Exception, T>>() {
            public Either<Exception, T> doInTransaction(TransactionStatus status) {
                return dao.update(t);
            }
        });
    }

    @Override
    public final Either<Exception, Unit> delete(final Code code) {
        return inTransaction(writeTemplate, new TransactionCallback<Either<Exception, Unit>>() {
            public Either<Exception, Unit> doInTransaction(TransactionStatus status) {
                return dao.delete(code);
            }
        });
    }

    protected final <U> Either<Exception, U> inTransaction(final TransactionTemplate template,
                                                           final TransactionCallback<Either<Exception, U>> action) {
        try {
            return template.execute(action);
        } catch (Exception e) {
            return left(e);
        }
    }

}

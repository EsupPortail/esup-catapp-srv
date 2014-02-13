package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.path.PathBuilder;

import fj.F;
import fj.P1;
import fj.Unit;
import fj.data.Either;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Versionned;

import javax.persistence.EntityManager;

import java.util.NoSuchElementException;

import static com.mysema.query.jpa.JPAExpressions.max;
import static fj.Unit.unit;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;

abstract class CrudDao<T extends Versionned<T>> implements ICrudDao<T> {
    protected final EntityManager entityManager;
    protected final EntityPath<T> ent;
    protected final PathBuilder<T> tPath;
    protected final Expression<Version> lastVersion;
    protected final PathBuilder<Code> codePath;
    protected final PathBuilder<Version> versionPath;

    protected CrudDao(EntityManager entityManager, EntityPath<T> ent, Class<T> clazz) {
        this.entityManager = entityManager;
        this.ent = ent;
        tPath = new PathBuilder<>(clazz, ent.getMetadata());
        codePath = tPath.get("code", Code.class);
        versionPath = tPath.get("version", Version.class);
        lastVersion = max(tPath.getSet("version", Version.class));
    }

    protected abstract Either<Exception, T> prepareEntity(T t);
    protected abstract Either<Exception, T> refineEntity(T t);

    @Override
    public final Either<Exception, T> create(T t) {
        return prepareEntity(t)
                .right()
                .bind(new F<T, Either<Exception, T>>() {
                    public Either<Exception, T> f(T t) {
                        return doCreate(t, version(1));
                    }
                });
    }

    @Override
    public final Either<Exception, T> read(final Code code, final Option<Version> version) {
        final Expression<Version> realVersion = version
                .map(new F<Version, Expression<Version>>() {
                    public Expression<Version> f(Version version) {
                        return Expressions.constant(version);
                    }
                })
                .orSome(new JPASubQuery().from(ent).unique(lastVersion));
        try {
            final JPAQuery query =
                    from(ent).where(codePath.eq(code).and(versionPath.eq(realVersion)));
            return fromNull(query.uniqueResult(ent))
                    .toEither(new P1<Exception>() {
                        public Exception _1() {
                            return new NoSuchElementException(
                                    "aucune entité trouvée pour la paire clé-version : " + code + "-" + version);
                        }})
                    .right()
                    .bind(new F<T, Either<Exception, T>>() {
                        public Either<Exception, T> f(T t) {
                            return refineEntity(t);
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> update(final T t) {
        return searchLastVersion()
                .right()
                .bind(new F<Version, Either<Exception, T>>() {
                    public Either<Exception, T> f(final Version version) {
                        return prepareEntity(t)
                                .right()
                                .bind(new F<T, Either<Exception, T>>() {
                                    public Either<Exception, T> f(T completeT) {
                                        return doCreate(completeT, version.plus(1));
                                    }
                                });
                    }
                });
    }

    @Override
    public final Either<Exception, Unit> delete(final Code code) {
        try {
            new JPADeleteClause(entityManager, ent)
                    .where(codePath.eq(code))
//                            .and(versionPath.eq(new JPASubQuery()
//                                    .from(ent).unique(lastVersion))))
                    .execute();
            return right(unit());
        } catch (Exception e) {
            return left(e);
        }
    }

    @SafeVarargs
    protected final JPAQuery from(EntityPath<T>... path) {
        return new JPAQuery(entityManager).from(path);
    }

    protected final Either<Exception, Version> searchLastVersion() {
        try {
            return fromNull(from(ent).uniqueResult(lastVersion))
                    .toEither(new P1<Exception>() {
                        public Exception _1() {
                            return new Exception("La dernière version de l'entité n'a pu être récupérée");
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

    private Either<Exception, T> doCreate(T t, Version v) {
        try {
            final T versionnedT = t.withVersion(v);
            entityManager.persist(versionnedT);
            return right(versionnedT);
        } catch (Exception e) {
            return left(e);
        }
    }
}

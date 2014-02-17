package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import fj.F;
import fj.P1;
import fj.Unit;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.HasCode;
import org.esupportail.catappsrvs.model.Versionned;

import javax.persistence.EntityManager;
import java.util.NoSuchElementException;

import static com.mysema.query.jpa.JPAExpressions.max;
import static fj.Unit.unit;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;
import static org.esupportail.catappsrvs.model.Versionned.Version.*;

abstract class CrudDao<T extends Versionned<T> & HasCode<T>> implements ICrudDao<T> {
    private final EntityPath<T> ent;
    private final Class<T> clazz;
    private final PathBuilder<Code> codePath;
    private final PathBuilder<Version> versionPath;

    protected final EntityManager entityManager;
    protected final PathBuilderFactory pathBuilderFactory = new PathBuilderFactory();

    protected CrudDao(EntityManager entityManager, EntityPath<T> ent, Class<T> clazz) {
        this.entityManager = entityManager;
        this.ent = ent;
        this.clazz = clazz;
        final PathBuilder<T> tPath = pathBuilderFactory.create(clazz);
        codePath = tPath.get("code", Code.class);
        versionPath = tPath.get("version", Version.class);
    }

    protected abstract Either<Exception, T> prepare(T t);
    protected abstract Either<Exception, T> persist(T t);
    protected abstract Either<Exception, T> refine(T t);

    @Override
    public Either<Exception, Boolean> exists(Code code) {
        try {
            return right(from(ent).where(codePath.eq(code)).exists());
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> create(T t) {
        return prepare(t)
                .right()
                .bind(new F<T, Either<Exception, T>>() {
                    public Either<Exception, T> f(T readyT) {
                        return persist(readyT.withVersion(version(1)));
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
                .orSome(new JPASubQuery().from(ent).where(codePath.eq(code)).unique(lastVersion(clazz)));
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
                            return refine(t);
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public Either<Exception, List<T>> list() {
        final PathBuilder<T> subPath = pathBuilderFactory.create(clazz);
        final PathBuilder<Code> subCodePath = subPath.get("code", Code.class);
        try {
            return Either.<Exception, List<T>>right(
                    iterableList(from(ent)
                            .where(versionPath.eq(new JPASubQuery()
                                    .from(ent).where(codePath.eq(subCodePath)).unique(lastVersion(clazz))))
                            .list(ent)))
                    .right()
                    .bind(new F<List<T>, Either<Exception, List<T>>>() {
                        public Either<Exception, List<T>> f(List<T> ts) {
                            return Either.sequenceRight(ts.map(new F<T, Either<Exception, T>>() {
                                public Either<Exception, T> f(T t) {
                                    return refine(t);
                                }
                            }));
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> update(final T t) {
        return searchLastVersion(t)
                .right()
                .bind(new F<Version, Either<Exception, T>>() {
                    public Either<Exception, T> f(final Version version) {
                        return prepare(t)
                                .right()
                                .bind(new F<T, Either<Exception, T>>() {
                                    public Either<Exception, T> f(T readyT) {
                                        return persist(readyT.withVersion(version.plus(1)));
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

    protected final <U> Expression<Version> lastVersion(Class<U> clazz) {
        return max(pathBuilderFactory.create(clazz).getSet("version", Version.class));
    }

    protected final Either<Exception, Version> searchLastVersion(T t) {
        try {
            return fromNull(from(ent).where(codePath.eq(t.code())).uniqueResult(lastVersion(clazz)))
                    .toEither(new P1<Exception>() {
                        public Exception _1() {
                            return new Exception("La dernière version de l'entité n'a pu être récupérée");
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

}

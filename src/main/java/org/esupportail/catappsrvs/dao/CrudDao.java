package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import fj.F;
import fj.P1;
import fj.Unit;
import fj.data.Either;
import fj.data.List;
import org.esupportail.catappsrvs.model.HasCode;

import javax.persistence.EntityManager;
import java.util.NoSuchElementException;

import static fj.Unit.unit;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;

abstract class CrudDao<T extends HasCode<T>> implements ICrudDao<T> {
    private final EntityPath<T> ent;
    private final Class<T> clazz;
    private final PathBuilder<Code> codePath;

    protected final EntityManager entityManager;
    protected final PathBuilderFactory pathBuilderFactory = new PathBuilderFactory();
    private final PathBuilder<T> tPath;

    protected CrudDao(EntityManager entityManager, EntityPath<T> ent, Class<T> clazz) {
        this.entityManager = entityManager;
        this.ent = ent;
        this.clazz = clazz;
        tPath = pathBuilderFactory.create(clazz);
        codePath = tPath.get("code", Code.class);
    }

    abstract Either<Exception, T> prepare(T t);
    abstract Either<Exception, T> refine(T t);

    @Override
    public final Either<Exception, Boolean> exists(Code code) {
        try {
            return right(from(ent).where(codePath.eq(code)).exists());
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> read(final Code code) {
        try {
            return fromNull(from(ent).where(codePath.eq(code)).uniqueResult(ent))
                    .toEither(new P1<Exception>() {
                        public Exception _1() {
                            return new NoSuchElementException("aucune entité trouvée de code : " + code);
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
    public final Either<Exception, List<T>> list() {
        try {
            return Either.<Exception, List<T>>right(
                    iterableList(from(ent).list(ent)))
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
    public final Either<Exception, Unit> delete(final Code code) {
        try {
            new JPADeleteClause(entityManager, ent)
                    .where(codePath.eq(code))
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
}

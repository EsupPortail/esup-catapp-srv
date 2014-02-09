package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.query.SimpleSubQuery;
import fj.F;
import fj.Unit;
import fj.data.Either;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Versionned;

import javax.persistence.EntityManager;

import static com.mysema.query.jpa.JPAExpressions.max;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public abstract class CrudDao<T extends Versionned<T>> implements ICrudDao<T> {
    protected final EntityManager entityManager;
    protected final EntityPath<T> ent;
    protected final PathBuilder<T> tPath;
    protected final SimpleSubQuery<Version> lastVersion;

    private PathBuilder<Code> codePath;
    private PathBuilder<Version> versionPath;

    protected CrudDao(EntityManager entityManager, EntityPath<T> ent, Class<T> clazz) {
        this.entityManager = entityManager;
        this.ent = ent;
        tPath = new PathBuilder<>(clazz, ent.getMetadata());
        codePath = tPath.get("code", Code.class);
        versionPath = tPath.get("version", Version.class);
        lastVersion = new JPASubQuery()
                .from(new EntityPathBase<>(clazz, "subEnt"))
                .unique((Expression<Version>) max(tPath.getSet("version", Version.class)));
    }

    @Override
    public final Either<Exception, T> create(T t) {
        try {
            entityManager.persist(t);
            return right(t);
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> read(final Code code, Option<Version> version) {
        final Expression<Version> realVersion = version
                .map(new F<Version, Expression<Version>>() {
                    public Expression<Version> f(Version version) {
                        return Expressions.constant(version);
                    }
                })
                .orSome(lastVersion);
        try {
            final JPAQuery query =
                    from(ent).where(codePath.eq(code).and(versionPath.eq(realVersion)));
            return right(query.uniqueResult(ent));
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public final Either<Exception, T> update(final T t) {
        return searchLastVersion()
                .right()
                .bind(new F<Version, Either<Exception, T>>() {
                    public Either<Exception, T> f(Version version) {
                        return create(t.withVersion(version));
                    }
                });
    }

    @Override
    public final Either<Exception, Unit> delete(T t) {
        return null;
    }

    @SafeVarargs
    protected final JPAQuery from(EntityPath<T>... path) {
        return new JPAQuery(entityManager).from(path);
    }

    private Either<Exception, Version> searchLastVersion() {
        try {
            return right(from(ent)
                    .where(versionPath.eq(lastVersion))
                    .uniqueResult(versionPath));
        } catch (Exception e) {
            return left(e);
        }
    }
}

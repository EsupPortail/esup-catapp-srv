package org.esupportail.catappsrvs.model.util.hibernate;

import com.google.common.collect.ImmutableList;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ImList implements UserCollectionType {
    @Override
    public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentList(session);
    }

    @Override
    public PersistentCollection wrap(SessionImplementor session, Object collection) {
        return new PersistentList(session, (List) collection);
    }

    @Override
    public Iterator getElementsIterator(Object collection) {
        return ((List) collection).iterator();
    }

    @Override
    public boolean contains(Object collection, Object entity) {
        return ((List) collection).contains(entity);
    }

    @Override
    public Object indexOf(Object collection, Object entity) {
        return ((List) collection).indexOf(entity);
    }

    @Override
    public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
        return new ImmutableList.Builder<>().addAll((List) original).build();
    }

    @Override
    public Object instantiate(int anticipatedSize) {
        return new ImmutableList.Builder<>().build();
    }
}

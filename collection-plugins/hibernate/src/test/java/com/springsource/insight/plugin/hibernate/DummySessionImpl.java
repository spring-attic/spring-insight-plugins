/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;

public class DummySessionImpl implements Session {
    private static final long serialVersionUID = 7080645949615301278L;
    protected static final int DUMMY_ENTITY_COUNT = -1;
    protected static final int DUMMY_COLLECTION_COUNT = -1;
    protected static final FlushMode DUMMY_FLUSH_MODE = FlushMode.MANUAL;

    public DummySessionImpl() {
        super();
    }

    public Transaction beginTransaction() throws HibernateException {
        return null;
    }

    public void cancelQuery() throws HibernateException {
        // ignored
    }

    public void clear() {
        // ignored
    }

    public Connection close() throws HibernateException {
        return null;
    }

    public Connection connection() throws HibernateException {
        return null;
    }

    public boolean contains(Object arg0) {
        return false;
    }

    @SuppressWarnings("rawtypes")
    public Criteria createCriteria(Class arg0) {
        return null;
    }

    public Criteria createCriteria(String arg0) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Criteria createCriteria(Class arg0, String arg1) {
        return null;
    }

    public Criteria createCriteria(String arg0, String arg1) {
        return null;
    }

    public Query createFilter(Object arg0, String arg1) throws HibernateException {
        return null;
    }

    public Query createQuery(String arg0) throws HibernateException {
        return null;
    }

    public SQLQuery createSQLQuery(String arg0) throws HibernateException {
        return null;
    }

    public void delete(Object arg0) throws HibernateException {
        // ignored
    }

    public void delete(String arg0, Object arg1) throws HibernateException {
        // ignored
    }

    public void disableFilter(String arg0) {
        // ignored
    }

    public Connection disconnect() throws HibernateException {
        return null;
    }

    public Filter enableFilter(String arg0) {
        return null;
    }

    public void evict(Object arg0) throws HibernateException {
        // ignored
    }

    public void flush() throws HibernateException {
        // ignored
    }

    @SuppressWarnings("rawtypes")
    public Object get(Class arg0, Serializable arg1) throws HibernateException {
        return null;
    }

    public Object get(String arg0, Serializable arg1) throws HibernateException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Object get(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        return null;
    }

    public Object get(String arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        return null;
    }

    public CacheMode getCacheMode() {
        return null;
    }

    public LockMode getCurrentLockMode(Object arg0) throws HibernateException {
        return null;
    }

    public Filter getEnabledFilter(String arg0) {
        return null;
    }

    public EntityMode getEntityMode() {
        return null;
    }

    public String getEntityName(Object arg0) throws HibernateException {
        return null;
    }

    public FlushMode getFlushMode() {
        return DUMMY_FLUSH_MODE;
    }

    public Serializable getIdentifier(Object arg0) throws HibernateException {
        return null;
    }

    public Query getNamedQuery(String arg0) throws HibernateException {
        return null;
    }

    public Session getSession(EntityMode arg0) {
        return null;
    }

    public SessionFactory getSessionFactory() {
        return null;
    }

    public SessionStatistics getStatistics() {
        return new SessionStatistics() {
            public int getCollectionCount() {
                return DUMMY_COLLECTION_COUNT;
            }

            @SuppressWarnings("rawtypes")
            public Set getCollectionKeys() {
                return null;
            }

            public int getEntityCount() {
                return DUMMY_ENTITY_COUNT;
            }

            @SuppressWarnings("rawtypes")
            public Set getEntityKeys() {
                return null;
            }
        };
    }

    public Transaction getTransaction() {
        return null;
    }

    public boolean isConnected() {
        return false;
    }

    public boolean isDirty() throws HibernateException {
        return false;
    }

    public boolean isOpen() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    public Object load(Class arg0, Serializable arg1) throws HibernateException {
        return null;
    }

    public Object load(String arg0, Serializable arg1) throws HibernateException {
        return null;
    }

    public void load(Object arg0, Serializable arg1) throws HibernateException {
        // ignored
    }

    @SuppressWarnings("rawtypes")
    public Object load(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        return null;
    }

    public Object load(String arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        return null;
    }

    public void lock(Object arg0, LockMode arg1) throws HibernateException {
        // ignored
    }

    public void lock(String arg0, Object arg1, LockMode arg2) throws HibernateException {
        // ignored
    }

    public Object merge(Object arg0) throws HibernateException {
        return null;
    }

    public Object merge(String arg0, Object arg1) throws HibernateException {
        return null;
    }

    public void persist(Object arg0) throws HibernateException {
        // ignored
    }

    public void persist(String arg0, Object arg1) throws HibernateException {
        // ignored
    }

    public void reconnect() throws HibernateException {
        // ignored
    }

    public void reconnect(Connection arg0) throws HibernateException {
        // ignored
    }

    public void refresh(Object arg0) throws HibernateException {
        // ignored
    }

    public void refresh(Object arg0, LockMode arg1) throws HibernateException {
        // ignored
    }

    public void replicate(Object arg0, ReplicationMode arg1) throws HibernateException {
        // ignored
    }

    public void replicate(String arg0, Object arg1, ReplicationMode arg2) throws HibernateException {
        // ignored
    }

    public Serializable save(Object arg0) throws HibernateException {
        return null;
    }

    public Serializable save(String arg0, Object arg1) throws HibernateException {
        return null;
    }

    public void saveOrUpdate(Object arg0) throws HibernateException {
        // ignored
    }

    public void saveOrUpdate(String arg0, Object arg1) throws HibernateException {
        // ignored
    }

    public void setCacheMode(CacheMode arg0) {
        // ignored
    }

    public void setFlushMode(FlushMode arg0) {
        // ignored
    }

    public void setReadOnly(Object arg0, boolean arg1) {
        // ignored
    }

    public void update(Object arg0) throws HibernateException {
        // ignored
    }

    public void update(String arg0, Object arg1) throws HibernateException {
        // ignored
    }

    public boolean isDefaultReadOnly() {
        return false;
    }

    public void setDefaultReadOnly(boolean readOnly) {
        // ignored
    }

    @SuppressWarnings("rawtypes")
    public Object load(Class theClass, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        return null;
    }

    public Object load(String entityName, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        return null;
    }

    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return null;
    }

    public void refresh(Object object, LockOptions lockOptions)
            throws HibernateException {
        // ignored
    }

    @SuppressWarnings("rawtypes")
    public Object get(Class clazz, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        return null;
    }

    public Object get(String entityName, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        return null;
    }

    public boolean isReadOnly(Object entityOrProxy) {
        return false;
    }

    public void doWork(Work work) throws HibernateException {
        // ignored
    }

    public boolean isFetchProfileEnabled(String name)
            throws UnknownProfileException {
        return false;
    }

    public void enableFetchProfile(String name) throws UnknownProfileException {
        // ignored
    }

    public void disableFetchProfile(String name) throws UnknownProfileException {
        // ignored
    }

    public TypeHelper getTypeHelper() {
        return null;
    }

    public LobHelper getLobHelper() {
        return null;
    }

}

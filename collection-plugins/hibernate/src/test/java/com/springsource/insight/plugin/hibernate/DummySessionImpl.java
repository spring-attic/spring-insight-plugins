/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

    protected static final int DUMMY_ENTITY_COUNT = -1;
    protected static final int DUMMY_COLLECTION_COUNT = -1;
    protected static final FlushMode DUMMY_FLUSH_MODE = FlushMode.MANUAL;

    public Transaction beginTransaction() throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void cancelQuery() throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void clear() {
        // TODO Auto-generated method stub
        
    }

    public Connection close() throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Connection connection() throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean contains(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public Criteria createCriteria(Class arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Criteria createCriteria(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Criteria createCriteria(Class arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Criteria createCriteria(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Query createFilter(Object arg0, String arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Query createQuery(String arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLQuery createSQLQuery(String arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void delete(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void delete(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void disableFilter(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public Connection disconnect() throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Filter enableFilter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void evict(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void flush() throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public Object get(Class arg0, Serializable arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(String arg0, Serializable arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(String arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public CacheMode getCacheMode() {
        // TODO Auto-generated method stub
        return null;
    }

    public LockMode getCurrentLockMode(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Filter getEnabledFilter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public EntityMode getEntityMode() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEntityName(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public FlushMode getFlushMode() {
        return DUMMY_FLUSH_MODE;
    }

    public Serializable getIdentifier(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Query getNamedQuery(String arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Session getSession(EntityMode arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public SessionFactory getSessionFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    public SessionStatistics getStatistics() {
        // TODO Auto-generated method stub
        SessionStatistics stats = new SessionStatistics() {

            public int getCollectionCount() {
                // TODO Auto-generated method stub
                return DUMMY_COLLECTION_COUNT;
            }

            public Set getCollectionKeys() {
                // TODO Auto-generated method stub
                return null;
            }

            public int getEntityCount() {
                // TODO Auto-generated method stub
                return DUMMY_ENTITY_COUNT;
            }

            public Set getEntityKeys() {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
        return stats;
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDirty() throws HibernateException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    public Object load(Class arg0, Serializable arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object load(String arg0, Serializable arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void load(Object arg0, Serializable arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public Object load(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object load(String arg0, Serializable arg1, LockMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void lock(Object arg0, LockMode arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void lock(String arg0, Object arg1, LockMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public Object merge(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object merge(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void persist(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void persist(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void reconnect() throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void reconnect(Connection arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void refresh(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void refresh(Object arg0, LockMode arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void replicate(Object arg0, ReplicationMode arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void replicate(String arg0, Object arg1, ReplicationMode arg2) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public Serializable save(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Serializable save(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public void saveOrUpdate(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void saveOrUpdate(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void setCacheMode(CacheMode arg0) {
        // TODO Auto-generated method stub
        
    }

    public void setFlushMode(FlushMode arg0) {
        // TODO Auto-generated method stub
        
    }

    public void setReadOnly(Object arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    public void update(Object arg0) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public void update(String arg0, Object arg1) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public boolean isDefaultReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setDefaultReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub
        
    }

    public Object load(Class theClass, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object load(String entityName, Serializable id,
            LockOptions lockOptions) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public LockRequest buildLockRequest(LockOptions lockOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    public void refresh(Object object, LockOptions lockOptions)
            throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public Object get(Class clazz, Serializable id, LockOptions lockOptions)
            throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(String entityName, Serializable id,
            LockOptions lockOptions) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isReadOnly(Object entityOrProxy) {
        // TODO Auto-generated method stub
        return false;
    }

    public void doWork(Work work) throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    public boolean isFetchProfileEnabled(String name)
            throws UnknownProfileException {
        // TODO Auto-generated method stub
        return false;
    }

    public void enableFetchProfile(String name) throws UnknownProfileException {
        // TODO Auto-generated method stub
        
    }

    public void disableFetchProfile(String name) throws UnknownProfileException {
        // TODO Auto-generated method stub
        
    }

    public TypeHelper getTypeHelper() {
        // TODO Auto-generated method stub
        return null;
    }

    public LobHelper getLobHelper() {
        // TODO Auto-generated method stub
        return null;
    }

}

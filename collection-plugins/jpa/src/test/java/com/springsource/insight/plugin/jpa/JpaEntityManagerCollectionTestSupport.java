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

package com.springsource.insight.plugin.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class JpaEntityManagerCollectionTestSupport
        extends OperationCollectionAspectTestSupport {
    protected final EntityManager   entityManager=new TestEntityManager();

    protected JpaEntityManagerCollectionTestSupport() {
        super();
    }

    protected Operation assertManagerOperation (String testName, String action, String opGroup) {
        return assertManagerOperation(getLastEntered(), testName, action, opGroup);
    }

    static Operation assertManagerOperation (Operation op, String testName, String action, String opGroup) {
        assertNotNull(testName + ": No operation extracted", op);
        assertEquals(testName + ": Mismatched operation type", JpaDefinitions.ENTITY_MGR, op.getType());
        assertEquals(testName + ": Mismatched group", opGroup, op.get(JpaDefinitions.GROUP_ATTR, String.class));
        assertEquals(testName + ": Mismatched action", action, op.get(JpaDefinitions.ACTION_ATTR, String.class));
        return op;
    }

    static class TestEntityManager implements EntityManager {
        private FlushModeType       flushMode=FlushModeType.AUTO;
        private final Set<Object>   data=Collections.synchronizedSet(new HashSet<Object>());
        private boolean             open=true;
        protected final Logger logger=Logger.getLogger(getClass().getName());
        public TestEntityManager() {
            super();
        }

        public void persist(Object entity) {
            boolean result=data.add(entity);
            logger.info("persist(" + entity + "): " + result);
        }

        public <T> T merge(T entity) {
            boolean result=data.add(entity);
            logger.info("merge(" + entity + "): " + result);
            return entity;
        }

        public void remove(Object entity) {
            boolean result=data.remove(entity);
            logger.info("remove(" + entity + "): " + result);
        }

        public <T> T find(Class<T> entityClass, Object primaryKey) {
            return null;
        }

        public <T> T getReference(Class<T> entityClass, Object primaryKey) {
            return null;
        }

        public void flush() {
            logger.info("flush()");
        }

        public void setFlushMode(@SuppressWarnings("hiding") FlushModeType flushMode) {
            this.flushMode = flushMode;
        }

        public FlushModeType getFlushMode() {
            return flushMode;
        }

        public void lock(Object entity, LockModeType lockMode) {
            logger.info("lock(" + entity + ")[" + lockMode + "]");
        }

        public void refresh(Object entity) {
            logger.info("refresh(" + entity + ")");
        }

        public void clear() {
            logger.info("clear(): " + data.size());
            data.clear();
        }

        public boolean contains(Object entity) {
            return data.contains(entity);
        }

        public Query createQuery(String qlString) {
            return null;
        }

        public Query createNamedQuery(String name) {
            return null;
        }

        public Query createNativeQuery(String sqlString) {
            return null;
        }

        @SuppressWarnings("rawtypes")
		public Query createNativeQuery(String sqlString, Class resultClass) {
            return null;
        }

        public Query createNativeQuery(String sqlString, String resultSetMapping) {
            return null;
        }

        public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
            // TODO Auto-generated method stub
            return null;
        }

        public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            // TODO Auto-generated method stub
        }

        public void refresh(Object entity, Map<String, Object> properties) {
            // TODO Auto-generated method stub
        }

        public void refresh(Object entity, LockModeType lockMode) {
            // TODO Auto-generated method stub
        }

        public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            // TODO Auto-generated method stub
        }

        public void detach(Object entity) {
            // TODO Auto-generated method stub
        }

        public LockModeType getLockMode(Object entity) {
            // TODO Auto-generated method stub
            return null;
        }

        public void setProperty(String propertyName, Object value) {
            // TODO Auto-generated method stub
        }

        public Map<String, Object> getProperties() {
            // TODO Auto-generated method stub
            return Collections.emptyMap();
        }

        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> TypedQuery<T> createNamedQuery(String name,Class<T> resultClass) {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> T unwrap(Class<T> cls) {
            // TODO Auto-generated method stub
            return null;
        }

        public EntityManagerFactory getEntityManagerFactory() {
            // TODO Auto-generated method stub
            return null;
        }

        public CriteriaBuilder getCriteriaBuilder() {
            // TODO Auto-generated method stub
            return null;
        }

        public Metamodel getMetamodel() {
            // TODO Auto-generated method stub
            return null;
        }

        public void joinTransaction() {
            logger.info("joinTransaction()");
        }

        public Object getDelegate() {
            return null;
        }

        public void close() {
            if (open) {
                logger.info("close()");
                open = false;
            }
        }

        public boolean isOpen() {
            return open;
        }

        public EntityTransaction getTransaction() {
            return null;
        }
    }
}

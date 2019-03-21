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

package com.springsource.insight.plugin.eclipse.persistence;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.partitioning.PartitioningPolicy;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.eclipse.persistence.exceptions.IntegrityChecker;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.history.AsOfClause;
import org.eclipse.persistence.internal.databaseaccess.Platform;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.platform.server.ServerPlatform;
import org.eclipse.persistence.queries.AttributeGroup;
import org.eclipse.persistence.queries.Call;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sequencing.SequencingControl;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.ExternalTransactionController;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.Login;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEventManager;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.coordination.CommandManager;
import org.eclipse.persistence.sessions.factories.ReferenceMode;

/**
 * 
 */
@SuppressWarnings({ "rawtypes", "deprecation" }) 
public class MockDatabaseSession implements DatabaseSession {
    private final Logger    logger=Logger.getLogger(getClass().getName());
    private Login   login;

    public MockDatabaseSession() {
        super();
    }

    public Session acquireHistoricalSession(AsOfClause pastTime) {
        throw new UnsupportedOperationException("acquireHistoricalSession(" + pastTime + ") N/A");
    }

    public UnitOfWork acquireUnitOfWork() {
        throw new UnsupportedOperationException("acquireUnitOfWork() N/A");
    }

    public UnitOfWork acquireUnitOfWork(ReferenceMode referenceMode) {
        throw new UnsupportedOperationException("acquireUnitOfWork(" + referenceMode + ") N/A");
    }

    public void addQuery(String name, DatabaseQuery query) {
        throw new UnsupportedOperationException("addQuery(" + name + ") N/A");
    }

    public void addJPAQuery(DatabaseQuery query) {
        throw new UnsupportedOperationException("addJPAQuery(" + query.getName() + ") N/A");
    }

    public void clearIntegrityChecker() {
        throw new UnsupportedOperationException("clearIntegrityChecker() N/A");
    }

    public void clearProfile() {
        throw new UnsupportedOperationException("clearProfile() N/A");
    }

    public boolean containsQuery(String queryName) {
        return false;
    }

    public Object copy(Object originalObjectOrObjects) {
        throw new UnsupportedOperationException("copy(" + originalObjectOrObjects + ") N/A");
    }

    public Object copy(Object originalObjectOrObjects, AttributeGroup group) {
        throw new UnsupportedOperationException("copy(" + originalObjectOrObjects + ")[" + group + "] N/A");
    }

    public Object copyObject(Object original) {
        throw new UnsupportedOperationException("copyObject(" + original + ") N/A");
    }

    public Object copyObject(Object original, ObjectCopyingPolicy policy) {
        throw new UnsupportedOperationException("copyObject(" + original + ")[" + policy + "] N/A");
    }

    public boolean doesObjectExist(Object object) throws DatabaseException {
        return false;
    }

    public void dontLogMessages() {
        // ignore
    }

    public int executeNonSelectingCall(Call call) {
        throw new UnsupportedOperationException("executeNonSelectingCall() N/A");
    }

    public void executeNonSelectingSQL(String sqlString) {
        throw new UnsupportedOperationException("executeNonSelectingSQL(" + sqlString + ") N/A");
    }

    // NOTE: we delegate on purpose in order to check the cflowbelow issue(s)
    public Object executeQuery(String queryName) {
        return executeQuery(queryName, (Object) null);
    }

    public Object executeQuery(String queryName, Object arg1) {
        return executeQuery(queryName, arg1, null);
    }

    public Object executeQuery(String queryName, Object arg1, Object arg2) {
        return executeQuery(queryName, arg1, arg2, null);
    }

    public Object executeQuery(String queryName, Object arg1, Object arg2, Object arg3) {
        return executeQuery(queryName, Arrays.asList(String.valueOf(arg1), String.valueOf(arg2), String.valueOf(arg3)));
    }

    public Object executeQuery(String queryName, List argumentValues) {
        return executeQuery(queryName, Object.class, argumentValues);
    }

    public Object executeQuery(String queryName, Class domainClass) {
        return executeQuery(queryName, domainClass, null);
    }

    public Object executeQuery(String queryName, Class domainClass, Object arg1) {
        return executeQuery(queryName, domainClass, arg1, null);
    }

    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2) {
        return executeQuery(queryName, domainClass, arg1, arg2, null);
    }

    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2, Object arg3) {
        return executeQuery(queryName, domainClass, Arrays.asList(String.valueOf(arg1), String.valueOf(arg2), String.valueOf(arg3)));
    }

    public Object executeQuery(DatabaseQuery query) throws EclipseLinkException {
        return executeQuery(query, Collections.emptyList());
    }

    public Object executeQuery(DatabaseQuery query, List argumentValues) {
        return executeQuery(query.getName(), argumentValues);
    }

    public Object executeQuery(String queryName, Class domainClass, List argumentValues) {
        logger.info("executeQuery(" + queryName + ")[" + domainClass.getName() + "]: " + argumentValues);
        return queryName;
    }

    public Vector executeSelectingCall(Call call) {
        throw new UnsupportedOperationException("executeSelectingCall() N/A");
    }

    public Vector executeSQL(String sqlString) {
        throw new UnsupportedOperationException("executeSQL(" + sqlString + ") N/A");
    }

    public Session getActiveSession() {
        return null;
    }

    public UnitOfWork getActiveUnitOfWork() {
        return null;
    }

    public ClassDescriptor getClassDescriptor(Class theClass) {
        return null;
    }

    public ClassDescriptor getClassDescriptor(Object domainObject) {
        return null;
    }

    public ClassDescriptor getClassDescriptorForAlias(String alias) {
        return null;
    }

    public AsOfClause getAsOfClause() {
        return null;
    }

    public ReferenceMode getDefaultReferenceMode() {
        return null;
    }

    public ClassDescriptor getDescriptor(Class theClass) {
        return null;
    }

    public ClassDescriptor getDescriptor(Object domainObject) {
        return null;
    }

    public ClassDescriptor getDescriptorForAlias(String alias) {
        return null;
    }

    public Map<Class, ClassDescriptor> getDescriptors() {
        return Collections.emptyMap();
    }

    public List<DatabaseQuery> getJPAQueries() {
        return Collections.emptyList();
    }

    public SessionEventManager getEventManager() {
        return null;
    }

    public ExceptionHandler getExceptionHandler() {
        return null;
    }

    public ExternalTransactionController getExternalTransactionController() {
        return null;
    }

    public IdentityMapAccessor getIdentityMapAccessor() {
        return null;
    }

    public IntegrityChecker getIntegrityChecker() {
        return null;
    }

    public Writer getLog() {
        return null;
    }

    public DatabasePlatform getPlatform() {
        return null;
    }

    public Platform getDatasourcePlatform() {
        return null;
    }

    public DatabaseLogin getLogin() {
        if (this.login == null) {
            return null;
        }

        if (this.login instanceof DatabaseLogin) {
            return (DatabaseLogin) this.login;
        }

        DatabaseLogin   dblogin=new DatabaseLogin();
        dblogin.setUserName(this.login.getUserName());
        dblogin.setPassword(this.login.getPassword());
        return dblogin;
    }
    public void setLogin(@SuppressWarnings("hiding") Login login) {
        this.login = login;
    }

    public Login getDatasourceLogin() {
        return this.login;
    }

    public void setDatasourceLogin(@SuppressWarnings("hiding") Login login) {
        setLogin(login);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public Number getNextSequenceNumberValue(Class domainClass) {
        return null;
    }

    public SessionProfiler getProfiler() {
        return null;
    }

    public Project getProject() {
        return null;
    }

    public Map<Object, Object> getProperties() {
        return Collections.emptyMap();
    }

    public Object getProperty(String name) {
        return null;
    }

    public Map<String, List<DatabaseQuery>> getQueries() {
        return Collections.emptyMap();
    }

    public DatabaseQuery getQuery(String name) {
        return getQuery(name, Collections.emptyList());
    }

    public DatabaseQuery getQuery(String name, List arguments) {
        return null;
    }

    public SessionLog getSessionLog() {
        return null;
    }

    public Object handleException(RuntimeException exception)
            throws RuntimeException {
        logger.warning("handleException(" + exception.getClass().getSimpleName() + ")"
                     + ": " + exception.getMessage());
        throw exception;
    }

    public boolean hasDescriptor(Class theClass) {
        return false;
    }

    public boolean hasExceptionHandler() {
        return false;
    }

    public boolean hasExternalTransactionController() {
        return false;
    }

    public boolean isClientSession() {
        return false;
    }

    public boolean isConnected() {
        return true;
    }

    public boolean isDatabaseSession() {
        return true;
    }

    public boolean isDistributedSession() {
        return false;
    }

    public boolean isInProfile() {
        return false;
    }

    public boolean isRemoteSession() {
        return false;
    }

    public boolean isServerSession() {
        return false;
    }

    public boolean isSessionBroker() {
        return false;
    }

    public boolean isUnitOfWork() {
        return false;
    }

    public boolean isRemoteUnitOfWork() {
        return false;
    }

    public Object getId(Object domainObject) throws ValidationException {
        throw new UnsupportedOperationException("getId(" + domainObject + ") N/A");
    }

    public Vector keyFromObject(Object domainObject) throws ValidationException {
        throw new UnsupportedOperationException("keyFromObject(" + domainObject + ") N/A");
    }

    public void log(SessionLogEntry entry) {
        logger.info("log(" + entry.getNameSpace() + ")[" + entry.getLevel() + "]: " + entry.getMessage());
    }

    public void logMessage(String message) {
        logger.info("logMessage() " + message);
    }

    public Vector readAllObjects(Class domainClass) throws DatabaseException {
        return readAllObjects(domainClass, (Call) null);
    }

    public Vector readAllObjects(Class domainClass, Call aCall)
            throws DatabaseException {
        throw new UnsupportedOperationException("readAllObjects(" + domainClass.getName() + ") N/A");
    }

    public Vector readAllObjects(Class domainClass, Expression selectionCriteria)
            throws DatabaseException {
        throw new UnsupportedOperationException("readAllObjects(" + domainClass.getName() + ")[" + selectionCriteria + "] N/A");
    }

    public Object readObject(Class domainClass) throws DatabaseException {
        return readObject(domainClass, (Call) null);
    }

    public Object readObject(Class domainClass, Call aCall)
            throws DatabaseException {
        throw new UnsupportedOperationException("readObject(" + domainClass.getName() + ") N/A");
    }

    public Object readObject(Class domainClass, Expression selectionCriteria)
            throws DatabaseException {
        throw new UnsupportedOperationException("readObject(" + domainClass.getName() + ")[" + selectionCriteria + "] N/A");
    }

    public Object readObject(Object object) throws DatabaseException {
        throw new UnsupportedOperationException("readObject(" + object + ") N/A");
    }

    public Object refreshObject(Object object) {
        throw new UnsupportedOperationException("refreshObject(" + object + ") N/A");
    }

    public void release() {
        logger.info("release()");
    }

    public void removeProperty(String property) {
        // ignored 
    }

    public void removeQuery(String queryName) {
        // ignored 
    }

    public void setDefaultReferenceMode(ReferenceMode defaultReferenceMode) {
        // ignored 
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        // ignored 
    }

    public void setIntegrityChecker(IntegrityChecker integrityChecker) {
        // ignored 
    }

    public void setLog(Writer log) {
        // ignored 
    }

    public void setName(String name) {
        // ignored 
    }

    public void setProfiler(SessionProfiler profiler) {
        // ignored 
    }

    public void setProperty(String propertyName, Object propertyValue) {
        // ignored 
    }

    public void setSessionLog(SessionLog sessionLog) {
        // ignored 
    }

    public boolean shouldLogMessages() {
        return true;
    }

    public void validateCache() {
        // ignored 
    }

    public int getLogLevel(String category) {
        return SessionLog.INFO;
    }

    public int getLogLevel() {
        return SessionLog.INFO;
    }

    public void setLogLevel(int level) {
        // ignored 
    }

    public boolean shouldLog(int Level, String category) {
        return true;
    }

    public Object handleSevere(RuntimeException exception)
            throws RuntimeException {
        logger.severe("handleSevere(" + exception.getClass().getSimpleName() + ")"
                + ": " + exception.getMessage());
        throw exception;
    }

    public boolean isFinalizersEnabled() {
        return false;
    }

    public void setIsFinalizersEnabled(boolean isFinalizersEnabled) {
        // ignored 
    }

    public void setQueryTimeoutDefault(int queryTimeoutDefault) {
        // ignored 
    }

    public PartitioningPolicy getPartitioningPolicy() {
        return null;
    }

    public void setPartitioningPolicy(PartitioningPolicy partitioningPolicy) {
        // ignored 
    }

    public void addDescriptor(ClassDescriptor descriptor) {
        addDescriptors(Collections.singletonList(descriptor));
    }

    public void addDescriptors(Collection descriptors) {
        throw new UnsupportedOperationException("addDescriptors(" + descriptors + ") N/A");
    }

    public void addDescriptors(Project project) {
        throw new UnsupportedOperationException("add(Project)Descriptors(" + project + ") N/A");
    }

    public void beginTransaction() throws DatabaseException {
        logger.info("beginTransaction()");
    }

    public void commitTransaction() throws DatabaseException {
        logger.info("commitTransaction()");
    }

    public void deleteAllObjects(Collection domainObjects) {
        throw new UnsupportedOperationException("deleteAllObjects(" + domainObjects + ") N/A");
    }

    public Object deleteObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        throw new UnsupportedOperationException("deleteObject(" + domainObject + ") N/A");
    }

    public Object insertObject(Object domainObject) throws DatabaseException {
        throw new UnsupportedOperationException("insertObject(" + domainObject + ") N/A");
    }

    public boolean isInTransaction() {
        return false;
    }

    public void setServerPlatform(ServerPlatform newServerPlatform) {
        // ignored
    }

    public ServerPlatform getServerPlatform() {
        return null;
    }

    public SequencingControl getSequencingControl() {
        return null;
    }

    // NOTE: we delegate on purpose in order to check the cflowbelow issue(s)
    public void login() throws DatabaseException {
        login(this.login);
    }

    public void login(@SuppressWarnings("hiding") Login login) throws DatabaseException {
        login(login.getUserName(), login.getPassword());
    }

    public void login(String userName, String password)
            throws DatabaseException {
        logger.info("login(" + userName + ")[" + password + "]");
    }

    public void logout() throws DatabaseException {
        logger.info("logout()");
    }

    public Object refreshAndLockObject(Object object) {
        return refreshAndLockObject(object, (short) (-1));
    }

    public Object refreshAndLockObject(Object object, short lockMode) {
        throw new UnsupportedOperationException("refreshAndLockObject(" + object + ")[" + lockMode + "] N/A");
    }

    public void rollbackTransaction() throws DatabaseException {
        logger.info("rollbackTransaction()");
    }

    public void setExternalTransactionController(ExternalTransactionController etc) {
        // ignored
    }

    public CommandManager getCommandManager() {
        return null;
    }

    public void setCommandManager(CommandManager commandManager) {
        // ignored
    }

    public void setShouldPropagateChanges(boolean choice) {
        // ignored
    }

    public boolean shouldPropagateChanges() {
        return false;
    }

    public Object updateObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        throw new UnsupportedOperationException("updateObject(" + domainObject + ") N/A");
    }

    public void writeAllObjects(Collection domainObjects) {
        throw new UnsupportedOperationException("writeAllObjects(" + domainObjects + ") N/A");
    }

    public Object writeObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        writeAllObjects(Collections.singletonList(domainObject));
        return domainObject;
    }
}

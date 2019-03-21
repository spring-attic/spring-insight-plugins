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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.hibernate.Session;
import org.hibernate.stat.SessionStatistics;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;

public aspect HibernateSessionOperationCollectionAspect
		extends AbstractOperationCollectionAspect
		implements CollectionSettingsUpdateListener {
    
    protected static final OperationType TYPE = OperationType.valueOf("hibernate");
    protected static final CollectionSettingName cs = new CollectionSettingName("statistics",
    		HibernatePluginRuntimeDescriptor.PLUGIN_NAME,
            "Provides additional statistics from Session.getStatistics");
    protected final AtomicBoolean collectStatistics=new AtomicBoolean(true);

    public HibernateSessionOperationCollectionAspect() {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(this);
        registry.register(cs, Boolean.TRUE);
    }

    public pointcut flushExecute() 
        : execution(void Session.flush());

    public pointcut saveExecute()
        : execution(* Session.save(..));
    
    public pointcut updateExecute()
    	: execution(void Session.update(..));

    public pointcut deleteExecute()
    	: execution(void Session.delete(..));

    public pointcut loadExecute()
    	: execution(Object Session.load(..));
   
    public pointcut isDirtyExecute()
    	: execution(boolean Session.isDirty());
    
    public pointcut getExecute()
    	: execution(Object Session.get(..));

    /**
     * Many of the basic hibernate methods are chained,
     * so we use cflowbelow to cull subsequent calls.
     */
    public pointcut collectionPoint() 
	    : (flushExecute() && !cflowbelow(flushExecute()))
	    || (saveExecute() && !cflowbelow(saveExecute()))
	    || (updateExecute() && !cflowbelow(updateExecute()))
	    || (deleteExecute() && !cflowbelow(deleteExecute()))
	    || (isDirtyExecute() && !cflowbelow(isDirtyExecute()))
	    || (getExecute() && !cflowbelow(getExecute()))         
	    || (loadExecute() && !cflowbelow(loadExecute()))
	    ;
    
    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Session session = (Session) jp.getThis();
        Operation op = new Operation()
            .type(TYPE)
            .label("Hibernate Session." + method)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("method", method)
            .put("flushMode", session.getFlushMode().toString());
        if (collectStatistics.get()) {
            SessionStatistics stats = session.getStatistics();
            op = op.put("entityCount", stats.getEntityCount())
                   .put("collectionCount", stats.getCollectionCount());
        }

        return op;
    }

    /**
     * Example of direct usage of CollectionSettings
     */
    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (cs.equals(name)) {
            boolean	newValue=CollectionSettingsRegistry.getBooleanSettingValue(value);
            boolean	prevValue=collectStatistics.getAndSet(newValue);
            if (prevValue != newValue) {
            	Logger	logger=Logger.getLogger(getClass().getName());
            	logger.info("incrementalUpdate(" + name + ") " + prevValue + " => " + newValue);
            }
        }
   }

    @Override
    public String getPluginName() {
        return HibernatePluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

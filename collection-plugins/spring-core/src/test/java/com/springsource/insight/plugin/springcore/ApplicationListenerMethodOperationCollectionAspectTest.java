/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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

package com.springsource.insight.plugin.springcore;

import il.co.springsource.insight.MyApplicationListener;
import il.co.springsource.insight.MyApplicationListenerAndRepository;
import il.co.springsource.insight.MyEvent;
import il.co.springsource.insight.MyEventSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.support.StaticApplicationContext;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.OperationListCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;

public class ApplicationListenerMethodOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ApplicationListenerMethodOperationCollectionAspectTest () {
		super();
	}

    @Override
	protected OperationCollector createSpiedOperationCollector(OperationCollector originalCollector) {
        assertNotNull("No original collector", originalCollector);
		return new OperationListCollector();
	}

	@Override
	protected Operation getLastEnteredOperation(OperationCollector spiedCollector) {
        List<Operation>	opsList=((OperationListCollector) spiedCollector).getCollectedOperations();
		if (ListUtil.size(opsList) <= 0) {
			return null;
		} else {
			return opsList.get(opsList.size() - 1);
		}
	}

	/**
     * Verifies that if a regular listener and a @Repository annotated listener are called, that
     * only the regular listener generates a MethodOperation.  The @Repository
     * annotation will generate a MethodOperation independently for those methods;
     */
    @Test
    public void testAppListenerCalled() {
        StaticApplicationContext ctx = new StaticApplicationContext();
        ctx.registerSingleton("myListener", MyApplicationListener.class);
        ctx.registerSingleton("myListenerAndRepo", MyApplicationListenerAndRepository.class);
        ctx.refresh();

        MyApplicationListener listener = ctx.getBean(MyApplicationListener.class);
        MyApplicationListenerAndRepository listenerAndRepo = ctx.getBean(MyApplicationListenerAndRepository.class);
        
        MyEvent event = new MyEvent("fubar");
        ctx.publishEvent(event);
        assertSame("Mismatched listener event", event, listener.getLastEvent());
        assertSame("Mismatched listener&repo event", event, listenerAndRepo.getLastEvent());
        
        List<Operation>	opsList=((OperationListCollector) spiedOperationCollector).getCollectedOperations();
        assertEquals("Mismatched number of collected operations", 2, ListUtil.size(opsList));

        @SuppressWarnings("unchecked")
		Collection<Class<?>>	beanTypes=ListUtil.asSet((Class<?>) MyApplicationListener.class, (Class<?>) MyApplicationListenerAndRepository.class);
        for (Operation op : opsList) {
        	Class<?>	matchClass=assertOperationBelongsToClass(op, beanTypes);
        	assertTrue("Unexpected match: " + matchClass.getSimpleName(), beanTypes.remove(matchClass));
        }
    }
    
    @Test
    public void testSourceFilteredListener() {
        StaticApplicationContext ctx = new StaticApplicationContext();
        MyApplicationListener delegate = new MyApplicationListener();
        MyEventSource source = new MyEventSource();
        MyEvent event = new MyEvent(source);
        ctx.addApplicationListener(new SourceFilteringListener(source, delegate));
        ctx.refresh();
        ctx.publishEvent(event);

        assertSame("Mismatched delegate event", event, delegate.getLastEvent());
        
        List<Operation>	opsList=((OperationListCollector) spiedOperationCollector).getCollectedOperations();
        assertEquals("Mismatched number of collected operations", 1, ListUtil.size(opsList));
        assertOperationBelongsToClass(opsList.get(0), MyApplicationListener.class);
    }
    
    private Class<?> assertOperationBelongsToClass(Operation op, Class<?> ... beanTypes) {
    	return assertOperationBelongsToClass(op,
    				(ArrayUtil.length(beanTypes) <= 0)
    					? Collections.<Class<?>>emptyList()
    					: Arrays.asList(beanTypes));
    }

    private Class<?> assertOperationBelongsToClass(Operation op, Collection<Class<?>> beanTypes) {
    	if (op.isFinalizable()) {
    		op.finalizeConstruction();
    	}

    	String		shortName=op.get(OperationFields.SHORT_CLASS_NAME, String.class);
    	Class<?>	matchingClass=null;
    	for (Class<?> clazz : beanTypes) {
    		if (clazz.getSimpleName().equals(shortName)) {
    			if (matchingClass != null) {
    				fail("Muliple matches for " + shortName + ": " + matchingClass.getSimpleName());
    			}
    			matchingClass = clazz;
    		}
    	}
    	assertNotNull("Mismatched short class name: " + shortName, matchingClass);

        String	compType=op.get(StereotypedSpringBeanMethodOperationCollectionAspectSupport.COMP_TYPE_ATTR, String.class);
        // make sure not intercepted by one of the stereotyped beans aspects
        assertNull("Unexpected stereotyped bean method collection: " + compType, compType);

        String	methodName=op.get(OperationFields.METHOD_NAME, String.class, "");
        assertTrue(methodName + " does not start with onApplicationEvent", methodName.startsWith("onApplicationEvent"));

        OperationList	argsList=op.get(OperationFields.ARGUMENTS, OperationList.class);
        assertNotNull("Missing arguments list", argsList);
        assertEquals("Mismatched number of arguments", 1, argsList.size());
        assertEquals("Mismatched argument type", MyEvent.class.getName(), argsList.get(0));
        
        return matchingClass;
    }

    @Override
    public ApplicationListenerMethodOperationCollectionAspect getAspect() {
        return ApplicationListenerMethodOperationCollectionAspect.aspectOf();
    }
}

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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.plugin.springcore.beans.Fubar;

public class ClassPathScanOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public ClassPathScanOperationCollectionAspectTest () {
        super();
    }

    @Test
    public void testMethodsIntercepted() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-class-path-scan-operation.xml", getClass());
        assertNotNull("Cannot find " + Fubar.class.getSimpleName(), ctx.getBean(Fubar.class));

        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        Mockito.verify(spiedOperationCollector, Mockito.atLeastOnce()).enter(opCaptor.capture());

        final Package		pkg=getClass().getPackage();
        Map<String,String>	locationsMap=new TreeMap<String, String>() {
				private static final long serialVersionUID = 1L;
	
				{
	        		put("findCandidateComponents", pkg.getName());
	        		put("findPathMatchingResources", "classpath*:" + pkg.getName().replace('.',  '/') + "/**/*.class");
	        	}
	        };
        for (Operation captured : opCaptor.getAllValues()) {
        	Operation			op=assertScanOperation(captured);
        	SourceCodeLocation	scl=op.getSourceCodeLocation();
        	String				methodName=scl.getMethodName();
        	String				expectedLocation=locationsMap.remove(methodName);
        	assertNotNull("Unnown method: " + methodName, expectedLocation);
        	
        	String	actualLocation=op.get(SpringLifecycleMethodOperationCollectionAspect.EVENT_ATTR, String.class);
        	assertEquals(methodName + ": Mismatched location", expectedLocation, actualLocation);
        }

        assertTrue("Aspect did not intercept call to " + locationsMap.keySet(), locationsMap.isEmpty());
    }

    protected Operation assertScanOperation (Operation op) {
    	assertNotNull("No operation", op);
        assertEquals("Mismatched operation type", SpringCorePluginRuntimeDescriptor.CLASSPATH_SCAN_TYPE, op.getType());

        String	compType=op.get(StereotypedSpringBeanMethodOperationCollectionAspectSupport.COMP_TYPE_ATTR, String.class);
        // make sure not intercepted by one of the stereotyped beans aspects
        assertNull("Unexpected stereotyped bean method collection: " + compType, compType);
        return op;

    }
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ClassPathScanOperationCollectionAspect.aspectOf();
    }
}

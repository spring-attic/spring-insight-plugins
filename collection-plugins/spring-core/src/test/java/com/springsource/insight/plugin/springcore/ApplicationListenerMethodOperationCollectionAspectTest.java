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

package com.springsource.insight.plugin.springcore;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.stereotype.Repository;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;

public class ApplicationListenerMethodOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ApplicationListenerMethodOperationCollectionAspectTest () {
		super();
	}

    /**
     * Verifies that if a regular listener and a @Repository annotated listener are called, that
     * only the regular listener generates a MethodOperation.  The @Repository
     * annotation will generate a MethodOperation independently for those methods;
     */
    @Test
    public void appListenerCalled() {
        StaticApplicationContext ctx = new StaticApplicationContext();
        ctx.registerSingleton("myListener", MyApplicationListener.class);
        ctx.registerSingleton("myListenerAndRepo", MyApplicationListenerAndRepository.class);
        ctx.refresh();
        MyApplicationListener listener = ctx.getBean(MyApplicationListener.class);
        MyApplicationListenerAndRepository listenerAndRepo = ctx.getBean(MyApplicationListenerAndRepository.class);
        
        MyEvent event = new MyEvent("fubar");
        ctx.publishEvent(event);
        assertSame(event, listener.getLastEvent());
        assertSame(event, listenerAndRepo.getLastEvent());
        
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, times(1)).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        
        assertOperationBelongsToClass(operation, MyApplicationListener.class);
    }
    
    @Test
    public void sourceFilteredListener() {
        StaticApplicationContext ctx = new StaticApplicationContext();
        MyApplicationListener delegate = new MyApplicationListener();
        MyEventSource source = new MyEventSource();
        MyEvent event = new MyEvent(source);
        ctx.addApplicationListener(new SourceFilteringListener(source, delegate));
        ctx.refresh();
        ctx.publishEvent(event);

        assertSame(event, delegate.getLastEvent());
        
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, times(1)).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        assertOperationBelongsToClass(operation, MyApplicationListener.class);
    }
    
    private void assertOperationBelongsToClass(Operation op, Class<?> clazz) {
        op.finalizeConstruction();
        assertEquals(clazz.getSimpleName(), op.get("shortClassName"));
        assertTrue(op.get("methodName") + " does not start with onApplicationEvent", op.get("methodName", String.class).startsWith("onApplicationEvent"));
        assertEquals(MyEvent.class.getName(), op.get(OperationFields.ARGUMENTS, OperationList.class).get(0));
        assertEquals(1, op.get("arguments", OperationList.class).size());
    }

    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ApplicationListenerMethodOperationCollectionAspect.aspectOf();
    }

    public static class MyEventSource {
    	public MyEventSource () {
    		super();
    	}
    }
    
    public static class MyEvent extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public MyEvent(MyEventSource src) {
            super(src);
        }
        
        public MyEvent(String src) {
            super(src);
        }
    }
    
    public static class MyApplicationListener implements ApplicationListener<MyEvent> {
        private MyEvent lastEvent ;

        public ApplicationEvent getLastEvent() {
            return lastEvent;
        }
        
        public void onApplicationEvent(MyEvent event) {
            lastEvent = event;
        }
    }
    
    @Repository
    public static class MyApplicationListenerAndRepository implements ApplicationListener<MyEvent> {
        private MyEvent lastEvent = null;

        public ApplicationEvent getLastEvent() {
            return lastEvent;
        }
        
        public void onApplicationEvent(MyEvent event) {
            lastEvent = event;
        }
    }
}

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
package com.springsource.insight.plugin.jsf;

import static org.mockito.Mockito.mock;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.sun.faces.lifecycle.LifecycleImpl;

/**
 * This test verifies that JSF validators are correctly captured by the aspect,
 * {@link ValidatorOperationCollectionAspect}.
 */
public class PhaseListenerOperationCollectionAspectTest extends
		OperationCollectionAspectTestSupport {

    @Test
    public void beforePhaseCollected() {
        /**
         * First step: Execute whatever method is matched by our pointcut in
         * {@link PhaseListenerOperationCollectionAspect}
         * 
         */
        FacesContext mockFacesContext = mock(FacesContext.class);

        PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.PROCESS_VALIDATIONS, new LifecycleImpl());
        
        MockPhaseListener listener = new MockPhaseListener();
        listener.beforePhase(event);

        /**
         * Second step: Snatch the operation that was just created
         */
        Operation op = getLastEntered();

        /**
         * Third step: Validate that our operation has been created as we expect
         */
        assertEquals(MockPhaseListener.class.getName(), op.getSourceCodeLocation()
                .getClassName());
        assertEquals("beforePhase", op.getSourceCodeLocation().getMethodName());
        assertEquals(PhaseId.PROCESS_VALIDATIONS.toString(), op.get("phaseId"));

        listener.afterPhase(event);
    }

    @Test
    public void afterPhaseCollected() {
        /**
         * First step: Execute whatever method is matched by our pointcut in
         * {@link PhaseListenerOperationCollectionAspect}
         * 
         */
        FacesContext mockFacesContext = mock(FacesContext.class);

        PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.PROCESS_VALIDATIONS, new LifecycleImpl());
        
        MockPhaseListener listener = new MockPhaseListener();
        listener.afterPhase(event);

        /**
         * Second step: Snatch the operation that was just created
         */
        Operation op = getLastEntered();

        /**
         * Third step: Validate that our operation has been created as we expect
         */
        assertEquals(MockPhaseListener.class.getName(), op.getSourceCodeLocation()
                .getClassName());
        assertEquals("afterPhase", op.getSourceCodeLocation().getMethodName());
        assertEquals(PhaseId.PROCESS_VALIDATIONS.toString(), op.get("phaseId"));
    }

	private static class MockPhaseListener implements PhaseListener {

        private static final long serialVersionUID = 20121125L;

        public void afterPhase(PhaseEvent event) {
        }

        public void beforePhase(PhaseEvent event) {
        }

        public PhaseId getPhaseId() {
            return PhaseId.PROCESS_VALIDATIONS;
        }
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return PhaseListenerOperationCollectionAspect.aspectOf();
	}
}

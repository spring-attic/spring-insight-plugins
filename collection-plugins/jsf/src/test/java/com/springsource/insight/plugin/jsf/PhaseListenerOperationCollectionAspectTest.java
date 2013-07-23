package com.springsource.insight.plugin.jsf;

import static org.junit.Assert.assertEquals;
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

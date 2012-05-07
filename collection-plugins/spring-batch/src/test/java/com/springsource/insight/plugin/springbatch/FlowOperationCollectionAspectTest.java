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

package com.springsource.insight.plugin.springbatch;

import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecution;
import org.springframework.batch.core.job.flow.FlowExecutionException;
import org.springframework.batch.core.job.flow.FlowExecutor;
import org.springframework.batch.core.job.flow.State;

import com.springsource.insight.intercept.operation.Operation;


/**
 * 
 */
public class FlowOperationCollectionAspectTest
        extends SpringBatchOperationCollectionAspectTestSupport {
    public FlowOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testStart () throws FlowExecutionException {
        FlowExecutor    flowExecutor=createFlowExecutor("testStartJob", "testStartStep");
        Flow            flow=new Flow() {
                public String getName() {
                    return "testStart";
                }
    
                public State getState(String stateName) {
                    return null;
                }
    
                public FlowExecution start(FlowExecutor executor)
                        throws FlowExecutionException {
                    return null;
                }
    
                public FlowExecution resume(String stateName, FlowExecutor executor)
                        throws FlowExecutionException {
                    throw new FlowExecutionException("Unexpected resume call");
                }
    
                public Collection<State> getStates() {
                    return Collections.emptyList();
                }
            };
        flow.start(flowExecutor);

        Operation   op=assertOperationDetails(getLastEntered(), "start", flow.getName());
        assertOperationPath(op, flowExecutor);
    }

    @Test
    public void testResume () throws FlowExecutionException {
        FlowExecutor    flowExecutor=createFlowExecutor("testResumeJob", "testResumeStep");
        Flow    flow=new Flow() {
                public String getName() {
                    return "testResume";
                }
    
                public State getState(String stateName) {
                    return null;
                }
    
                public FlowExecution start(FlowExecutor executor)
                        throws FlowExecutionException {
                    throw new FlowExecutionException("Unexpected start call");
                }
    
                public FlowExecution resume(String stateName, FlowExecutor executor)
                        throws FlowExecutionException {
                    return null;
                }
    
                public Collection<State> getStates() {
                    return Collections.emptyList();
                }
            };
        flow.resume("test", flowExecutor);

        Operation   op=assertOperationDetails(getLastEntered(), "resume", flow.getName());
        Assert.assertEquals("Mismatched state value", "test", op.get("flowState", String.class));
        assertOperationPath(op, flowExecutor);
    }

    @Override
    public FlowOperationCollectionAspect getAspect() {
        return FlowOperationCollectionAspect.aspectOf();
    }
}

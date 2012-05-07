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

package com.springsource.insight.plugin.jta;

import org.mockito.Mockito;

import org.junit.Assert;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;

/**
 * 
 */
public abstract class JtaOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    protected static final Class<?>[] EMPTY_CLASSES={ };

    protected JtaOperationCollectionAspectTestSupport() {
        super();
    }

    protected <E extends Enum<E> & Runnable & ParameterTypeDescriptor> void runAspectOperations (Class<E> opsClass) {
        for (E testCase : opsClass.getEnumConstants()) {
            String  testName=testCase.name(), action=testName.toLowerCase();
            testCase.run();
            assertTransactionOperation(testName, action, testCase.getArgTypes());
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    protected Operation assertTransactionOperation (String testName, String action, Class<?>[] argTypes) {
        Operation   op=getLastEntered();
        Assert.assertNotNull(testName + ": No operation", op);

        JtaOperationCollectionAspect    aspectInstance=getJtaOperationCollectionAspect();
        Assert.assertEquals(testName + ": Mismatched operation type", aspectInstance.getOperationType(), op.getType());

        Class<?>    txClass=aspectInstance.getTransactionClass();
        Assert.assertEquals(testName + ": Mismatched full class name", txClass.getName(), op.get(OperationFields.CLASS_NAME, String.class));
        Assert.assertEquals(testName + ": Mismatched short class name", txClass.getSimpleName(), op.get(OperationFields.SHORT_CLASS_NAME, String.class));
        Assert.assertEquals(testName + ": Mismatched action", action, op.get(JtaDefinitions.ACTION_ATTR, String.class));

        SourceCodeLocation  scl=new SourceCodeLocation(op.get(OperationFields.CLASS_NAME, String.class),
                                                       op.get(OperationFields.METHOD_NAME, String.class),
                                                       (-1));
        Assert.assertEquals(testName + ": Mismatched method signature",
                            JoinPointBreakDown.getMethodStringFromArgs(scl, argTypes),
                            op.get(OperationFields.METHOD_SIGNATURE, String.class));
        return op;
    }

    JtaOperationCollectionAspect getJtaOperationCollectionAspect () {
        return (JtaOperationCollectionAspect) getAspect();
    }
    
    protected static interface ParameterTypeDescriptor {
        Class<?>[] getArgTypes ();
    }
}

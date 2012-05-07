/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.ejb3;

import java.lang.annotation.Annotation;
import java.util.Date;

import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.StringFormatterUtils;

/**
 */
public class Ejb3LocalServiceOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public Ejb3LocalServiceOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testEjb3StatefulLocalServiceAction () throws Exception {
        runEjb3LocalServiceAction(new ExampleStatefulEjb3LocalServiceAction(), Stateful.class);
    }

    @Test
    public void testEjb3StatelessLocalServiceAction () throws Exception {
        runEjb3LocalServiceAction(new ExampleStatelessEjb3LocalServiceAction(), Stateless.class);
    }

    private <C extends ExampleEjb3LocalService & SessionSynchronization> void runEjb3LocalServiceAction (
            final C testedService, final Class<? extends Annotation> annClass)
                throws Exception {
        final Class<?>  testedServiceClass=testedService.getClass();
        Assert.assertNotNull("Missing " + annClass.getSimpleName() + " annotation", testedServiceClass.getAnnotation(annClass));

        testedService.afterBegin();

        final Date[]    invocationArgs={ new Date(System.currentTimeMillis()) };
        testedService.beforeCompletion();
        testedService.invokeMe(invocationArgs[0]);
        testedService.afterCompletion(true);

        final Operation op=getLastEntered();
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched " + annClass.getSimpleName() + " EJB operation type(s)", Ejb3LocalServiceDefinitions.TYPE, op.getType());

        // see JoinPointBreakDownSourceCodeLocationFinalizer#populateOperation
        Assert.assertEquals("Mismatched " + annClass.getSimpleName() + " class name", testedServiceClass.getName(), op.get(OperationFields.CLASS_NAME, String.class));
        Assert.assertEquals("Mismatched " + annClass.getSimpleName() + " invocation method", "invokeMe", op.get("methodName", String.class));
        
        final OperationList argsList=op.get(OperationFields.ARGUMENTS, OperationList.class);
        Assert.assertNotNull("Missing " + annClass.getSimpleName() + " invocation arguments", argsList);
        Assert.assertEquals("Mismatched " + annClass.getSimpleName() + " num. of invocation arguments", invocationArgs.length, argsList.size());

        for (int    aIndex=0; aIndex < invocationArgs.length; aIndex++) {
            final Object    expArg=invocationArgs[aIndex], actArg=argsList.get(aIndex);
            final String    expStr=StringFormatterUtils.formatObject(expArg),
                            actStr=StringFormatterUtils.formatObject(actArg);
            Assert.assertEquals("Mismatched " + annClass.getSimpleName() + " invocation arguments #" + aIndex, expStr, actStr);
        }
    }

    @Override
    public Ejb3LocalServiceOperationCollectionAspect getAspect() {
        return Ejb3LocalServiceOperationCollectionAspect.aspectOf();
    }

}

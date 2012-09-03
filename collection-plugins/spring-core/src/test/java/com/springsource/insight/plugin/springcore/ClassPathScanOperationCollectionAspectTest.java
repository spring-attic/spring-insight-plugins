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

import static com.springsource.insight.util.ListUtil.asSet;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ClassPathScanOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public ClassPathScanOperationCollectionAspectTest () {
        super();
    }

    @Test
    public void methodsIntercepted() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("com/springsource/insight/plugin/springcore/test-class-path-scan-operation.xml");
        assertNotNull(ctx.getBean(Fubar.class));

        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, atLeastOnce()).enter(opCaptor.capture());

        Set<String> methodsToFind = asSet("refresh", "findCandidateComponents", "findPathMatchingResources");
        for (Operation op : opCaptor.getAllValues()) {
            methodsToFind.remove(op.getSourceCodeLocation().getMethodName());
        }

        assertTrue("Aspect did not intercept call to " + methodsToFind, methodsToFind.isEmpty());
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ClassPathScanOperationCollectionAspect.aspectOf();
    }
}

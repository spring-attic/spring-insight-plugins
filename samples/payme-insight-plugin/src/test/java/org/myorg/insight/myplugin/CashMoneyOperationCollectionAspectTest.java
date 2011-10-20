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

package org.myorg.insight.myplugin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This test verifies that {@link MyOperation} is correctly captured by
 * the aspect, {@link MyOperationAspectCollectionAspect}.
 */
public class CashMoneyOperationCollectionAspectTest
    extends OperationCollectionAspectTestSupport
{
    @Test
    public void myOperationCollected() {
        /**
         * First step:  Execute whatever method is matched by our pointcut in
         * {@link CashMoneyOperationCollectionAspect}
         * 
         * In this case, we simply create a new class which matches our pointcut
         * (which is any method named setCashBalance, taking a (int) balance.
         */
        DummyAccount account = new DummyAccount();
        account.setBalance(42);
        
        /**
         * Second step:  Snatch the operation that was just created   
         */
        Operation op = getLastEntered(Operation.class);
        
        /**
         * Third step:  Validate that our operation has been created as we expect
         */
        assertEquals(42, op.get("newBalance"));
        assertEquals("Cash Balance Set: 42", op.getLabel());
        assertEquals(DummyAccount.class.getName(), op.getSourceCodeLocation().getClassName());
        assertEquals("setBalance", op.getSourceCodeLocation().getMethodName());
    }

    private static class DummyAccount {
        public void setBalance(int balance) {
            // ... real implementations will actually do something here.
        }
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return CashMoneyOperationCollectionAspect.aspectOf();
    }
}

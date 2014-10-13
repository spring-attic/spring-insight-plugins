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

package org.myorg.insight.myplugin;

import org.junit.Test;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * This test verifies that {@link MyOperation} is correctly captured by
 * the aspect, {@link MyOperationAspectCollectionAspect}.
 */
public class CashMoneyOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public CashMoneyOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testMyOperationCollected() {
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
        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        /**
         * Third step:  Validate that our operation has been created as we expect
         */
        assertEquals("Mismatched operation type", CashMoneyOperationCollectionAspect.TYPE, op.getType());
        assertEquals("Mismatched balance value", 42, op.getInt("newBalance", (-1)));
        assertEquals("Mismatched label", "Cash Balance Set: 42", op.getLabel());

        SourceCodeLocation scl = op.getSourceCodeLocation();
        assertEquals("Mismatched source code class", DummyAccount.class.getName(), scl.getClassName());
        assertEquals("Mismatched source code method", "setBalance", scl.getMethodName());
    }

    static class DummyAccount {
        public void setBalance(int balance) {
            // ... real implementations will actually do something here.
        }
    }

    @Override
    public CashMoneyOperationCollectionAspect getAspect() {
        return CashMoneyOperationCollectionAspect.aspectOf();
    }
}

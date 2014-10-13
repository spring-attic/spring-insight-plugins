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

package com.springsource.insight.plugin.jta;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;

/**
 *
 */
public aspect TransactionManagerOperationCollectionAspect extends JtaOperationCollectionAspect {
    public TransactionManagerOperationCollectionAspect() {
        super(JtaDefinitions.MGR_OP, TransactionManager.class);
    }

    public pointcut collectionPoint()
            : execution(* TransactionManager+.begin())
            || execution(* TransactionManager+.commit())
            || execution(* TransactionManager+.rollback())
            || execution(* TransactionManager+.suspend())
            || execution(* TransactionManager+.resume(Transaction))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = super.createOperation(jp);
        String methodName = op.get(OperationFields.METHOD_NAME, String.class);
        if ("resume".equals(methodName)) {
            op.put(OperationFields.METHOD_SIGNATURE, JoinPointBreakDown.getMethodStringFromArgs(methodName, Transaction.class));
        }
        return op;
    }
}

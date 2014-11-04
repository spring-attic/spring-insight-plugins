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

package com.springsource.insight.plugin.eclipse.persistence;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.eclipse.persistence.sessions.DatabaseSession;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public aspect TransactionOperationCollectionAspect extends EclipsePersistenceCollectionAspect {
    public TransactionOperationCollectionAspect() {
        super(EclipsePersistenceDefinitions.TX, "Transaction");
    }

    public pointcut collectionPoint()
            : execution(* DatabaseSession+.beginTransaction())
            || execution(* DatabaseSession+.commitTransaction())
            || execution(* DatabaseSession+.rollbackTransaction())
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature sig = jp.getSignature();
        String methodName = sig.getName(), actionName = getTransactionAction(methodName);
        return createOperation(jp, actionName);
    }

    static String getTransactionAction(String methodName) {
        if (StringUtil.isEmpty(methodName)) {
            return EclipsePersistenceDefinitions.UNKNOWN_ACTION;
        }

        int tPos = methodName.indexOf('T');
        if ((tPos <= 0) || (tPos >= (methodName.length() - 1))) { // if missing or is first or is last)
            return methodName;
        }

        return methodName.substring(0, tPos);
    }
}

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

package com.springsource.insight.plugin.eclipse.persistence;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import org.eclipse.persistence.sessions.DatabaseSession;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect TransactionOperationCollectionAspect extends MethodOperationCollectionAspect {
    public TransactionOperationCollectionAspect () {
        super();
    }

    @Override
    public String getPluginName() {
        return EclipsePersistenceDefinitions.PLUGIN_NAME;
    }

    public pointcut collectionPoint()
        : execution(* DatabaseSession+.beginTransaction())
       || execution(* DatabaseSession+.commitTransaction())
       || execution(* DatabaseSession+.rollbackTransaction())
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        String      methodName=sig.getName(), actionName=getTransactionAction(methodName);
        return super.createOperation(jp)
                    .type(EclipsePersistenceDefinitions.TX)
                    .label("Transaction " + actionName)
                    .put(EclipsePersistenceDefinitions.ACTION_ATTR, actionName)
                    ;
    }

    static String getTransactionAction (String methodName) {
        if ((methodName == null) || (methodName.length() <= 0)) {
            return "unknown";
        }
        
        int tPos=methodName.indexOf('T');
        if ((tPos <= 0) || (tPos >= (methodName.length() - 1))) { // if missing or is first or is last)
           return methodName;
        }

        return methodName.substring(0, tPos);
    }
}

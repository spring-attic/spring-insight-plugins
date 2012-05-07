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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract aspect JtaOperationCollectionAspect extends AbstractOperationCollectionAspect {
    protected final OperationType   opType;
    protected final Class<?>    txClass;

    protected JtaOperationCollectionAspect (OperationType type, Class<?> clazz) {
        if ((opType=type) == null) {
            throw new IllegalStateException("No operation type specified");
        }

        if ((txClass=clazz) == null) {
            throw new IllegalStateException("No object class specified");
        }
    }

    final OperationType getOperationType () {
        return opType;
    }

    final Class<?> getTransactionClass () {
        return txClass;
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        String      actionName=sig.getName();
        return new Operation().type(opType)
                              .label("Transaction " + actionName)
                              .sourceCodeLocation(getSourceCodeLocation(jp))
                              .put(OperationFields.CLASS_NAME, txClass.getName())
                              .put(OperationFields.METHOD_NAME, sig.getName())
                              .put(OperationFields.SHORT_CLASS_NAME, txClass.getSimpleName())
                               // same as JoinPointBreakDown#getMethodStringFromArgs
                              .put(OperationFields.METHOD_SIGNATURE, sig.getName() + "()")
                              .put(JtaDefinitions.ACTION_ATTR, actionName)
                              ;
    }

    @Override
    public String getPluginName() { return "jta"; }
}

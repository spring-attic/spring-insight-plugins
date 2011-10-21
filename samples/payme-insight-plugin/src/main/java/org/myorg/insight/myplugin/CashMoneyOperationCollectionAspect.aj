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

package org.myorg.insight.myplugin;

import org.aspectj.lang.JoinPoint;
import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * This aspect is responsible for intercepting calls to any code (library calls,
 * annotated methods, whatever) and creating an instance of {@link CashMoneyOperation} which
 * map to them.
 * 
 * In the case of this example, we will create a new instance of {@link CashMoneyOperation} 
 * whenever a method called setBalance(int) is called on any object.  
 * 
 * The {@link AbstractOperationCollectionAspect} does most of the heavy lifting in cases
 * like this, where we only depend on the arguments passed into the method.
 */
public aspect CashMoneyOperationCollectionAspect
    extends AbstractOperationCollectionAspect
{
    
    private static final OperationType TYPE = OperationType.valueOf("cash_money_operation");
    
    public pointcut collectionPoint()
        : execution(void *.setBalance(int));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Integer newBalance = (Integer)args[0];
        return new Operation()
            .type(TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .label("Cash Balance Set: " + newBalance)
            .put("newBalance", newBalance);
    }

    @Override
    public boolean isEndpoint() {
        return true; // Always include this frame in the trace
}
}

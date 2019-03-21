/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.cassandra;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Insert.Options;
import com.springsource.insight.intercept.operation.Operation;
import org.aspectj.lang.JoinPoint;

public aspect CassandraBuiltStatementOperationCollectionAspect extends OperationCollectionAspectSupport {

    public CassandraBuiltStatementOperationCollectionAspect() {
        super();
    }

    public pointcut collect()
    :  if (strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    ;

    pointcut builtStatementValue(Object arg1, Object arg2)
            : collect()
            && execution(Insert com.datastax.driver.core.querybuilder.BuiltStatement+.value(String,Object))
            && args(arg1,arg2)
            ;
    pointcut builtStatementValues(Object arg1, Object arg2)
            : collect()
            && execution(Insert com.datastax.driver.core.querybuilder.BuiltStatement+.values(String[],Object[]))
            && args(arg1,arg2)
            ;

//    pointcut insertStatementValue(Object arg1, Object arg2)
//            : collect()
//            && execution(Insert com.datastax.driver.core.querybuilder.Insert.value(String,Object))
//            && args(arg1,arg2)
//            ;
//    pointcut insertStatementValues(Object arg1, Object arg2)
//            : collect()
//            && execution(Insert com.datastax.driver.core.querybuilder.Insert.values(String[],Object[]))
//            && args(arg1,arg2)
//            ;
//    pointcut insertUsingStatementValue(Object arg1, Object arg2)
//            : collect()
//            && execution(Insert com.datastax.driver.core.querybuilder.Insert.Options.value(String,Object))
//            && args(arg1,arg2)
//            ;
//    pointcut insertUsingStatementValues(Object arg1, Object arg2)
//            : collect()
//            && execution(Insert com.datastax.driver.core.querybuilder.Insert.Options.value(String[],Object[]))
//            && args(arg1,arg2)
//            ;

//    after(Object arg1, Object arg2) returning(BuiltStatement result)
//            : ( insertStatementValue(arg1, arg2)
//            || insertUsingStatementValue(arg1, arg2)
//            || insertStatementValues(arg1, arg2)
//            || insertUsingStatementValues(arg1, arg2)
//
//            ) {

    after(Object arg1, Object arg2) returning(BuiltStatement result)
            : ( builtStatementValue(arg1, arg2)
            || builtStatementValues(arg1, arg2)) {

        Operation operation = CassandraOperationFinalizer.get(result);
        if (operation == null)
            operation = createOperationForStatement(thisJoinPoint, result);

        if (arg1 instanceof String[]) {
            String[] names = (String[]) arg1;
            Object[] values = (Object[])arg2;
            for(int i = 0; i < names.length; i++) {
                CassandraOperationFinalizer.addParam(operation, names[i], values[i]);
            }
        } else {
            CassandraOperationFinalizer.addParam(operation, (String)arg1, (Object)arg2);
        }
    }

    @Override
    public String getPluginName() {
        return CassandraRuntimePluginDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }

    private Operation createOperationForStatement(JoinPoint jp, BuiltStatement statement) {

        Operation operation = new Operation()
                .type(CassandraExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp));

        CassandraOperationFinalizer.put(statement, operation);

        return operation;
    }
}

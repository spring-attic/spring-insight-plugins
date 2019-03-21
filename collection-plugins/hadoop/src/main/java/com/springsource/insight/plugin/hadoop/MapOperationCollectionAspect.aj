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

package com.springsource.insight.plugin.hadoop;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for Hadoop Mapper 
 */
public privileged aspect MapOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public MapOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(public void org.apache.hadoop.mapred.Mapper+.map(..)) ||
            execution(protected void org.apache.hadoop.mapreduce.Mapper+.map(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();

        Operation operation = new Operation().type(OperationCollectionTypes.MAP_TYPE.type)
                .label(OperationCollectionTypes.MAP_TYPE.label)
                .sourceCodeLocation(getSourceCodeLocation(jp));

        operation.putAnyNonEmpty("key", args[0].toString());
        operation.putAnyNonEmpty("value", args[1].toString());

        if (args[2] instanceof Context) {
            @SuppressWarnings("rawtypes")
            Context ctx = (Context) args[2];
            Configuration config = ctx.getConfiguration();
            operation.putAnyNonEmpty("host", config.get("mapreduce.job.submithostaddress"));
        }

        return operation;
    }

    @Override
    public String getPluginName() {
        return HadoopPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}

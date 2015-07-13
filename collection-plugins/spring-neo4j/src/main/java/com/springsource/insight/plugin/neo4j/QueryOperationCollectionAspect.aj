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

package com.springsource.insight.plugin.neo4j;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import org.springframework.data.neo4j.support.Neo4jTemplate;
import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This aspect create insight operation for Neo4J Template execute/query()
 */
public privileged aspect QueryOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public QueryOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(* Neo4jTemplate+.execute(String, Map)) ||
            execution(* Neo4jTemplate+.query(String, Map, ..));

    @SuppressWarnings("unchecked")
    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String method = jp.getSignature().getName();

        Operation op = new Operation().type(OperationCollectionTypes.QUERY_TYPE.type)
                .label(OperationCollectionTypes.QUERY_TYPE.label + method)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("statement", (String) args[0]);

        OperationMap map = op.createMap("params");
        map.putAnyAll((Map<String, Object>) args[1]);

        Neo4jTemplate template = (Neo4jTemplate) jp.getTarget();
        Neo4JOperationCollectionSupport.addServiceInfo(template, op);

        return op;
    }

    @Override
    public String getPluginName() {
        return Neo4JPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

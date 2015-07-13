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

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import org.aspectj.lang.JoinPoint;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.data.neo4j.support.Neo4jTemplate;


public privileged aspect SaveOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public SaveOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(* Neo4jTemplate+.save(*,*))
            ;


    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Operation op = new Operation()
                .type(OperationCollectionTypes.SAVE_TYPE.type)
                .label(OperationCollectionTypes.SAVE_TYPE.label + jp.getSignature().getName())
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("entityClass", args[0].getClass().getSimpleName());
        if (args[1] != null) {
            RelationshipType rt = (RelationshipType)args[1];
            op.putAnyNonEmpty("relationship", rt.name());
        }


        Neo4jTemplate template = (Neo4jTemplate) jp.getTarget();
        Neo4JOperationCollectionSupport.addServiceInfo(template, op);
        return op;
    }
    @Override
    public String getPluginName() {
        return Neo4JPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

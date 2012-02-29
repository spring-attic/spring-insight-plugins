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

package com.springsource.insight.plugin.mongodb;

import java.util.List;

import org.aspectj.lang.JoinPoint;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;

public aspect MongoDbOperationCollectionAspect extends AbstractOperationCollectionAspect {

    public pointcut collectionPoint(): execution(CommandResult DB.command(..));

    @Override
    protected Operation createOperation(final JoinPoint jp) {
        Operation op = new Operation().label("MongoDB: DB." + jp.getSignature().getName() + "()").type(MongoDBOperationAnalyzer.TYPE);
        OperationList opList = op.createList("args");

        List<String> args = MongoArgumentUtils.toString(jp.getArgs());
        for (String arg : args) {
            opList.add(arg);
        }
        
        DB db = (DB) jp.getTarget();
        try {
        	op.put("dbName", db.getName());
			op.put("host", db.getMongo().getAddress().getHost());
			op.put("port", db.getMongo().getAddress().getPort());
		} catch (Exception e) {}
        
        return op;
    }
}

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

package com.springsource.insight.plugin.mongodb;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceOutput;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;

public aspect MongoCollectionOperationCollectionAspect extends AbstractOperationCollectionAspect {
	public MongoCollectionOperationCollectionAspect () {
		super();
	}

	public pointcut insertExecute(): 
		execution(WriteResult DBCollection.insert(DBObject[], WriteConcern));

    public pointcut updateExecute(): 
    	execution(WriteResult DBCollection.update(DBObject, DBObject, boolean, boolean));

    public pointcut removeExecute(): 
    	execution(WriteResult DBCollection.remove(DBObject, WriteConcern));

    public pointcut saveExecute():
    	execution(WriteResult DBCollection.save(..));

    public pointcut findExecute(): 
    	execution(* DBCollection.find*(..));

    public pointcut createIndexExecute(): 
    	execution(void DBCollection.createIndex(DBObject, DBObject));

    public pointcut getCountExecute(): 
    	execution(long DBCollection.getCount(DBObject, DBObject, long, long));

    public pointcut groupExecute(): 
    	execution(DBObject DBCollection.group(..));

    public pointcut distinctExecute(): 
    	execution(List DBCollection.distinct(String,DBObject));

    public pointcut mapReduceExecute(): 
    	execution(MapReduceOutput DBCollection.mapReduce(..));

    public pointcut dropIndexExecute(): 
    	execution(void DBCollection.dropIndexes(..));

    public pointcut collectionPoint():
        insertExecute() ||
        updateExecute() ||
        removeExecute() ||
        (findExecute() && !cflowbelow(findExecute()))||
        createIndexExecute() ||
        (saveExecute()  && !cflowbelow(saveExecute())) ||
        getCountExecute() ||
        (groupExecute() && !cflowbelow(groupExecute()))||
        distinctExecute() ||
        (mapReduceExecute() && !cflowbelow(mapReduceExecute())) ||
        dropIndexExecute()
        ;

    @Override
    protected Operation createOperation(final JoinPoint joinPoint) {
        final Signature signature = joinPoint.getSignature();
        final DBCollection collection = (DBCollection) joinPoint.getThis();
        Operation op = new Operation()
                .label("MongoDB: " + collection + "." + signature.getName())
                .type(MongoDBCollectionExternalResourceAnalyzer.TYPE)
                 .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(joinPoint))
                .put("collection", collection.getFullName());
        OperationList opList = op.createList("args");
        List<String> args = MongoArgumentUtils.toString(joinPoint.getArgs());
        for (String arg : args) {
            opList.add(arg);
        }
        
        try {
        	MongoArgumentUtils.putDatabaseDetails(op, collection.getDB());
		} catch (Exception e) {
			// ignored
		}

        return op;
    }

    @Override
    public String getPluginName() {
        return MongoDBPluginRuntimeDescriptor.PLUGIN_NAME;
    }
    
    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}

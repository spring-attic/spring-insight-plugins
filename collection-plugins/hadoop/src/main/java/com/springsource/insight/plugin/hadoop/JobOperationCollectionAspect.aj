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

package com.springsource.insight.plugin.hadoop;

import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;

/**
 * Collection operation for Hadoop job start 
 */
public privileged aspect JobOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public JobOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(public boolean org.apache.hadoop.mapreduce.Job.waitForCompletion(boolean));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Job job=(Job)jp.getTarget();
    	
		Operation operation = new Operation().type(OperationCollectionTypes.JOB_TYPE.type)
    						.label(OperationCollectionTypes.JOB_TYPE.label)
    						.sourceCodeLocation(getSourceCodeLocation(jp));

		operation.put("jobName", job.getJobName());
		
		try {
			operation.putAnyNonEmpty("mapper", getClassName(job.getMapperClass()));
			operation.putAnyNonEmpty("reducer", getClassName(job.getReducerClass()));
		
			operation.putAnyNonEmpty("outputFormat", getClassName(job.getOutputFormatClass()));
			operation.putAnyNonEmpty("inputFormat", getClassName(job.getInputFormatClass()));
		}
		catch (ClassNotFoundException e) {
			// ignore
		}
		
		Path[] inPaths=FileInputFormat.getInputPaths(job);
		if (ArrayUtil.length(inPaths) > 0) {
			OperationList list=operation.createList("inputPath");
			for (Path path: inPaths) {
				list.add(path.getName());
			}
		}

		Path outPath=FileOutputFormat.getOutputPath(job);
		if (outPath!=null) {
			operation.put("outputPath", outPath.getName());
		}
		
		operation.putAnyNonEmpty("mapperOutKey", getClassName(job.getMapOutputKeyClass()));
		operation.putAnyNonEmpty("mapperOutValue", getClassName(job.getMapOutputValueClass()));
		
		operation.putAnyNonEmpty("reducerOutKey", getClassName(job.getOutputKeyClass()));
		operation.putAnyNonEmpty("reducerOutValue", getClassName(job.getOutputValueClass()));

		// set configuration data
		Iterator<Map.Entry<String,String>> params=job.getConfiguration().iterator();
		if (params.hasNext()) {
			OperationMap confMap=operation.createMap("config");
			while(params.hasNext()) {
				Map.Entry<String,String> prop=params.next();
				confMap.put(prop.getKey(), prop.getValue());
			}
		}
		
		return operation;
    }
    
    private String getClassName(Class<?> claz) {
    	if (claz==null) {
    		return null;
    	}

    	return claz.getName();
    }
    
	@Override
    public String getPluginName() {
		return HadoopPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}

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

import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.JobConf;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Collection operation for Hadoop job start  
 */
public privileged aspect JobClientOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public JobClientOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(public RunningJob org.apache.hadoop.mapred.JobClient.runJob(JobConf)) ||
            execution(public RunningJob org.apache.hadoop.mapred.JobClient.submitJob(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();

        Operation operation = new Operation().type(OperationCollectionTypes.JOB_TYPE.type)
                .label(OperationCollectionTypes.JOB_TYPE.label)
                .sourceCodeLocation(getSourceCodeLocation(jp));

        Configuration config;
        if (args[0] instanceof JobConf) {
            JobConf jobConf = (JobConf) args[0];
            config = jobConf;

            operation.put("jobName", jobConf.getJobName());

            operation.putAnyNonEmpty("mapper", getClassName(jobConf.getMapperClass()));
            operation.putAnyNonEmpty("reducer", getClassName(jobConf.getReducerClass()));
            operation.put("mapper_tasks", jobConf.getNumMapTasks());
            operation.put("reducer_tasks", jobConf.getNumReduceTasks());

            operation.putAnyNonEmpty("inputFormat", getClassName(jobConf.getInputFormat().getClass()));
            Path[] inPaths = FileInputFormat.getInputPaths(jobConf);
            if (inPaths != null && inPaths.length > 0) {
                OperationList list = operation.createList("inputPath");
                for (Path path : inPaths) {
                    list.add(path.getName());
                }
            }
            operation.putAnyNonEmpty("outputFormat", getClassName(jobConf.getOutputFormat().getClass()));
            Path outPath = FileOutputFormat.getOutputPath(jobConf);
            if (outPath != null) {
                operation.put("outputPath", outPath.getName());
            }

            operation.putAnyNonEmpty("mapperOutKey", getClassName(jobConf.getMapOutputKeyClass()));
            operation.putAnyNonEmpty("mapperOutValue", getClassName(jobConf.getMapOutputValueClass()));
            operation.putAnyNonEmpty("reducerOutKey", getClassName(jobConf.getOutputKeyClass()));
            operation.putAnyNonEmpty("reducerOutValue", getClassName(jobConf.getOutputValueClass()));
        } else {
            //configuration from external config file
            config = ((JobClient) jp.getTarget()).getConf();

            operation.putAnyNonEmpty("jobConfigFile", args[0]);
        }

        // set configuration data
        Iterator<Map.Entry<String, String>> params = config.iterator();
        if (params.hasNext()) {
            OperationMap confMap = operation.createMap("config");
            while (params.hasNext()) {
                Map.Entry<String, String> prop = params.next();
                confMap.put(prop.getKey(), prop.getValue());
            }
        }

        return operation;
    }

    private String getClassName(Class<?> claz) {
        if (claz == null) {
            return null;
        }

        return claz.getName();
    }

    @Override
    public String getPluginName() {
        return HadoopPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

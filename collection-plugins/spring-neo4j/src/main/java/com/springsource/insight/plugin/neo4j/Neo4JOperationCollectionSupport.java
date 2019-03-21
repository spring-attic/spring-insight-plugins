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
package com.springsource.insight.plugin.neo4j;


import com.springsource.insight.intercept.operation.Operation;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.springframework.data.neo4j.support.Infrastructure;
import org.springframework.data.neo4j.support.Neo4jTemplate;

public class Neo4JOperationCollectionSupport {

    public static void addServiceInfo(Neo4jTemplate template, Operation op) {
        Infrastructure infrastructure = template.getInfrastructure();
        if (infrastructure != null) {
            GraphDatabaseService serv = infrastructure.getGraphDatabaseService();
            if (serv instanceof RestGraphDatabase) {
                op.put("service", ((RestGraphDatabase) serv).getRestAPI().getBaseUri());
            } else
                op.put("service", serv.toString());
        }

    }
}

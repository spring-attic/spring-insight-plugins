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

package com.springsource.insight.plugin.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;

/**
 * 
 */
public class RedisExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	public static final OperationType TYPE =  OperationType.valueOf("redis-client-method");
	private static final RedisExternalResourceAnalyzer	INSTANCE=new RedisExternalResourceAnalyzer();

	private RedisExternalResourceAnalyzer () {
		super(TYPE);
	}

	public static final RedisExternalResourceAnalyzer getInstance() {
		return INSTANCE;
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> dbFrames) {
		if (ListUtil.size(dbFrames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> dbDescriptors = new ArrayList<ExternalResourceDescriptor>(dbFrames.size());
		for (Frame dbFrame : dbFrames) {
			Operation op = dbFrame.getOperation();
			String host = op.get("host", String.class);
			int port = op.getInt("port", (-1));
			String color = colorManager.getColor(op);
			String dbName = op.get("dbName", String.class);
			
			String redisHash = MD5NameGenerator.getName(dbName+host+port);
			
			dbDescriptors.add(new ExternalResourceDescriptor(dbFrame,
					"redis:" + redisHash,
					dbName,
					ExternalResourceType.DATABASE.name(),
					"Redis",
					host,
					port,
                    color, false) );			
		}
		
		return dbDescriptors;
	}

}
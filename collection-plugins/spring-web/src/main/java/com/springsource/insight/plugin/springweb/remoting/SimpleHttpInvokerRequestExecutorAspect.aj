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

package com.springsource.insight.plugin.springweb.remoting;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.plugin.springweb.SpringWebPluginRuntimeDescriptor;

/**
 * This is a special aspect whose task is to add the forward coloring data
 * to the outgoing HTTP invocation request
 */
public privileged aspect SimpleHttpInvokerRequestExecutorAspect extends OperationCollectionAspectSupport {
	public SimpleHttpInvokerRequestExecutorAspect () {
		super();
	}

	public pointcut connectionPreparation()
	: execution(* org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor+.prepareConnection(HttpURLConnection,int))
	;

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around() throws IOException
	: connectionPreparation()
	&& (!cflowbelow(connectionPreparation()))	// using cflowbelow in case a derived class delegates to its parent
	&& if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
		OperationCollector  	opsCollector=getCollector();
		final HttpURLConnection	connection=(HttpURLConnection) thisJoinPoint.getArgs()[0];
		final Operation   	  	op=createOperation(thisJoinPoint, connection);

		opsCollector.enter(op);
		try {
			Object	returnValue=proceed();
			colorForward(new ColorParams() {
				public void setColor(String key, String value) {
					connection.setRequestProperty(key, value);
				}

				public Operation getOperation() {
					return op;
				}
			});
			updatePreparedConnectionDetails(op, connection);
			opsCollector.exitNormal();
			return returnValue;
		} catch(IOException e) {
			opsCollector.exitAbnormal(e);
			throw e;
		} catch(RuntimeException e) {
			opsCollector.exitAbnormal(e);
			throw e;
		}
	}

	Operation createOperation (JoinPoint jp, HttpURLConnection conn) {
		URL	url=conn.getURL();
		return OperationCollectionUtil.methodOperation(
				new Operation().type(HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER), jp)
				.put(OperationFields.URI, url.toExternalForm())
				.put(HttpInvokerRequestExecutorExternalResourceAnalyzer.DIRECT_CALL_ATTR, true)
				;
	}

	Operation updatePreparedConnectionDetails (Operation op, HttpURLConnection conn) {
		return op.put("method", conn.getRequestMethod());
	}

	@Override
	public String getPluginName() {
		return SpringWebPluginRuntimeDescriptor.PLUGIN_NAME;
	}

	@Override
	public boolean isMetricsGenerator() {
		return true; // This provides an external resource
	}
}

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

package com.springsource.insight.plugin.springweb.http;

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
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public privileged aspect SimpleClientHttpRequestFactoryCollectionAspect extends OperationCollectionAspectSupport {
	public SimpleClientHttpRequestFactoryCollectionAspect () {
		super();
	}

	public pointcut connectionPreparation()
	: execution(* org.springframework.http.client.SimpleClientHttpRequestFactory+.prepareConnection(HttpURLConnection,String))
	;

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around() throws IOException
	: connectionPreparation()
	&& (!cflowbelow(connectionPreparation()))	// using cflowbelow in case a derived class delegates to its parent
	&& if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
		OperationCollector  		opsCollector=getCollector();
		Object[]					args=thisJoinPoint.getArgs();
		final HttpURLConnection	connection=(HttpURLConnection) args[0];
		final Operation   	  	op=createOperation(thisJoinPoint, connection, (String) args[1]);

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

	Operation createOperation (JoinPoint jp, HttpURLConnection conn, String method) {
		URL		url=conn.getURL();
		String	urlValue=url.toExternalForm();
		return OperationCollectionUtil.methodOperation(
				new Operation().type(ClientHttpRequestExternalResourceAnalyzer.TYPE), jp)
				.label(createLabel(method, urlValue))
				.put(OperationFields.URI, urlValue)
				.put("method", method)
				;
	}

	static String createLabel (String method, String url) {
		return new StringBuilder(StringUtil.getSafeLength(method) + 1 + StringUtil.getSafeLength(url))
		.append(method.toUpperCase())
		.append(' ')
		.append(url)
		.toString();
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

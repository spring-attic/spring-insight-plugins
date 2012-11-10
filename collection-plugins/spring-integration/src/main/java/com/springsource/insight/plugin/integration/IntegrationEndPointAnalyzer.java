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
package com.springsource.insight.plugin.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.endpoint.AbstractEndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;

/**
 * {@link EndPointAnalyzer} for Spring Integration traces.
 * Integration 'Transformer' operations are never considered.
 *
 */
public class IntegrationEndPointAnalyzer extends AbstractEndPointAnalyzer {	
	// NOTE - order matters here!
	// the first ServiceActivator frame beats the first Gateway frame which beats the first Channel frame
	public static final List<OperationType>	SI_OPS=Collections.unmodifiableList(Arrays.asList(
			SpringIntegrationDefinitions.SI_OP_SERVICE_ACTIVATOR_TYPE, SpringIntegrationDefinitions.SI_OP_GATEWAY_TYPE, 
			SpringIntegrationDefinitions.SI_OP_CHANNEL_TYPE));
	private static final IntegrationEndPointAnalyzer INSTANCE = new IntegrationEndPointAnalyzer();
	public static final int ANALYSIS_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE + 2;

	private IntegrationEndPointAnalyzer () {
		super(SI_OPS);
	}

	public static final IntegrationEndPointAnalyzer getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected EndPointAnalysis makeEndPoint(Frame si, int depth) {
		Operation op = si.getOperation();
		String exampleRequest = op.get(SpringIntegrationDefinitions.SI_COMPONENT_TYPE_ATTR, String.class);
		String opLabel = op.getLabel();
		EndPointName name = EndPointName.valueOf(opLabel);
		String label = name.getName();
		return new EndPointAnalysis(name, label, exampleRequest, ANALYSIS_SCORE, op);
	}
}

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

package com.springsource.insight.plugin.springweb.validation;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.springweb.AbstractSpringWebEndPointAnalyzer;

/**
 * 
 */
public class ValidationEndPointAnalyzer extends AbstractSpringWebEndPointAnalyzer {
	private static final ValidationEndPointAnalyzer	INSTANCE=new ValidationEndPointAnalyzer();
	public static final int	VALIDATION_ENDPOINT_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE;

	private ValidationEndPointAnalyzer () {
		super(ValidationErrorsMetricsGenerator.TYPE);
	}

	public static final ValidationEndPointAnalyzer getInstance() {
		return INSTANCE;
	}

	@Override
    protected int getOperationScore(Operation op, int depth) {
		return VALIDATION_ENDPOINT_SCORE;
	}
}

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

package com.springsource.insight.plugin.springbatch;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;

public class SpringBatchEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
	public static final int	DEFAULT_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE;

    public SpringBatchEndPointAnalyzer() {
        super(SpringBatchDefinitions.BATCH_TYPE);
    }

    @Override
    public int getScore(Frame frame, int depth) {
    	if (validateScoringFrame(frame) == null) {
    		return EndPointAnalysis.MIN_SCORE_VALUE;
    	} else {
    		return DEFAULT_SCORE;
    	}
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation   op=frame.getOperation();
        return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), DEFAULT_SCORE, op);
    }
}

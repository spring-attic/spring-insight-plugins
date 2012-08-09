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
package com.springsource.insight.plugin.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.endpoint.AbstractEndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * Locates lifecycle endpoints within Traces.  
 * 
 * If a LifecycleOperation is detected in the root frame, an endpoint analysis 
 * will be returned. The score of the analysis will always be {@link LifecycleEndPointAnalyzer#ANALYSIS_SCORE}, and its endpoint
 * key/label will be based on the lifecycle event.
 */
public class LifecycleEndPointAnalyzer extends AbstractEndPointAnalyzer {
    /**
     * The <U>static</U> score assigned to the generated endpoints.
     * <B>Note:</B> we assign a rather high value since we want this endpoint
     * to &quot;trump&quot; others with high probability. However, it will
     * do so only if the <U>root</U> frame is a lifecycle one
     * @see #getScoringFrame(Trace)
     * @see #validateScoringFrame(Frame)
     */
    public static final int ANALYSIS_SCORE = 50;

    public static final OperationType SERVLET_LISTENER_TYPE = OperationType.valueOf("servlet-listener");
    public static final OperationType LIFECYCLE_TYPE_TYPE = OperationType.APP_LIFECYCLE;
    public static final EndPointName	ENDPOINT_NAME=EndPointName.valueOf("lifecycle");
    public static final String	ENDPOINT_LABEL="Lifecycle";
	/**
	 * The {@link List} of {@link OperationType}-s that mark a lifecycle frame
	 */
    public static final List<OperationType>	OPS=Collections.unmodifiableList(Arrays.asList(SERVLET_LISTENER_TYPE, LIFECYCLE_TYPE_TYPE));

    public LifecycleEndPointAnalyzer () {
    	super(OPS);
    }

    @Override
	public Frame getScoringFrame(Trace trace) {
    	Frame	root=trace.getRootFrame();
    	if (validateScoringFrame(root) == null) {
    		return null;
    	} else {
    		return root;
    	}
	}

	@Override
	protected int getDefaultScore(int depth) {
		return ANALYSIS_SCORE;
	}

	@Override
	protected OperationType validateScoringFrame(Frame frame) {
		if ((frame == null) || (!frame.isRoot())) {
			return null;
		} else {
			return super.validateScoringFrame(frame);
		}
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame lifecycleFrame, int depth) {
        Operation op = lifecycleFrame.getOperation();
        String endPointExample = op.get("event", String.class);

        return new EndPointAnalysis(ENDPOINT_NAME, ENDPOINT_LABEL, endPointExample, getOperationScore(op, depth), op);
    }
}

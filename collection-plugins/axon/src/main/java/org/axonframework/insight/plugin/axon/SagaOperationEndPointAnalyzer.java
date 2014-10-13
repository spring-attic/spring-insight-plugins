/**
 * Copyright (c) 2010-2012 Axon Framework All Rights Reserved.
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

package org.axonframework.insight.plugin.axon;

import static com.springsource.insight.intercept.operation.OperationFields.CLASS_NAME;
import static com.springsource.insight.intercept.operation.OperationFields.METHOD_NAME;
import static com.springsource.insight.intercept.operation.OperationFields.SHORT_CLASS_NAME;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;

/**
 * Analyzer for Axon Saga operations.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public class SagaOperationEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {

    private SagaOperationEndPointAnalyzer() {
        super(AxonOperationType.SAGA);
    }

    private static final SagaOperationEndPointAnalyzer INSTANCE = new SagaOperationEndPointAnalyzer();

    public static final SagaOperationEndPointAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation op = frame.getOperation();
        String example = "SAGA: " + op.get(SHORT_CLASS_NAME);
        EndPointName endPointName = EndPointName.valueOf(op.get(CLASS_NAME) + "#" + op.get(METHOD_NAME));

        return new EndPointAnalysis(endPointName, op.getLabel(), example, getOperationScore(op, depth), op);
    }

}

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

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;

/**
 * Common handling flow for Event- and CommandHandlers.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public abstract class AbstractHandlerEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {

    protected AbstractHandlerEndPointAnalyzer(OperationType handlerOp) {
        super(handlerOp);
    }


    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Frame busFrame = FrameUtil.getLastParentOfType(frame, getBusOperationType());
        if (busFrame == null) {
            return null;
        }

        Operation handlerOp = frame.getOperation();

        EndPointName endPointName = EndPointName.valueOf(
                handlerOp.get(CLASS_NAME) + "#" + handlerOp.get(METHOD_NAME));


        return new EndPointAnalysis(endPointName, handlerOp.getLabel(),
                getExample(busFrame.getOperation()), getOperationScore(handlerOp, depth), handlerOp);
    }


    abstract OperationType getBusOperationType();

    abstract String getExample(Operation operation);
}

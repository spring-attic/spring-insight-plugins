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

package com.springsource.insight.plugin.akka;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;

public class AkkaUntypedActorEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {

    private static final AkkaUntypedActorEndPointAnalyzer INSTANCE = new AkkaUntypedActorEndPointAnalyzer();

    public static final AkkaUntypedActorEndPointAnalyzer getInstance() {
        return INSTANCE;
    }

    protected AkkaUntypedActorEndPointAnalyzer() {
        super(AkkaDefinitions.OperationTypes.AKKA_OP_UNTYPED_ACTOR);
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation operation = frame.getOperation();
        EndPointName endPointName = EndPointName.valueOf(operation);
        String example = createExample(operation);
        return new EndPointAnalysis(endPointName, operation.getLabel(), example, getDefaultScore(depth));
    }

    private String createExample(Operation operation) {
        return operation.get(AkkaDefinitions.Labels.ACTOR, String.class) + " received message of type "
                + operation.get(AkkaDefinitions.Labels.MESSAGE, String.class);
    }
}

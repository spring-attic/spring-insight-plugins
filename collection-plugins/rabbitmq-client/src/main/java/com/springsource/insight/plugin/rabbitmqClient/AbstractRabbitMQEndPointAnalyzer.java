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

package com.springsource.insight.plugin.rabbitmqClient;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractRabbitMQEndPointAnalyzer implements EndPointAnalyzer {

    public AbstractRabbitMQEndPointAnalyzer() {
        super();
    }

    protected abstract String getExamplePrefix();

    protected abstract String getExchange(Operation op);

    protected abstract String getRoutingKey(Operation op);
    
    protected abstract OperationType getOperationType();

    public EndPointAnalysis locateEndPoint(Trace trace) {
        EndPointAnalysis analysis = null;
        Frame frame = trace.getFirstFrameOfType(getOperationType());
        
        if (frame != null) {
            Operation op = frame.getOperation();
            if (op != null) {
                String routingKey = getRoutingKey(op);
                String exchange = getExchange(op);
                
                
                String label = buildLabel(routingKey, exchange);
                String endPointLabel = "Rabbit-" + label;
                
                String example = getExample(label);
                EndPointName endPointName = getName(label);
                
                TimeRange responseTime = frame.getRange();
                return new EndPointAnalysis(responseTime, endPointName, endPointLabel, example, 0);
            }
        }
        
        return analysis;
    }

    private EndPointName getName(String label) {
        return EndPointName.valueOf(label);
    }

    private String getExample(String label) {
        return getExamplePrefix()+label;
    }

    private String buildLabel(String routingKey, String exchange) {
        StringBuilder sb = new StringBuilder();
        boolean hasExchange = hasValue(exchange);
        
        if (hasExchange) {
            sb.append("Exchange#").append(exchange);
        } 
           
        if (hasValue(routingKey)) {
            if (hasExchange) {
                sb.append(" ");
            }
            sb.append("RoutingKey#").append(routingKey);
        }
        
        return sb.toString();
    }
    
    private boolean hasValue(String str) {
        return str != null && str.trim().length() > 0;
    }

}
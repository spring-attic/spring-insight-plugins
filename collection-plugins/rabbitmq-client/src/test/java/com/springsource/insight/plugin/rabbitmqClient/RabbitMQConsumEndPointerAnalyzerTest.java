/**
 * Copyright 2009-2010 the original author or authors.
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

import static org.junit.Assert.*;

import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class RabbitMQConsumEndPointerAnalyzerTest {
    
    @Test
    public void testExchangeLocateEndPoint() {
        Operation op = createOperation();
        
        OperationMap map = op.createMap("envelope");
        
        map.putAnyNonEmpty("deliveryTag", 1l);
        map.putAnyNonEmpty("exchange", "e");
        
        RabbitMQConsumEndPointerAnalyzer analyzer = new RabbitMQConsumEndPointerAnalyzer();
        
        Trace trace = createValidTrace(op);
        
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        
        String name = analysis.getEndPointName().getName();
        String example = analysis.getExample();
        String lbl = analysis.getResourceLabel();
        int score = analysis.getScore();
        
        assertEquals("Exchange#e", name);
        assertEquals(analyzer.getExamplePrefix()+"Exchange#e", example);
        assertEquals("Rabbit-Exchange#e", lbl);
        assertEquals(0, score);
    }
    
    @Test
    public void testRoutingKeyLocateEndPoint() {
        Operation op = createOperation();
        
        OperationMap map = op.createMap("envelope");
        
        map.putAnyNonEmpty("deliveryTag", 1l);
        map.putAnyNonEmpty("routingKey", "rk");
        
        RabbitMQConsumEndPointerAnalyzer analyzer = new RabbitMQConsumEndPointerAnalyzer();
        
        Trace trace = createValidTrace(op);
        
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        
        String name = analysis.getEndPointName().getName();
        String example = analysis.getExample();
        String lbl = analysis.getResourceLabel();
        int score = analysis.getScore();
        
        assertEquals("RoutingKey#rk", name);
        assertEquals(analyzer.getExamplePrefix()+"RoutingKey#rk", example);
        assertEquals("Rabbit-RoutingKey#rk", lbl);
        assertEquals(0, score);
    }
    
    @Test
    public void testBothLocateEndPoint() {
        Operation op = createOperation();
        
        OperationMap map = op.createMap("envelope");
        
        map.putAnyNonEmpty("deliveryTag", 1l);
        map.putAnyNonEmpty("routingKey", "rk");
        map.putAnyNonEmpty("exchange", "e");
        
        RabbitMQConsumEndPointerAnalyzer analyzer = new RabbitMQConsumEndPointerAnalyzer();
        
        Trace trace = createValidTrace(op);
        
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        
        String name = analysis.getEndPointName().getName();
        String example = analysis.getExample();
        String lbl = analysis.getResourceLabel();
        int score = analysis.getScore();
        
        assertEquals("Exchange#e RoutingKey#rk", name);
        assertEquals(analyzer.getExamplePrefix()+"Exchange#e RoutingKey#rk", example);
        assertEquals("Rabbit-Exchange#e RoutingKey#rk", lbl);
        assertEquals(0, score);
    }

    Operation createOperation() {
        Operation op = new Operation()
                        .type(OperationType.valueOf("rabbitmq-client-consumer"))
                        .label("Consume");
        return op;
    }
    
    private Trace createValidTrace(Operation op) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        
        builder.enter(op);
        
        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }

}

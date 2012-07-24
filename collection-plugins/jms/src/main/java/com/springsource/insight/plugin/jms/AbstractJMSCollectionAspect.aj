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
package com.springsource.insight.plugin.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.errorhandling.CollectionErrors;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

public abstract aspect AbstractJMSCollectionAspect extends OperationCollectionAspectSupport {
	protected final JMSPluginOperationType optype;

    protected AbstractJMSCollectionAspect (JMSPluginOperationType type) {
    	if ((optype=type) == null) {
    		throw new IllegalStateException("No operation type specified");
    	}
    }
    
    Operation createOperation(JoinPoint jp) {
        Operation op = new Operation();
        op.type(optype.getOperationType());
        op.label(optype.getLabel());
        op.sourceCodeLocation(getSourceCodeLocation(jp));
        
        return op;
    }

    Operation applyDestinationData(Message message, Operation op) {
        try {
            return applyDestinationData(message.getJMSDestination(), op);
        } catch (JMSException e) {
            CollectionErrors.markCollectionError(this.getClass(), e);
            return op;
        }
    }
    
    Operation applyDestinationData(Destination dest, Operation op) {
        try {
            DestinationType destinationType = JMSPluginUtils.getDestinationType(dest);
            String destinationName = JMSPluginUtils.getDestinationName(dest, destinationType);
            return applyDestinationData(op, destinationType, destinationName);
        } catch (JMSException e) {
            CollectionErrors.markCollectionError(this.getClass(), e);
            return op;
        }
    }
    
    Operation applyMessageData(Message message, Operation op) {
        try {
            JMSPluginUtils.extractMessageTypeAttributes(op, message);
            JMSPluginUtils.extractMessageHeaders(op, message);
            JMSPluginUtils.extractMessageProperties(op, message);
        } catch (JMSException e) {
            CollectionErrors.markCollectionError(this.getClass(), e);
        }

        return op;
    }

    static Operation applyDestinationData (Operation op, DestinationType destinationType, String destinationName) {
        op.put(OperationFields.CLASS_NAME, destinationType.name());
        op.put(OperationFields.METHOD_SIGNATURE, destinationName);
        
        op.put("destinationType", destinationType.name());
        op.put("destinationName", destinationName);
        return op;
    }

    @Override
    public boolean isEndpoint() {
    	return true;
    }

    @Override
    public String getPluginName() {
        return JmsPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

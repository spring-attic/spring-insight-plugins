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
import javax.jms.MessageProducer;

import com.springsource.insight.collection.errorhandling.CollectionErrors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;

public aspect JMSProducerCollectionAspect extends AbstractJMSCollectionAspect {
    private static final MessageOperationMap map = new MessageOperationMap();
    public JMSProducerCollectionAspect () {
        super();
    }

    public pointcut producer()
        : execution(void javax.jms.MessageProducer+.send(..))
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

	@SuppressAjWarnings({"adviceDidNotMatch"})
    before() : producer() {
        try {
            JoinPoint jp = thisJoinPoint; 
            final Message message = JMSPluginUtils.getMessage(jp.getArgs());
            if (message != null) {
                MessageWrapper wrapper = MessageWrapper.instance(message);
                final Operation op = map.get(wrapper);
                
                //check that we didn't enter the frame for this message
                //if so don't enter again
                if (op == null) {
                
                    Operation op1 = createOperation(jp);
                    applyDestinationData(jp, op1);
                
                    map.put(wrapper, op1, jp.toLongString());
                
                    getCollector().enter(op1);
                    
                    colorForward(new ColorParams() {
                        public void setColor(String key, String value) {
                            try {
                                message.setStringProperty(key, value);
                            } catch (JMSException e) {
                                //Nothing we can do, can't color forward
                            }
                        }

                        public Operation getOperation() {
                            return op;
                        }
                    });
                }
            }
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this.getClass(), t);
        }
    }
    
	@SuppressAjWarnings({"adviceDidNotMatch"})
    after() : producer() {
        try {
            JoinPoint jp = thisJoinPoint;
            Message message = JMSPluginUtils.getMessage(jp.getArgs());
        
            if (message != null) {
                MessageWrapper wrapper = MessageWrapper.instance(message);
                Operation op = map.get(wrapper);
                if (op != null) {
                    //check that the operation "origin" is this joint point
                    if (map.isRelevant(jp.toLongString(), op)) {
                        applyMessageData(message, op);
                        map.remove(wrapper);
                    
                        getCollector().exitNormal();
                    }
                }
            }
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this.getClass(), t);
        }
    }

	@SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception) : producer() {
        try {
            Message message = JMSPluginUtils.getMessage(thisJoinPoint.getArgs());
        
            if (message != null) {
                MessageWrapper wrapper = MessageWrapper.instance(message);
                map.remove(wrapper);
            }
        
            getCollector().exitAbnormal(exception);
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this.getClass(), t);
        }
    }
    
    @Override
    JMSPluginOperationType getOperationType() {
        return JMSPluginOperationType.SEND;
    }
    
    private void applyDestinationData(JoinPoint jp, Operation op) {
        try {
            Destination dest = ((MessageProducer) jp.getThis()).getDestination();
            applyDestinationData(dest, op);
        } catch (JMSException e) {
            // ignored - TODO consider logging
        }
    }
}

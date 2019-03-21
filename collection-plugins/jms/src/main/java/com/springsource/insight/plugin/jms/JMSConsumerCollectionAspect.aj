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
package com.springsource.insight.plugin.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.color.ColorManager.ExtractColorParams;
import com.springsource.insight.intercept.operation.Operation;

public aspect JMSConsumerCollectionAspect extends AbstractJMSCollectionAspect {
    public JMSConsumerCollectionAspect() {
        super(JMSPluginOperationType.RECEIVE);
    }

    public pointcut consumer()
            : execution(Message javax.jms.MessageConsumer+.receive*(..))
            && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart))
            ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(final Message message): consumer() {
        if (message != null) {
            JoinPoint jp = thisJoinPoint;

            Operation op = createOperation(jp);
            try {
                applyAdditionalData(op, jp);
                applyDestinationData(message, op);
                applyMessageData(message, op);
            } catch (Throwable t) {
                markException("afterReturning", t);
            }

            //Set the color for this frame
            extractColor(new ExtractColorParams() {
                public String getColor(String key) {
                    try {
                        return message.getStringProperty(key);
                    } catch (JMSException e) {
                        return null;
                    }
                }
            });

            OperationCollector collector = getCollector();
            //we enter and exit cause we want to ignore null messages
            //for now there is no way to discard a frame once it was entered
            collector.enter(op);
            collector.exitNormal(message);
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception): consumer() {
        Operation op = createOperation(thisJoinPoint);
        OperationCollector collector = getCollector();
        collector.enter(op);
        // we enter and exit since there is no "before" clause
        collector.exitAbnormal(exception);
    }

    private Operation applyAdditionalData(Operation op, JoinPoint jp) {
        try {
            MessageConsumer consumer = (MessageConsumer) jp.getThis();
            String selector = consumer.getMessageSelector();
            op.putAnyNonEmpty("selector", selector);
        } catch (JMSException e) {
            markException("applyAdditionalData", e);
        }

        return op;
    }
}

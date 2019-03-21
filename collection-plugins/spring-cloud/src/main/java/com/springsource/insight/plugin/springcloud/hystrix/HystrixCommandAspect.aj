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

package com.springsource.insight.plugin.springcloud.hystrix;


import com.netflix.hystrix.*;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.*;
import com.springsource.insight.plugin.springcloud.SpringCloudOperationCollector;
import com.springsource.insight.plugin.springcloud.SpringCloudOperationSupport;
import com.springsource.insight.plugin.springcloud.SpringCloudPluginRuntimeDescriptor;
import com.springsource.insight.util.StringUtil;
import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.List;
import java.util.logging.Level;

public aspect HystrixCommandAspect extends SpringCloudOperationSupport {

    public HystrixCommandAspect() {
        super(new SpringCloudOperationCollector());
    }

    public pointcut executePoint() :
        execution(* com.netflix.hystrix.HystrixCommand+.execute()) &&
            if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    public pointcut queuePoint() :
        execution(* com.netflix.hystrix.HystrixCommand+.queue()) &&
            !cflowbelow(executePoint()) &&
            if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    public pointcut toObservablePoint() :
        execution(* com.netflix.hystrix.AbstractCommand+.toObservable()) &&
            !cflowbelow(queuePoint()) && !cflowbelow(executePoint()) &&
            if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    public pointcut commandCallPoint() :
        execution(void com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction+.call())
        ;

    public pointcut invokePoint() :
        commandCallPoint() &&
            !cflowbelow(commandCallPoint()) &&
            if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    public pointcut metricsPoint() :
        execution(void com.netflix.hystrix.HystrixCommandMetrics+.mark*(..))
        ;

    @SuppressAjWarnings("adviceDidNotMatch")
    after() : metricsPoint() {
        fillInCommandKeyIfPossible(thisJoinPoint);
        fillInEventsIfPossible(thisJoinPoint);
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    before() : invokePoint() {
        Operation operation = null;
        try {
            operation = createOperation(thisJoinPoint);
        } catch (Exception e) {
           operation = fillInException(thisJoinPoint, e);
        }

        getCollector().enter(operation);
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    after() returning(Object returnValue) : invokePoint() {

        if (((MethodSignature) thisJoinPointStaticPart.getSignature()).getReturnType() == void.class) {
            getCollector().exitNormal();
        } else {
            getCollector().exitNormal(returnValue);
        }
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    after() throwing(Throwable exception)  : invokePoint() {
        getCollector().exitAbnormal(exception);
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    before() : executePoint()  || queuePoint() || toObservablePoint() {
        Operation operation = null;
        try {
            operation = createOperation(thisJoinPoint);
        } catch (Exception e) {
            operation = fillInException(thisJoinPoint, e);
        }

        getCollector().enter(operation);
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    after() returning(Object returnValue) : executePoint()  || queuePoint() || toObservablePoint() {

        fillInEventsIfPossible(thisJoinPoint);
        // We want to capture difference between not having a return value and having a null return value
        if (((MethodSignature) thisJoinPointStaticPart.getSignature()).getReturnType() == void.class) {
            getCollector().exitNormal();
        } else {
            getCollector().exitNormal(returnValue);
        }
    }

    @SuppressAjWarnings("adviceDidNotMatch")
    after() throwing(Throwable exception)  : executePoint()  || queuePoint() || toObservablePoint() {

        fillInEventsIfPossible(thisJoinPoint);
        getCollector().exitAbnormal(exception);
    }

    private Operation createOperation(JoinPoint jp) {
        Operation operation = OperationCollectionUtil.methodOperation(new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND), jp);
        populateOperation(operation, jp);
        return operation;

    }

    private void populateOperation(Operation op, JoinPoint jp) {

        Object aThis = jp.getThis();
        if (aThis instanceof HystrixCommand) {
            String commandKey = "";
            HystrixCommand hystrixCommand = (HystrixCommand) aThis;

            HystrixCommandGroupKey groupKey = hystrixCommand.getCommandGroup();
            if (groupKey != null) {
                op.putAnyNonEmpty("commandGroup", groupKey.name());
            }
            HystrixCommandKey key = hystrixCommand.getCommandKey();
            if (key != null) {
                commandKey = key.name();
                op.putAnyNonEmpty("commandKey", commandKey);
            }

            op.put("circuitBreakerOpen", hystrixCommand.isCircuitBreakerOpen());
            op.put("isolationStrategy", hystrixCommand.getProperties().executionIsolationStrategy().get().toString());
            op.label("HystrixCommand " +   jp.getSignature().getName() + " : " + commandKey);
        } else {
            op.label("HystrixCommand run");
        }

    }

    private void fillInCommandKeyIfPossible(JoinPoint jp) {
        SpringCloudOperationCollector collector = (SpringCloudOperationCollector)getCollector();
        Operation operation = collector.getBuilder().peek();
        if (operation != null && operation.getType() == SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND) {
            Object o = jp.getThis();
            if (o instanceof HystrixCommandMetrics) {
                HystrixCommandMetrics metrics = (HystrixCommandMetrics)o;
                HystrixCommandGroupKey groupKey = metrics.getCommandGroup();
                if (groupKey != null) {
                    operation.putAnyNonEmpty("commandGroup", groupKey.name());
                }
                HystrixCommandKey key = metrics.getCommandKey();
                if (key != null) {
                    operation.putAnyNonEmpty("commandKey", key.name());
                }
            }
        }
    }

    private void fillInEventsIfPossible(JoinPoint jp) {

        SpringCloudOperationCollector collector = (SpringCloudOperationCollector)getCollector();
        Operation operation = collector.getBuilder().peek();
        if (operation != null && operation.getType() == SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND) {
            Object o = jp.getThis();
            if (o instanceof  HystrixCommand) {
                HystrixCommand hystrixCommand = (HystrixCommand) jp.getThis();
                fillInEvents(operation, hystrixCommand);
            } else if (o instanceof HystrixCommandMetrics) {
                HystrixCommandMetrics hystrixCommandMetrics = (HystrixCommandMetrics) jp.getThis();
                fillInEvents(operation, jp.getSignature().getName());
            }
        } else {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Ignoring event: " + jp.getSignature().toString() );
            }
        }
    }

    private void fillInEvents(Operation operation, String metricMethodName) {
        HystrixEventType event = mapToEventType(metricMethodName);
        if (event == null)
            return;

        String newEvent = event.toString();
        addNewEvent(operation, newEvent);

    }

    private void addNewEvent(Operation operation, String newEvent) {
        String events = getEvents(operation);
        if (!events.contains(newEvent)) {
            if (StringUtil.isEmpty(events))
                putEvents(operation, newEvent);
            else {
                events = events + "," + newEvent;
                putEvents(operation, events);
            }
        }
    }


    private HystrixEventType mapToEventType(String metricMethodName) {

        if ("markSuccess".equals(metricMethodName)) {
            return HystrixEventType.SUCCESS;
        }
        if ("markFailure".equals(metricMethodName)) {
            return HystrixEventType.FAILURE;
        }
        if ("markTimeout".equals(metricMethodName)) {
            return HystrixEventType.TIMEOUT;
        }
        if ("markShortCircuited".equals(metricMethodName)) {
            return HystrixEventType.SHORT_CIRCUITED;
        }
        if ("markThreadPoolRejection".equals(metricMethodName)) {
            return HystrixEventType.THREAD_POOL_REJECTED;
        }
        if ("markSemaphoreRejection".equals(metricMethodName)) {
            return HystrixEventType.SEMAPHORE_REJECTED;
        }
        if ("markBadRequest".equals(metricMethodName)) {
            return HystrixEventType.BAD_REQUEST;
        }
        if ("markFallbackSuccess".equals(metricMethodName)) {
            return HystrixEventType.FALLBACK_SUCCESS;
        }
        if ("markFallbackFailure".equals(metricMethodName)) {
            return HystrixEventType.FALLBACK_FAILURE;
        }
        if ("markFallbackRejection".equals(metricMethodName)) {
            return HystrixEventType.FALLBACK_REJECTION;
        }
        if ("markExceptionThrown".equals(metricMethodName)) {
            return HystrixEventType.EXCEPTION_THROWN;
        }
        if ("markCollapsed".equals(metricMethodName)) {
            return HystrixEventType.COLLAPSED;
        }
        return null;
    }

    private void fillInEvents(Operation operation, HystrixCommand command) {

        List<HystrixEventType> hevents = command.getExecutionEvents();

        for(HystrixEventType eventType: hevents) {
            String newEvent = eventType.toString();
            addNewEvent(operation, newEvent);
        }
    }

    private String getEvents(Operation operation) {
        String events = operation.get("events", String.class);
        if (events == null)
            return "";
        return events;
    }

    private void putEvents(Operation operation, String events) {
        operation.put("events", events);
    }
}

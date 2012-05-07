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
package com.springsource.insight.collection;

import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * This aspect differs from the {@link AbstractOperationCollectionAspect} by
 * the fact that while the {@link Operation} is generated in the <code>before</code>
 * advice, it is added to the collector only if non-<code>null. Otherwise a special
 * &quot;placeholder&quot; is used to signal the <code>after</code> advice <U>not</U>
 * to call {@link OperationCollector} <code>exit</code> methods (since the
 * <code>before</code> advice does not call the {@link OperationCollector#enter(Operation)}
 * if <code>null</code> {@link Operation} is returned by {@link #createOperation(JoinPoint)})
 *
 * TODO: We need to either propmote this to a general colleciton aspect, or remove it.
 */
public abstract aspect TrailingAbstractOperationCollectionAspect
        extends OperationCollectionAspectSupport {
    protected TrailingAbstractOperationCollectionAspect () {
        super();
    }

    protected TrailingAbstractOperationCollectionAspect(OperationCollector operationCollector) {
        super(operationCollector);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before () : collectionPoint() {
        final Operation             op=createOperation(thisJoinPoint);
        final OperationCollector    collector=getEnterCollector(op);
        if (collector != null) {
            collector.enter(op);
            push(op);
        } else {
            push(NULL_PLACEHOLDER);
        }
    }

    // returns null if {@link OperationCollector#enter(Operation)} should not be called
    protected OperationCollector getEnterCollector (Operation op) {
        if (op == null) {
            return null;
        }

        return getCollector();
    }
    /**
     * Inform the metric collector that a method has returned
     */
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(Object returnValue) : collectionPoint() {
        final OperationCollector    collector=getExitCollector(thisJoinPoint, returnValue, null);
        if (collector == null) {    // no operation created
            return;
        }

        // We want to capture difference between not having a return value and having a null return value
        if (getReturnType(thisJoinPointStaticPart.getSignature()) == void.class) {
            collector.exitNormal();
        } else {
            collector.exitNormal(returnValue);
        }
    }
    /**
     * Inform the metric collector that a method has thrown an exception
     */
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception) : collectionPoint() {
        final OperationCollector    collector=getExitCollector(thisJoinPoint, null, exception);
        if (collector == null) {    // no operation created
            return;
        }
        collector.exitAbnormal(exception);
    }

    // returns null if the OperationCollector exit method(s) should not be called
    protected OperationCollector getExitCollector(JoinPoint jp, Object returnValue, Throwable exception) {
        return getExitCollector(pop(), jp, returnValue, exception);
    }

    protected OperationCollector getExitCollector(Operation op,JoinPoint jp, Object returnValue, Throwable exception) {
        if (op == NULL_PLACEHOLDER) {
            return null;    // debug breakpoint
        }

        return getCollector();
    }
    /**
     * Used to replace a <code>null</code> {@link Operation} returned by
     * the call to {@link #createOperation(JoinPoint)}
     */
    protected static final Operation    NULL_PLACEHOLDER=new Operation().type(OperationType.valueOf("null"));
    private static final ThreadLocal<Stack<Operation>>  _pendingOperations=new ThreadLocal<Stack<Operation>>() {
            /*
             * @see java.lang.ThreadLocal#initialValue()
             */
            @Override
            protected Stack<Operation> initialValue() {
                return new Stack<Operation>();
            }
        };
    private static final void push (Operation op) {
        final Stack<Operation>  opers=_pendingOperations.get();
        opers.push(op);
    }

    private static final Operation pop () {
        final Stack<Operation>  opers=_pendingOperations.get();
        return opers.pop();
    }
    /**
     * @return The last {@link Operation} created by the <code>before</code>
     * advice - may be <code>null</code> if no pending operation or the
     * {@link #NULL_PLACEHOLDER} if the original {@link #createOperation(JoinPoint)}
     * returned <code>null</code>
     */
    protected static final Operation getPendingOperation () {
        final Stack<Operation>  opers=_pendingOperations.get();
        if (opers.isEmpty()) {
            return null;
        }
        return opers.peek();
    }

    // TODO move it to some 'util' class/JAR
    public static final Class<?> getReturnType (final Signature sig) {
        if (sig == null) {
            return null;
        }

        if (sig instanceof MethodSignature) {
            return ((MethodSignature) sig).getReturnType();
        }

        // usually a constructor...
        return sig.getDeclaringType();
    }
    /**
     * @param The intercepted {@link JoinPoint}
     * @return The {@link Operation} which represents the given
     * {@link JoinPoint} - ignored if <code>null</code>
     */
    protected abstract Operation createOperation(JoinPoint jp);
    /**
     * Overridden by subclass to specify a pointcut that we will interact with.
     */
    public abstract pointcut collectionPoint();

}

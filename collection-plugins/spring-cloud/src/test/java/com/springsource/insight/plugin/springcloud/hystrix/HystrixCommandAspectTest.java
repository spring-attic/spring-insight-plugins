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

package com.springsource.insight.plugin.springcloud.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class HystrixCommandAspectTest extends OperationCollectionAspectTestSupport {

    @Test
    public void testExecute() {
        CommandHelloWorld testCommand = new CommandHelloWorld("Test");
        testCommand.execute();
        //System.out.println(Thread.currentThread().getName());
        Operation op = getLastEntered();
        assertNotNull(op);
        //System.out.println(op.getLabel());
        assertNotNull(op.get("events"));
        assertEquals("ExampleGroup", op.get("commandGroup"));
        assertEquals("CommandHelloWorld", op.get("commandKey"));
    }

    @Test
    public void testQueue() throws ExecutionException, InterruptedException {
        CommandHelloWorld testCommand = new CommandHelloWorld("Test");
        Future<String> queue = testCommand.queue();
        queue.get();
        //System.out.println(Thread.currentThread().getName());
        Operation op = getLastEntered();
        assertNotNull(op);
        //System.out.println(op.getLabel());
        assertNotNull(op.get("events"));
        assertEquals("ExampleGroup", op.get("commandGroup"));
        assertEquals("CommandHelloWorld", op.get("commandKey"));
    }


    private boolean testToObservableComplete;

    @Test
    public void testToObservable() throws ExecutionException, InterruptedException {
        CommandHelloWorld testCommand = new CommandHelloWorld("Test");
        Observable<String> observable = testCommand.toObservable();

        testToObservableComplete = false;
        Subscription subscribe = observable.subscribe(new Observer<String>() {
            public void onCompleted() {
                testToObservableComplete = true;
            }

            public void onError(Throwable e) {
            }

            public void onNext(String s) {
            }
        });

        while(!testToObservableComplete) {
            Thread.sleep(20);
        }

        Operation op = getLastEntered();
        assertNotNull(op);
        assertNotNull(op.get("events"));
        assertEquals("ExampleGroup", op.get("commandGroup"));
        assertEquals("CommandHelloWorld", op.get("commandKey"));
    }



    @Override
    public OperationCollectionAspectSupport getAspect() {
        return HystrixCommandAspect.aspectOf();
    }

    @Override
    protected Operation getLastEnteredOperation(OperationCollector spiedCollector) {
        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedCollector, atLeastOnce()).enter(opCaptor.capture());
        Operation capturedOp = opCaptor.getValue();
        // verifyCapturedOpHasSourceCodeProperties(capturedOp);
        verifyCapturedOpHasSourceCodeLocation(capturedOp);
        return opCaptor.getValue();
    }

    static class CommandHelloWorld extends HystrixCommand<String> {

        private final String name;

        public CommandHelloWorld(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }

        @Override
        protected String run() {
            // a real example would do work like a network call here
            return "Hello " + name + "!";
        }
    }


}
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
package com.springsource.insight.plugin.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class Slf4jLoggingOperationCollectionAspectTest
        extends LoggingMethodOperationCollectionAspectTestSupport {

    public Slf4jLoggingOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testLogErrorMessage () {
       String   msg="testLogErrorMessage";
       Logger   logger=LoggerFactory.getLogger(getClass());
       logger.error(msg);
       assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Test
    public void testLogErrorMessageWithException () {
       String       msg="testLogErrorMessageWithException";
       Logger       logger=LoggerFactory.getLogger(getClass());
       Throwable    t=new IllegalArgumentException(msg);
       logger.error(msg, t);
       assertLoggingOperation(logger, "ERROR", msg, t);
    }

    @Test
    public void testLogFormatError () {
       String   msg="testLogFormatError: %d";
       Logger   logger=LoggerFactory.getLogger(getClass());
       logger.error(msg, Integer.valueOf((int) System.nanoTime()));
       assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Test
    public void testLogFormatErrorWith2Values () {
       String   msg="testLogFormatErrorWith2Values: %d/%d";
       Logger   logger=LoggerFactory.getLogger(getClass());
       logger.error(msg, Integer.valueOf((int) System.currentTimeMillis()), Integer.valueOf((int) System.nanoTime()));
       assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Test
    public void testLogFormatErrorWithArray () {
       String   msg="testLogFormatErrorWithArray: %d/%d";
       Logger   logger=LoggerFactory.getLogger(getClass());
       Object[] args={ Integer.valueOf((int) System.currentTimeMillis()), Integer.valueOf((int) System.nanoTime()) };
       logger.error(msg, args);
       assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Override
    public Slf4jLoggingOperationCollectionAspect getAspect() {
        return Slf4jLoggingOperationCollectionAspect.aspectOf();
    }

    private Operation assertLoggingOperation (Logger logger, String level, String msg, Throwable t) {
        Operation   op=assertLoggingOperation(Logger.class, level, msg, t);
        assertEquals("Mismatched logger name", logger.getName(), op.get(LoggingDefinitions.NAME_ATTR));
        return op;
    }

}

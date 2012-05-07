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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;


/**
 * 
 */
public class Log4jLoggingOperationCollectionAspectTest
        extends LoggingMethodOperationCollectionAspectTestSupport {

    public Log4jLoggingOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testLogErrorMessage () {
       String   msg="testLogErrorMessage";
       Logger   logger=Logger.getLogger(getClass());
       logger.error(msg);
       assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Test
    public void testLogErrorMessageWithException () {
       String       msg="testLogErrorMessageWithException";
       Logger       logger=Logger.getLogger(getClass());
       Throwable    t=new IllegalArgumentException(msg);
       logger.error(msg, t);
       assertLoggingOperation(logger, "ERROR", msg, t);
    }

    @Test
    public void testFatalErrorMessage () {
       String   msg="testFatalErrorMessage";
       Logger   logger=Logger.getLogger(getClass());
       logger.fatal(msg);
       assertLoggingOperation(logger, "FATAL", msg, null);
    }

    @Test
    public void testFatalErrorMessageWithException () {
       String       msg="testFatalErrorMessageWithException";
       Logger       logger=Logger.getLogger(getClass());
       Throwable    t=new IllegalArgumentException(msg);
       logger.fatal(msg, t);
       assertLoggingOperation(logger, "FATAL", msg, t);
    }

    @Test
    public void testIndirectErrorLogMessage () {
        String   msg="testIndirectErrorLogMessage";
        Logger   logger=Logger.getLogger(getClass());
        logger.log(Level.ERROR, msg);
        assertLoggingOperation(logger, "ERROR", msg, null);
    }

    @Test
    public void testIndirectErrorLogMessageWithException () {
        String      msg="testIndirectErrorLogMessageWithException";
        Logger      logger=Logger.getLogger(getClass());
        Throwable   t=new IllegalArgumentException(msg);
        logger.log(Level.ERROR, msg, t);
        assertLoggingOperation(logger, "ERROR", msg, t);
    }

    @Test
    public void testIndirectFatalLogMessage () {
        String   msg="testIndirectFatalLogMessage";
        Logger   logger=Logger.getLogger(getClass());
        logger.log(Level.FATAL, msg);
        assertLoggingOperation(logger, "FATAL", msg, null);
    }

    @Test
    public void testIndirectFatalLogMessageWithException () {
        String      msg="testIndirectFatalLogMessageWithException";
        Logger      logger=Logger.getLogger(getClass());
        Throwable   t=new IllegalArgumentException(msg);
        logger.log(Level.FATAL, msg, t);
        assertLoggingOperation(logger, "FATAL", msg, t);
    }
    /*
     * @see com.springsource.insight.collection.OperationCollectionAspectTestSupport#getAspect()
     */
    @Override
    public Log4jLoggingOperationCollectionAspect getAspect() {
        return Log4jLoggingOperationCollectionAspect.aspectOf();
    }

    private Operation assertLoggingOperation (Logger logger, String level, String msg, Throwable t)
    {
        Operation   op=assertLoggingOperation(Logger.class, level, msg, t);
        Assert.assertEquals("Mismatched logger name", logger.getName(), op.get(LoggingDefinitions.NAME_ATTR));
        return op;
    }
}

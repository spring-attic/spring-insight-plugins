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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * 
 */
public class CommonsLoggingOperationCollectionAspectTest
        extends LoggingMethodOperationCollectionAspectTestSupport {
    public CommonsLoggingOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testLogErrorMessage () {
        String  msg="testLogErrorMessage";
        Log     logger=LogFactory.getLog(getClass());
        logger.error(msg);
        assertLoggingOperation(Log.class, "ERROR", msg, null);
    }

    @Test
    public void testLogErrorMessageWithException () {
        String      msg="testLogErrorMessageWithException";
        Log         logger=LogFactory.getLog(getClass());
        Throwable   t=new IllegalArgumentException(msg);
        logger.error(msg, t);
        assertLoggingOperation(Log.class, "ERROR", msg, t);
    }

    @Test
    public void testLogFatalMessage () {
        String  msg="testLogFatalMessage";
        Log     logger=LogFactory.getLog(getClass());
        logger.fatal(msg);
        assertLoggingOperation(Log.class, "FATAL", msg, null);
    }

    @Test
    public void testLogFatalMessageWithException () {
        String      msg="testLogFatalMessageWithException";
        Log         logger=LogFactory.getLog(getClass());
        Throwable   t=new IllegalArgumentException(msg);
        logger.fatal(msg, t);
        assertLoggingOperation(Log.class, "FATAL", msg, t);
    }
    /*
     * @see com.springsource.insight.collection.OperationCollectionAspectTestSupport#getAspect()
     */
    @Override
    public CommonsLoggingOperationCollectionAspect getAspect() {
        return CommonsLoggingOperationCollectionAspect.aspectOf();
    }
}

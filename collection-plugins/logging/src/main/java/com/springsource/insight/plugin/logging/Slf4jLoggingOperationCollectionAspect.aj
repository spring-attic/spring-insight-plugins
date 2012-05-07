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
package com.springsource.insight.plugin.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.Marker;

import com.springsource.insight.intercept.operation.Operation;


/**
 * 
 */
public aspect Slf4jLoggingOperationCollectionAspect extends LoggingMethodOperationCollectionAspect {
    public pointcut errorLogFlow ()
        : execution(* Logger+.error(..))
        ;

    public pointcut collectionPoint()
        : errorLogFlow()
       && (!cflowbelow(errorLogFlow()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        return createOperation(jp, (Logger) jp.getTarget(), sig.getName().toUpperCase(), jp.getArgs());
    }
    
    Operation createOperation (JoinPoint jp, Logger logger, String level, Object ... args) {
        Object      msg=resolveMessage(args);
        return super.createOperation(jp, Logger.class, level, String.valueOf(msg), findLastArgument(Throwable.class, args))
                    .putAnyNonEmpty(LoggingDefinitions.NAME_ATTR, logger.getName())
                    ;
    }
    
    static Object resolveMessage (Object ... args) {
        switch(args.length)
        {
            case 1  :   // simple call to error(String)
                return args[0];

            case 2  :
                {
                    Object  arg1=args[0], arg2=args[1];
                    if (arg1 instanceof Marker)
                        return arg2;    // call to error(Marker,String)
                    if (arg2 instanceof Throwable)
                        return arg1;    // call to error(String,Throwable)
                    // call to formatter - we do not format the message to avoid possible exceptions
                    return arg1;
                }

            default : // call to formatter - we do not format the message to avoid possible exceptions
                {
                    Object  arg1=args[0];
                    if (arg1 instanceof Marker)
                        return args[1]; // some formatting call with Marker
                    else
                        return arg1;
                }
        }
    }

    static <T> T findLastArgument (Class<T> expectedClass, Object... args) {
        for (int    index=args.length-1; index >= 0; index--) {
            Object      arg=args[index];
            Class<?>    argClass=(arg == null) ? null : arg.getClass();
            if ((argClass != null) & expectedClass.isAssignableFrom(argClass)) {
                return expectedClass.cast(arg);
            }
        }
        
        return null;
    }

}

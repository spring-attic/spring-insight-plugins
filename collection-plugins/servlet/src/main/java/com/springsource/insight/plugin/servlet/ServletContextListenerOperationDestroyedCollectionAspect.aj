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

package com.springsource.insight.plugin.servlet;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;

public aspect ServletContextListenerOperationDestroyedCollectionAspect extends AbstractOperationCollectionAspect {
    
    private static final OperationType TYPE = LifecycleEndPointAnalyzer.SERVLET_LISTENER_TYPE;
    
    public pointcut collectionPoint() 
        : execution(* ServletContextListener+.contextDestroyed(ServletContextEvent));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        ServletContextListener listener = (ServletContextListener) jp.getThis();
        ServletContextEvent event = (ServletContextEvent) jp.getArgs()[0];
        ServletContext context = event.getServletContext();
        String application = context.getContextPath();
        if ("".equals(application)) {
            application = "/";
        }
        Operation operation = new Operation()
            .type(TYPE)
            .label("Servlet Context: " + application + " Destroyed")
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("listenerClass", listener.getClass().getName())
            .put("listenerPhase", "Destroyed")
            .put("event", "Destroy")
            .put("application", application);
        OperationList contextParams = operation.createList("contextParams");
        for (Enumeration<String> paramNames = context.getInitParameterNames(); paramNames.hasMoreElements();) {
            String name = paramNames.nextElement();
            contextParams.createMap().put("name", name).put("value", event.getServletContext().getInitParameter(name));
        }
        return operation;
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }

    @Override
    public String getPluginName() {
        return "servlet";
    }

}

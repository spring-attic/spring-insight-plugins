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
package com.springsource.insight.plugin.servlet;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;

public aspect FilterOperationCollectionAspect extends AbstractOperationCollectionAspect {
    private static final OperationType TYPE = OperationType.valueOf("servlet-filter");
    private final Map<Filter, Operation> opCache = new ConcurrentHashMap<Filter, Operation>();

    public FilterOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() 
        : execution(* Filter+.doFilter(ServletRequest, ServletResponse, FilterChain));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Filter filter = (Filter) jp.getThis();
        Operation operation = opCache.get(filter);
        if (operation == null) {
            return new Operation()
                    .type(TYPE)
                    .put("filterClass", filter.getClass().getName())
                    .label("Filter: " + filter.getClass().getSimpleName());
        }

        // If we have some servlet request/response specific junk to add, do so here
        // Right now we just fill the filter up with the initParams, so we just pass in
        // the exact same operation
        return operation;
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(): execution(* Filter+.init(FilterConfig)) {
        Filter filter = (Filter) thisJoinPoint.getThis();
        FilterConfig config = (FilterConfig) thisJoinPoint.getArgs()[0];

        Operation  operation = new Operation();
        operation.type(TYPE).put("filterClass", filter.getClass().getName());
        operation.label("Filter: " + config.getFilterName())
                .put("filterName", config.getFilterName());
        OperationMap initParams = operation.createMap("initParams");
        for (Enumeration<String> initParamNames = config.getInitParameterNames(); initParamNames.hasMoreElements(); ) {
            String name = initParamNames.nextElement();
            initParams.put(name, config.getInitParameter(name));
        }
        opCache.put(filter, operation);

    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before() : execution(* Filter+.destroy()) {
        opCache.remove((Filter) thisJoinPoint.getThis());
    }

    @Override
    public String getPluginName() {
        return "servlet";
    }
}

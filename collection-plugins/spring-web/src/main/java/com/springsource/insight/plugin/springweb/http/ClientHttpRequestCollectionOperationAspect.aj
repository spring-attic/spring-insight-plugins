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

package com.springsource.insight.plugin.springweb.http;

import org.aspectj.lang.JoinPoint;
import org.springframework.http.client.ClientHttpRequest;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;

/**
 *
 */
public aspect ClientHttpRequestCollectionOperationAspect extends AbstractSpringWebAspectSupport {
    public ClientHttpRequestCollectionOperationAspect() {
        super(new ClientHttpRequestOperationCollector());
    }

    public pointcut execute() : execution(* ClientHttpRequest+.execute());

    public pointcut collectionPoint() : execute()
        && (!cflowbelow(execute()))
        ;


    @Override
    protected Operation createOperation(JoinPoint jp) {
        ClientHttpRequestOperationCollector collector = (ClientHttpRequestOperationCollector) getCollector();
        ClientHttpRequest request = (ClientHttpRequest) jp.getTarget();
        return collector.fillRequestDetails(OperationCollectionUtil.methodOperation(
                new Operation().type(ClientHttpRequestExternalResourceAnalyzer.TYPE), jp), request);
    }
}

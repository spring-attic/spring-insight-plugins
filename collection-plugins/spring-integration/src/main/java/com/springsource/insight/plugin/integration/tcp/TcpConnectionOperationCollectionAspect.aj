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

package com.springsource.insight.plugin.integration.tcp;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.springframework.integration.ip.tcp.connection.ConnectionFactory;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.integration.AbstractIntegrationOperationCollectionAspect;


/**
 *
 */
public aspect TcpConnectionOperationCollectionAspect extends AbstractIntegrationOperationCollectionAspect {
    public TcpConnectionOperationCollectionAspect() {
        super(new TcpConnectionOperationCollector());
    }

    public pointcut collectionPoint(): execution(* ConnectionFactory+.getConnection());

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object target = jp.getTarget();
        Signature sig = jp.getSignature();
        return new Operation().type(TcpConnectionExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(target.getClass().getSimpleName() + "#" + sig.getName())
                ;
    }
}

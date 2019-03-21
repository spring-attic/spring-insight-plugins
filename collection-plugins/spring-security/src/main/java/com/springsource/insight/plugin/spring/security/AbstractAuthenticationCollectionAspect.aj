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

package com.springsource.insight.plugin.spring.security;

import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.Authentication;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 *
 */
public abstract aspect AbstractAuthenticationCollectionAspect extends MethodOperationCollectionAspect {
    /**
     * The <U>static</U> score assigned to authentication operations - it is
     * just slightly above that of a servlet and/or queue operation, but lower
     * than a Spring component/service since their invocation is a better
     * endpoint name than <code>authenticate</code> call. The assumption is
     * that some &quot;login&quot; method of a service will be called
     */
    public static final int AUTHENTICATION_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE + 1;

    protected AbstractAuthenticationCollectionAspect() {
        super(new AuthenticationProviderOperationCollector());
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createAuthenticationOperation(super.createOperation(jp), extractAuthenticationData(jp));
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        AuthenticationProviderOperationCollector collector = (AuthenticationProviderOperationCollector) getCollector();
        collector.setSensitiveValueMarker(marker);
    }

    protected Operation createAuthenticationOperation(Operation op, Authentication credentials) {
        op.type(SpringSecurityDefinitions.AUTH_OP)
                .label("Authenticate")
                .put(EndPointAnalysis.SCORE_FIELD, AUTHENTICATION_SCORE)
                .putAnyNonEmpty("principal", credentials.getPrincipal())
                .putAnyNonEmpty("credentials", credentials.getCredentials())
        ;

        AuthenticationProviderOperationCollector collector = (AuthenticationProviderOperationCollector) getCollector();
        collector.markSensitiveValues(credentials);
        return op;
    }

    protected Authentication extractAuthenticationData(JoinPoint jp) {
        return (Authentication) jp.getArgs()[0];
    }

    @Override
    public String getPluginName() {
        return SpringSecurityPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

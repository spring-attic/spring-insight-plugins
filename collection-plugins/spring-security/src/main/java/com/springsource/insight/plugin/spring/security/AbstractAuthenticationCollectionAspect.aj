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

package com.springsource.insight.plugin.spring.security;

import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.Authentication;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * 
 */
public abstract aspect AbstractAuthenticationCollectionAspect extends MethodOperationCollectionAspect {
    protected AbstractAuthenticationCollectionAspect () {
        super(new AuthenticationProviderOperationCollector());
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createAuthenticationOperation(super.createOperation(jp), extractAuthenticationData(jp));
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        AuthenticationProviderOperationCollector    collector=(AuthenticationProviderOperationCollector) getCollector();
        collector.setSensitiveValueMarker(marker);
    }

    protected Operation createAuthenticationOperation (Operation op, Authentication credentials) {
        op.type(SpringSecurityDefinitions.AUTH_OP)
          .label("Authenticate")
          .putAnyNonEmpty("principal", credentials.getPrincipal())
          .putAnyNonEmpty("credentials", credentials.getCredentials())
          ;

        AuthenticationProviderOperationCollector collector = (AuthenticationProviderOperationCollector) getCollector();
        collector.markSensitiveValues(credentials);
        return op;
    }

    protected Authentication extractAuthenticationData (JoinPoint jp) {
        return (Authentication) jp.getArgs()[0];
    }
    
    @Override
    public String getPluginName() {
        return "spring-security";
    }
}

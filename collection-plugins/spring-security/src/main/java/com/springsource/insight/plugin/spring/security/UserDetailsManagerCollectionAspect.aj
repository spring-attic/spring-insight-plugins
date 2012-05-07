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

package com.springsource.insight.plugin.spring.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * Intercepts calls to {@link UserDetailsManager}
 */
public aspect UserDetailsManagerCollectionAspect extends MethodOperationCollectionAspect {
    public UserDetailsManagerCollectionAspect () {
        super(new UserDetailsOperationCollector());
    }

    /* NOTE: we list the actual calls since we cannot provide a wildcard pattern
     *  that will cover ONLY the calls we want - i.e., the interface ones
     */
    public pointcut collectionPoint()
        : execution(* UserDetailsService+.loadUserByUsername(String))
       || execution(* UserDetailsManager+.createUser(UserDetails))
       || execution(* UserDetailsManager+.updateUser(UserDetails))
       || execution(* UserDetailsManager+.deleteUser(String))
       || execution(* UserDetailsManager+.changePassword(String,String))
       || execution(* UserDetailsManager+.userExists(String))
        ;

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        UserDetailsOperationCollector    collector=(UserDetailsOperationCollector) getCollector();
        collector.setSensitiveValueMarker(marker);
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation   op=super.createOperation(jp)
                            .type(SpringSecurityDefinitions.USER_OP);
        Signature   sig=jp.getSignature();
        op.label(sig.getName())
          .put("action", sig.getName());
        encodeArguments(op, sig, jp.getArgs());
        return op;
    }

    // could have used separate aspects for each call, but for now this is good enough
    Operation encodeArguments (Operation op, Signature sig, Object ... args) {
        UserDetailsOperationCollector    collector=(UserDetailsOperationCollector) getCollector();
        String                           actionName=sig.getName();
        if ("loadUserByUsername".equals(actionName)
         || "deleteUser".equals(actionName)
         || "userExists".equals(actionName)) {
            String  username=(String) args[0];
            op.put("username", username);
            collector.markSensitiveString(username);
        } else if ("createUser".equals(actionName) || "updateUser".equals(actionName)) {
            UserDetails details=(UserDetails) args[0];
            collector.markSensitiveDetails(details);
            if (collectExtraInformation()) {
                UserDetailsOperationCollector.encodeUserDetails(op.createMap("userDetails"), details);
            }
        } else if ("changePassword".equals(actionName)) {
            String  oldPassword=(String) args[0], newPassword=(String) args[1];
            op.put("oldPassword", oldPassword)
              .put("newPassword", newPassword);
            collector.markSensitiveString(oldPassword);
            collector.markSensitiveString(newPassword);
        }
        
        return op;
    }

    boolean collectExtraInformation () {
        UserDetailsOperationCollector    collector=(UserDetailsOperationCollector) getCollector();
        return collector.collectExtraInformation();
    }

    @Override
    public String getPluginName() {
        return "spring-security";
    }
}

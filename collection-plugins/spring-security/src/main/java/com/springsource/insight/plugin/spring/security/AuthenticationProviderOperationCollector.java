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

package com.springsource.insight.plugin.spring.security;

import java.security.Principal;

import org.springframework.security.core.Authentication;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * 
 */
public class AuthenticationProviderOperationCollector extends ObscuringOperationCollector {
    public AuthenticationProviderOperationCollector() {
        super();
    }

    public AuthenticationProviderOperationCollector(ObscuredValueMarker marker) {
        super(marker);
    }

    // fill in some data from the return value
    @Override
    protected void markSensitiveReturnValueAttributes (Operation op, Object returnValue) {
        if (returnValue != null) {  // OK to return null to indicate authentication failure
            Authentication  auth=(Authentication) returnValue;
            markSensitiveValues(auth);
            op.put("authenticated", auth.isAuthenticated());
            updateGrantedAuthorities(op, auth.getAuthorities());
        } else {
            op.put("authenticated", false);
        }
    }

    void markSensitiveValues (Authentication auth) {
        markSensitiveValues(obscuredMarker, auth);
    }

    static void markSensitiveValues (ObscuredValueMarker marker, Authentication auth) {
        // can happen if AuthenticationProvider#authenticate returns null to indicate a failure 
        if (auth == null) {
            return;
        }

        markSensitivePrincipalValues(marker, auth);

        Object  principal=auth.getPrincipal();
        if (principal instanceof Principal) {
            markSensitivePrincipalValues(marker, (Principal) principal);
        } else {
            marker.markObscured(principal);
        }
        marker.markObscured(auth.getCredentials());
    }
    
    static void markSensitivePrincipalValues (ObscuredValueMarker marker, Principal principal) {
        if (principal == null) {
            return;
        }

        marker.markObscured(principal);
        marker.markObscured(principal.getName());
    }
}

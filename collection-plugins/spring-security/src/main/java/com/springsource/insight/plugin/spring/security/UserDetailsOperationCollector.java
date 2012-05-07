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

import org.springframework.security.core.userdetails.UserDetails;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * 
 */
public class UserDetailsOperationCollector extends ObscuringOperationCollector {
    /**
     * The name of the {@link OperationMap} under which the result {@link UserDetails}
     * is mapped in the {@link Operation} (provided {@link #collectExtraInformation()})
     */
    public static final String  RESULT_MAP_NAME="userResult";

    public UserDetailsOperationCollector() {
        super();
    }

    public UserDetailsOperationCollector(ObscuredValueMarker marker) {
        super(marker);
    }

    @Override
    protected void markSensitiveReturnValueAttributes(Operation op, Object returnValue) {
        if (returnValue instanceof UserDetails) {
            UserDetails details=(UserDetails) returnValue;
            markSensitiveDetails(details);
            if (collectExtraInformation()) {
                encodeUserDetails(op.createMap(RESULT_MAP_NAME), details);
            }
        }
    }

    void markSensitiveDetails (UserDetails details) {
        markSensitiveDetails(obscuredMarker, details);
    }
    // used for username/password arguments
    void markSensitiveString (String strValue) {
        if ((strValue != null) && (strValue.length() > 0)) {
            obscuredMarker.markObscured(strValue);
        }
    }

    static void markSensitiveDetails (ObscuredValueMarker marker, UserDetails details) {
        if (details == null) {
            return;
        }

        marker.markObscured(details.getUsername());
        marker.markObscured(details.getPassword());
    }
    
    static OperationMap encodeUserDetails (OperationMap map, UserDetails details) {
        if (details == null) {
            return map;
        }

        map.put("username", details.getUsername())
           .put("password", details.getPassword())
           .put("accountNonExpired", details.isAccountNonExpired())
           .put("accountNonLocked", details.isAccountNonLocked())
           .put("credentialsNonExpired", details.isCredentialsNonExpired())
           .put("enabled", details.isEnabled())
           ;
        updateGrantedAuthorities(map, details.getAuthorities());
        return map;
    }
}

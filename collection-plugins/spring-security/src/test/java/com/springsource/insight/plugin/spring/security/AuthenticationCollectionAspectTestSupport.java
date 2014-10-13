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


import java.util.Collection;

import org.springframework.security.core.Authentication;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;

/**
 *
 */
public abstract class AuthenticationCollectionAspectTestSupport
        extends SpringSecurityCollectionTestSupport {
    protected AuthenticationCollectionAspectTestSupport() {
        super();
    }

    protected void assertObscuredAuthValues(Authentication token, Authentication result, Collection<?> obscuredValues) {
        assertTrue("Original principal not obscured", obscuredValues.contains(token.getPrincipal()));
        assertTrue("Original credentials not obscured", obscuredValues.contains(token.getCredentials()));
        assertTrue("Result principal not obscured", obscuredValues.contains(result.getPrincipal()));
        assertTrue("Result credentials not obscured", obscuredValues.contains(result.getCredentials()));
    }

    protected Operation assertOperationResult(Authentication auth, Collection<String> grants) {
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);

        assertEquals("Mismatched operation type", SpringSecurityDefinitions.AUTH_OP, op.getType());
        assertSame("Mismatched principal", auth.getPrincipal(), op.get("principal"));
        assertSame("Mismatched credentials", auth.getCredentials(), op.get("credentials"));

        assertGrantedAuthorities(op.get(ObscuringOperationCollector.GRANTED_AUTHS_LIST_NAME, OperationList.class), grants);
        return op;
    }
}

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

import java.util.ArrayList;
import java.util.logging.Logger;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

/**
 * 
 */
public class AuthenticationProviderCollectionAspectTestProvider implements AuthenticationProvider {
    private final Logger    logger=Logger.getLogger(getClass().getName());
    private final boolean   useAuthCopy;
    public AuthenticationProviderCollectionAspectTestProvider(@SuppressWarnings("hiding") final boolean useAuthCopy) {
        this.useAuthCopy = useAuthCopy;
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        logger.info("authenticate() enter: " + authentication);
        Authentication  result=authentication;
        if (useAuthCopy)
            result = new TestingAuthenticationToken(authentication.getPrincipal(),
                            authentication.getCredentials(),
                            new ArrayList<GrantedAuthority>(authentication.getAuthorities()));
        if (!result.isAuthenticated())
            result.setAuthenticated(true);
        logger.info("authenticate() finished: " + result);
        return result;
    }
    public boolean supports(Class<? extends Object> authentication) {
        return TestingAuthenticationToken.class.isAssignableFrom(authentication);
    }

}

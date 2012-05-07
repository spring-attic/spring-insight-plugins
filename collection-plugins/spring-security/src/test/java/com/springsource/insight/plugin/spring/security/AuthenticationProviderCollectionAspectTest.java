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


import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthenticationProviderCollectionAspectTest extends AuthenticationCollectionAspectTestSupport {
    public AuthenticationProviderCollectionAspectTest() {
        super();
    }

    @Test
    public void testSuccessfulAuthentication () {
        String[]                grants={ "tester", "insight"};
        Authentication          token=new TestingAuthenticationToken("testSuccessfulAuthentication", "shir", grants);
        AuthenticationProvider  testProvider=new AuthenticationProviderCollectionAspectTestProvider(false);
        Authentication          result=testProvider.authenticate(token);
        Assert.assertNotNull("No authentication result", result);
        Assert.assertSame("Mismatched authentication instances equality", token, result);
        assertOperationResult(result, Arrays.asList(grants));
    }

    @Test
    public void testObscuredCredentials () {
        DummyObscuredValueMarker    marker=new DummyObscuredValueMarker();
        getAspect().setSensitiveValueMarker(marker);

        String[]                grants={ "tester", "insight"};
        Authentication          token=new TestingAuthenticationToken("testObscuredCredentials", "omer", grants);
        AuthenticationProvider  testProvider=new AuthenticationProviderCollectionAspectTestProvider(true);
        Authentication          result=testProvider.authenticate(token);
        Assert.assertNotSame("Authentication token not cloned", token, result);
        assertObscuredAuthValues(token, result, marker.getValues());
    }

    @Override
    public AuthenticationProviderCollectionAspect getAspect() {
        return AuthenticationProviderCollectionAspect.aspectOf();
    }
}

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

import java.util.Arrays;

import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;


/**
 * 
 */
public class AuthenticationManagerCollectionAspectTest
        extends AuthenticationCollectionAspectTestSupport {
    public AuthenticationManagerCollectionAspectTest() {
        super();
    }

    @Test
    public void testSuccessfulAuthentication () {
        String[]                grants={ "tester", "insight"};
        Authentication          token=new TestingAuthenticationToken("testSuccessfulAuthentication", "shir", grants);
        AuthenticationManager   testManager=new AuthenticationTestManager(false);
        Authentication          result=testManager.authenticate(token);
        assertNotNull("No authentication result", result);
        assertSame("Mismatched authentication instances equality", token, result);
        assertOperationResult(result, Arrays.asList(grants));
    }

    @Test
    public void testObscuredCredentials () {
        DummyObscuredValueMarker    marker=new DummyObscuredValueMarker();
        getAspect().setSensitiveValueMarker(marker);

        String[]                grants={ "tester", "insight"};
        Authentication          token=new TestingAuthenticationToken("testObscuredCredentials", "omer", grants);
        AuthenticationManager   testManager=new AuthenticationTestManager(true);
        Authentication          result=testManager.authenticate(token);
        assertNotSame("Authentication token not cloned", token, result);
        assertObscuredAuthValues(token, result, marker.getValues());
    }

    @Override
    public AuthenticationManagerCollectionAspect getAspect() {
        return AuthenticationManagerCollectionAspect.aspectOf();
    }

}

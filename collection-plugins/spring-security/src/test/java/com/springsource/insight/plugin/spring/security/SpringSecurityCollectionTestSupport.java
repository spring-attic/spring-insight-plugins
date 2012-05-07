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

import java.util.Collection;

import org.junit.Assert;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.OperationList;

/**
 * 
 */
public abstract class SpringSecurityCollectionTestSupport
        extends OperationCollectionAspectTestSupport {

    protected SpringSecurityCollectionTestSupport() {
        super();
    }

    protected OperationList assertGrantedAuthoritiesInstances (OperationList grantedList, Collection<GrantedAuthority> grants) {
        return assertGrantedAuthorities(grantedList, AuthorityUtils.authorityListToSet(grants));
    }

    protected OperationList assertGrantedAuthorities (OperationList grantedList, Collection<String> grants) {
        Assert.assertNotNull("Missing granted authorities", grantedList);
        Assert.assertEquals("Mismatched granted authorities size", grants.size(), grantedList.size());
        for (int index=0; index < grantedList.size(); index++) {
            String  ga=grantedList.get(index, String.class);
            Assert.assertNotNull("Empty granted authority #" + index, ga);
            Assert.assertTrue("Missing authority " + ga, grants.contains(ga));
        }
        return grantedList;
    }
}

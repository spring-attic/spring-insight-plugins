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

package com.springsource.insight.plugin.springdata;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.springsource.insight.plugin.springdata.dao.TestEntityRepository;

@TransactionConfiguration
@ContextConfiguration(locations={ "classpath:META-INF/jpaTestContext.xml" })
public class JpaRepositoryCollectionAspectTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Inject private TestEntityRepository    repository;
    private final RepositoryMethodOperationCollectionAspectTestSupport  testSupport=
            new RepositoryMethodOperationCollectionAspectTestSupport();
    public JpaRepositoryCollectionAspectTest() {
        super();
    }

    // need this since we cannot inherit from both classes
    @Before
    public void setUp () {
        testSupport.setUp();
    }

    @After
    public void tearDown () {
        testSupport.restore();
    }

    @Test
    public void testEntityRepository () {
        testSupport.testEntityRepository(repository);
    }
}

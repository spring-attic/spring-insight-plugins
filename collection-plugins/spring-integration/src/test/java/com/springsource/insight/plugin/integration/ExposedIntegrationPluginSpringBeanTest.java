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

package com.springsource.insight.plugin.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.springsource.insight.intercept.spring.AbstractExposedStandardPluginSpringBeanTestSupport;

/**
 * Makes sure that the exposed singletons are indeed visible 
 */
@ContextConfiguration(locations={ "classpath:META-INF/insight-plugin-spring-integration.xml" })
public class ExposedIntegrationPluginSpringBeanTest
	   extends AbstractExposedStandardPluginSpringBeanTestSupport<IntegrationPluginRuntimeDescriptor> {
    /* NOTE: all beans are autowired with required=false since we want
     * to have specific tests for each and we don't want to fail ALL the
     * tests if one bean is missing
     */
	@Autowired(required=false) protected IntegrationPluginRuntimeDescriptor	integrationPluginRuntimeDescriptor;
	@Autowired(required=false) protected IntegrationEndPointAnalyzer	integrationEndPointAnalyzer;
	@Autowired(required=false) protected TcpConnectionExternalResourceAnalyzer	tcpConnectionExternalResourceAnalyzer;

	public ExposedIntegrationPluginSpringBeanTest() {
		super(IntegrationPluginRuntimeDescriptor.class);
	}
}

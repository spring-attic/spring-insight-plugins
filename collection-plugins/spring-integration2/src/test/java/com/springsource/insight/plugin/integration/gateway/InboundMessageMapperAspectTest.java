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

package com.springsource.insight.plugin.integration.gateway;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.mapping.InboundMessageMapper;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;

/**
 * 
 */
public class InboundMessageMapperAspectTest extends AbstractCollectionTestSupport {
	public InboundMessageMapperAspectTest() {
		super();
	}

	@Test
	public void testHasRequestMapperIntroduction() {
		MessagingGatewaySupport	support=new TestMessagingGatewaySupport();
		assertInstanceOf(support.getClass().getSimpleName() + " " + HasRequestMapper.class.getSimpleName() + " ?",
						 support, HasRequestMapper.class);
	}

	@Test
	public void testRequestMapperTagging() {
		InboundMessageMapper<?> expected=Mockito.mock(InboundMessageMapper.class);
		MessagingGatewaySupport	support=new TestMessagingGatewaySupport();
		support.setRequestMapper(expected);
		
		assertInstanceOf(support.getClass().getSimpleName() + " " + HasRequestMapper.class.getSimpleName() + " ?",
				 		 support, HasRequestMapper.class);
		HasRequestMapper	mapperAccess=(HasRequestMapper) support;
		Object				actual=mapperAccess.__getRequestMapper();
		assertSame("Mismatched mapper instances", expected, actual);
	}

	public static class TestMessagingGatewaySupport extends MessagingGatewaySupport {
		private InboundMessageMapper<?>	lastSetMapper;

		public TestMessagingGatewaySupport() {
			super();
		}

		@Override
		public void setRequestMapper(InboundMessageMapper<?> requestMapper) {
			super.setRequestMapper(requestMapper);
			lastSetMapper = requestMapper;
		}
		
		public InboundMessageMapper<?> getLastSetMapper() {
			return lastSetMapper;
		}
	}
}

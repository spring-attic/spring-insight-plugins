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

package com.springsource.insight.plugin.rabbitmqClient;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.KeyValPair;


public class RabbitMQConsumerResourceAnalyzerTest extends AbstractRabbitMQResourceAnalyzerTest {
	public RabbitMQConsumerResourceAnalyzerTest() {
		super(new RabbitMQConsumerResourceAnalyzer());
	}

	@Override
	protected KeyValPair<String,String> addOperationProps(Operation operation, boolean addRouting, boolean addExchange, Boolean useTempRoutingKey){
		KeyValPair<String,String>	res = null;

		OperationMap map = operation.createMap("envelope");
		if (addExchange){
			map.put("exchange", TEST_EXCHANGE);
			res = setExchange(res, TEST_EXCHANGE);
		}

		if (addRouting){
			String routingKey = TEST_ROUTING_KEY;
			if (useTempRoutingKey){
				routingKey = TEST_TEMP_ROUTING_KEY;
			}
			map.put("routingKey", routingKey);
			res = setRoutingKey(res, routingKey);
		}

		return res;
	}
}

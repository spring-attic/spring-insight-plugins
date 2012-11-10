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

import com.springsource.insight.intercept.operation.OperationType;


public class SpringIntegrationDefinitions {

	public static final OperationType SI_OP_GATEWAY_TYPE = OperationType.valueOf("integration_gateway_operation");
	public static final OperationType SI_OP_MESSAGE_ADAPTER_TYPE = OperationType.valueOf("integration_message_adapter");
	public static final OperationType SI_OPERATION_TYPE = OperationType.valueOf("integration_operation");
	public static final OperationType SI_OP_SERVICE_ACTIVATOR_TYPE = OperationType.valueOf("integration_service_activator_operation");
	public static final OperationType SI_OP_CHANNEL_TYPE = OperationType.valueOf("integration_channel_operation");

	public static final String GATEWAY = "Gateway";
	public static final String CHANNEL = "Channel";
	public static final String MESSAGE_HANDLER = "MessageHandler";
	public static final String SERVICE_ACTIVATOR = "ServiceActivator";
	public static final String TRANSFORMER = "Transformer";
	public static final String MESSAGE_ADAPTER = "MessageAdapter";

	// some widely used operation attributes
	public static final String  SI_COMPONENT_TYPE_ATTR = "siComponentType";
	public static final String  SI_SPECIFIC_TYPE_ATTR  = "siSpecificType";
	public static final String  BEAN_NAME_ATTR 		 = "beanName";
	public static final String  PAYLOAD_TYPE_ATTR 	 = "payloadType";
	public static final String  ID_HEADER_ATTR 	 = "idHeader";



}

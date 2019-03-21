/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.integration.gateway;

import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 *
 */
public privileged aspect GatewayMethodInboundMessageMapperAspect {

    declare parents:org.springframework.integration.gateway.GatewayMethodInboundMessageMapper+implements HasMethod;

    /* ------------------------------------------------------------------------------------------------------------- *
     * HasMethod and HasRequestMapper - add support to proxy gateways. Proxy gateways are just interfaces, in-order
     * to expose the real gateway interface name, and the real method name we must expose:
     * 1. MessagingGatewaySupport#requestMapper - done with HasRequestMapper
     * 2. GatewayMethodInboundMessageMapper#method - done with HasMethod
     * ------------------------------------------------------------------------------------------------------------- */

    @SuppressWarnings("rawtypes")
    @SuppressAjWarnings
    after(Method method, Map map, org.springframework.integration.gateway.GatewayMethodInboundMessageMapper gatewayMapper):
            execution(public org.springframework.integration.gateway.GatewayMethodInboundMessageMapper+.new(Method, Map))
                    && args(method, map) && target(gatewayMapper) {

        if (gatewayMapper instanceof HasMethod) {
            ((HasMethod) gatewayMapper).__setInsightMethod(method);
        }
    }
}

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

package com.springsource.insight.plugin.rabbitmqClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public abstract class AbstractRabbitMQResourceAnalyzer implements ExternalResourceAnalyzer {
	public static final String RABBIT = "RabbitMQ";	
	/**
	 * Placeholder string used if no exchange name specified
	 */
	public static final String NO_EXCHANGE = "AMQP default";
	public static final String NO_ROUTING_KEY = "no routing key";

	/**
	 * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
	 * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
	 * to let other endpoints &quot;beat&quot; this one
	 */
	public static final int	DEFAULT_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE;

	public static final String UNNAMED_TEMP_QUEUE_KEY_PREFIX = "amq.gen-";
	public static final String UNNAMED_TEMP_QUEUE_LABEL = "AMQP internal routing";
	public static final String UNNAMED_RPC_QUEUE_KEY_PREFIX = "amqp.gen-";
	public static final String UNNAMED_RPC_QUEUE_LABEL = "RPC internal routing";

	private final RabbitPluginOperationType operationType;
	private final boolean isIncoming;

	protected AbstractRabbitMQResourceAnalyzer(RabbitPluginOperationType type, boolean incoming) {		
		this.operationType = type;
		this.isIncoming = incoming;
	}

	public final boolean isIncomingResource () {
		return isIncoming;
	}

	public final RabbitPluginOperationType getRabbitPluginOperationType () {
		return operationType;
	}

	protected abstract String getExchange(Operation op);

	protected abstract String getRoutingKey(Operation op);
	
	public OperationType getOperationType(){
		return operationType.getOperationType();
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		return locateExternalResourceName(trace,  locateFrames(trace));
	}

	public Collection<Frame> locateFrames(Trace trace) {
		return AbstractMetricsGenerator.locateDefaultMetricsFrames(trace, getOperationType());
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> queueFrames) {
		if (ListUtil.size(queueFrames) <= 0) {
			return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> queueDescriptors = new ArrayList<ExternalResourceDescriptor>(queueFrames.size());
		ColorManager					 colorManager=ColorManager.getInstance();
		for (Frame queueFrame : queueFrames) {
			Operation op = queueFrame.getOperation();
			String host = op.get("host", String.class);            
			int port = op.getInt("port", (-1));
			String color = colorManager.getColor(op);			

			String finalExchange = getFinalExchangeName(getExchange(op));
			String finalRoutingKey = getFinalRoutingKey(getRoutingKey(op));
			String exchangeResourceName = buildExternalResourceName(finalExchange, finalRoutingKey, false, host, port);

			ExternalResourceDescriptor externalResourceExchangeDescriptor =
					new ExternalResourceDescriptor(queueFrame,
							exchangeResourceName,
							buildExternalResourceLabel(buildLabel(finalExchange, null)),
							ExternalResourceType.QUEUE.name(),
							RABBIT,
							host,
							port,
							color, isIncoming);
			queueDescriptors.add(externalResourceExchangeDescriptor);            

			// even if there is no routing key, i.e in the Insight world there is no child resource,
			// we still report a dummy one so that AppInsight has a consistent API
			String childRoutingKey = finalRoutingKey;
			if (isTrimEmpty(getRoutingKey(op))){
				childRoutingKey = NO_ROUTING_KEY;
			}

			ExternalResourceDescriptor externalResourceRoutingKeyDescriptor =
					new ExternalResourceDescriptor(queueFrame,
							buildExternalResourceName(finalExchange, childRoutingKey, true, host, port),
							buildLabel(null, childRoutingKey),
							ExternalResourceType.QUEUE.name(),
							RABBIT,
							host,
							port,
							color, isIncoming, externalResourceExchangeDescriptor);
			externalResourceExchangeDescriptor.setChildren(Collections.singletonList(externalResourceRoutingKeyDescriptor));           
		}

		return queueDescriptors;
	}


	public static String getFinalExchangeName(String exchange){		
		boolean hasExchange = !isTrimEmpty(exchange);
		if (hasExchange) {
			return exchange;
		} else {
			return NO_EXCHANGE;
		}
	}

	public static String getFinalRoutingKey(String routingKey){
		boolean hasRoutingKey=!isTrimEmpty(routingKey);
		if (hasRoutingKey) {
			if (routingKey.startsWith(UNNAMED_TEMP_QUEUE_KEY_PREFIX)){
				return UNNAMED_TEMP_QUEUE_LABEL;
			} else if (routingKey.startsWith(UNNAMED_RPC_QUEUE_KEY_PREFIX)){			
				return UNNAMED_RPC_QUEUE_LABEL;
			}
		}
		return routingKey;
	}

	public static String buildExternalResourceName (String finalExchange, String finalRoutingKey, boolean useRoutingKey, String host, int port) {

		return RABBIT + ":" + MD5NameGenerator.getName(finalExchange + (useRoutingKey ? finalRoutingKey : "") + host + port);
	}

	public static String buildExternalResourceLabel (String label) {
		return RABBIT + "-" + label;
	}

	public static String buildLabel (String finalExchange, String finalRoutingKey) {

		StringBuilder sb = new StringBuilder(StringUtil.getSafeLength(finalExchange)
				+ StringUtil.getSafeLength(finalRoutingKey)
				+ 24 /* extra text */);	
		
		
		boolean hasExchange = !isTrimEmpty(finalExchange);
		if (hasExchange) {		
			sb.append("Exchange#").append(finalExchange);
		}

		boolean hasRoutingKey = !isTrimEmpty(finalRoutingKey);
		if (hasRoutingKey) {
			if (hasExchange) {	
				sb.append(' ');
			}
			sb.append("RoutingKey#").append(finalRoutingKey);
		}

		return sb.toString();
	}


	private static boolean isTrimEmpty(String str){
		return (str == null) || (str.trim().length() <= 0);
	}

	@Override
	public String toString() {
		return getRabbitPluginOperationType().name() + "[incoming=" + isIncomingResource() + "]";
	}

}

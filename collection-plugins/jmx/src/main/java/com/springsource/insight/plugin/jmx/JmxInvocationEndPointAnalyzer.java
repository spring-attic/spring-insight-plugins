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

package com.springsource.insight.plugin.jmx;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.MalformedObjectNameException;

import com.springsource.insight.collection.method.custom.CustomScoreGenerator;
import com.springsource.insight.collection.method.custom.ScoreGeneratorsFactory;
import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.logging.InsightLogManager;
import com.springsource.insight.util.logging.InsightLogger;
import com.springsource.insight.util.props.AggregateNamedPropertySource;
import com.springsource.insight.util.props.NamedPropertySource;
import com.springsource.insight.util.props.PropertiesUtil;

/**
 * 
 */
public class JmxInvocationEndPointAnalyzer
		extends AbstractSingleTypeEndpointAnalyzer
		implements CollectionSettingsUpdateListener {
	public static final String	DOMAIN_NAME_PROP=ResourceKey.DOMAIN_NAME_PROP,
								METHOD_NAME_PROP="invokedMethod",
								SIGNATURE_NAME_PROP="invokedSignature",
								INVOCATION_ARGS_PROP="invocationArgs";
	public static final String	NAME_KEY="name", TYPE_KEY="type";
		public static final String SIMPLE_BEAN_NAME_FORMAT="${" + NAME_KEY + PropertiesUtil.PROP_NAME_CHOICE_STRING + TYPE_KEY + "}";
	public static final CollectionSettingName	ENDPOINT_FORMAT=
			new CollectionSettingName("endpoint.format", JmxPluginRuntimeDescriptor.PLUGIN_NAME, "Formats the endpoint value");
		public static final String	DEFAULT_ENDPOINT_FORMAT=
				"${" + DOMAIN_NAME_PROP + "}"
			  + "." + SIMPLE_BEAN_NAME_FORMAT
			  + "#${" + METHOD_NAME_PROP + "}"
			  + "(${" + SIGNATURE_NAME_PROP + "})"
			  ;
	public static final CollectionSettingName	LABEL_FORMAT=
				new CollectionSettingName("label.format", JmxPluginRuntimeDescriptor.PLUGIN_NAME, "Formats the endpoint label");
		public static final String	DEFAULT_LABEL_FORMAT=
				SIMPLE_BEAN_NAME_FORMAT
			  + "#${" + METHOD_NAME_PROP + "}"
			  + "(${" + SIGNATURE_NAME_PROP + "})"
			  ;
	public static final CollectionSettingName	EXAMPLE_FORMAT=
			new CollectionSettingName("example.format", JmxPluginRuntimeDescriptor.PLUGIN_NAME, "Formats the endpoint example text");
		public static final String	DEFAULT_EXAMPLE_FORMAT=DEFAULT_LABEL_FORMAT;

	public static final CollectionSettingName	SCORE_VALUE=
				new CollectionSettingName("score.value", JmxPluginRuntimeDescriptor.PLUGIN_NAME, "Formats the endpoint score");
		public static final String	DEFAULT_SCORE_FORMAT=ScoreGeneratorsFactory.DEFAULT_SCORE;

	public static final List<CollectionSettingName>	SETTINGS=
			Collections.unmodifiableList(
					Arrays.asList(ENDPOINT_FORMAT, LABEL_FORMAT, EXAMPLE_FORMAT, SCORE_VALUE));
	private final Map<CollectionSettingName, String>	formatsMap=
			Collections.synchronizedMap(
					new TreeMap<CollectionSettingName,String>(CollectionSettingName.BY_KEY_COMPARATOR));

	private static class LazyFieldHolder {
		@SuppressWarnings("synthetic-access")
		private static final JmxInvocationEndPointAnalyzer	INSTANCE=new JmxInvocationEndPointAnalyzer();
	}

	private JmxInvocationEndPointAnalyzer() {
		this(CollectionSettingsRegistry.getInstance());
	}

	// jUnit tests
	JmxInvocationEndPointAnalyzer(CollectionSettingsRegistry registry) {
		super(JmxPluginRuntimeDescriptor.INVOKE);

		if (registry == null) {
			throw new IllegalArgumentException("No registry");
		}
		
		registry.addListener(this);
		registry.register(ENDPOINT_FORMAT, DEFAULT_ENDPOINT_FORMAT);
		registry.register(LABEL_FORMAT, DEFAULT_LABEL_FORMAT);
		registry.register(EXAMPLE_FORMAT, DEFAULT_EXAMPLE_FORMAT);
		registry.register(SCORE_VALUE, DEFAULT_SCORE_FORMAT);
	}

	@SuppressWarnings("synthetic-access")
	public static final JmxInvocationEndPointAnalyzer getInstance() {
		return LazyFieldHolder.INSTANCE;
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
	    Operation			operation=frame.getOperation();
	    NamedPropertySource	props=toPropertySource(operation);
	    String				endpointName=PropertiesUtil.format(getSettingFormat(ENDPOINT_FORMAT), props);
	    return new EndPointAnalysis(EndPointName.valueOf(endpointName),
	    							PropertiesUtil.format(getSettingFormat(LABEL_FORMAT), props),
	    							PropertiesUtil.format(getSettingFormat(EXAMPLE_FORMAT), props),
	                                getOperationScore(operation, depth),
	                                operation);
	}

	public String getSettingFormat(CollectionSettingName name) {
		if (name == null) {
			return null;
		} else {
			return formatsMap.get(name);
		}
	}

	@Override
	protected int getOperationScore(Operation op, int depth) {
		Number	score=resolveOperationScore(op, getSettingFormat(SCORE_VALUE));
        if (score != null) {	// check if have an override or a formatted value
        	return score.intValue();
        } else {
        	return super.getOperationScore(op, depth);
        }
	}

	public void incrementalUpdate(CollectionSettingName name, Serializable value) {
		if ((name == null) || (value == null) || (!SETTINGS.contains(name))) {
			return;
		}
		
		String	formatValue=value.toString(), prevValue=formatsMap.put(name, formatValue);
		if (StringUtil.safeCompare(formatValue, prevValue) != 0) {
			_logger.info("incrementalUpdate(" + name + ") " + prevValue + " => " + formatValue);
		}
	}
	
	public static final Number resolveOperationScore(Operation op, String scoreMode) {
        Number score=op.get(EndPointAnalysis.SCORE_FIELD, Number.class);
        if (score != null) {	// check if have an override in the operation
        	return score;
        }

        String	scoreType=scoreMode;
        if (StringUtil.isEmpty(scoreType)) {
        	scoreType = DEFAULT_SCORE_FORMAT;
        }
        
        ScoreGeneratorsFactory	factory=ScoreGeneratorsFactory.getInstance();
        try {
        	CustomScoreGenerator	gen=factory.resolveCustomScoreGenerator(scoreType);
        	// NOTE: the default score generator returns null
        	if ((score=gen.calculateOperationScore(op, null, JmxInvocationEndPointAnalyzer.class.getSimpleName())) != null) {
        		return score;
        	}
        } catch(RuntimeException e) {
			InsightLogger	logger=InsightLogManager.getLogger(JmxInvocationEndPointAnalyzer.class.getName());
        	logger.warning("resolveOperationScore(" + scoreType + ")"
        				  + " failed (" + e.getClass().getSimpleName() + ")"
        				  + " to generate score: " + e.getMessage());
        }

        return null;
	}

	public static final Operation updateOperation(Operation op, String methodName, String[] signature, Object[] argVals) {
		op.type(JmxPluginRuntimeDescriptor.INVOKE)
		  .label(JoinPointBreakDown.getMethodStringFromArgs(methodName, signature))
		  .putAnyNonEmpty(METHOD_NAME_PROP, methodName)
		  .putAnyNonEmpty(SIGNATURE_NAME_PROP, JoinPointBreakDown.createMethodParamsSignature(signature))
		  ;
		updateInvocationArgs(op, argVals);
		return op;
	}

	public static final OperationList updateInvocationArgs(Operation op, Object ... argVals) {
		return updateInvocationArgs(op.createList(INVOCATION_ARGS_PROP), argVals);
	}

	public static final OperationList updateInvocationArgs(OperationList op, Object ... argVals) {
		if (ArrayUtil.length(argVals) <= 0) {
			return op;
		}
		
		for (Object v : argVals) {
			op.add(StringFormatterUtils.formatObject(v));
		}
		return op;
	}

	public static final NamedPropertySource toPropertySource(final Operation op) {
		if (op == null) {
			return PropertiesUtil.EMPTY_SOURCE;
		}
		
		NamedPropertySource	opProps=OperationUtils.toPropertySource(op);
		String	beanName=op.get(JmxPluginRuntimeDescriptor.BEAN_NAME_PROP, String.class);
		if (StringUtil.isEmpty(beanName)) {
			return opProps;
		}
		
		try {
			NamedPropertySource	beanProps=ResourceKey.toPropertySource(beanName);
			// NOTE: order is important - especially for the 'type' property
			return new AggregateNamedPropertySource(beanProps, opProps);
		} catch(MalformedObjectNameException e) {
			InsightLogger	logger=InsightLogManager.getLogger(JmxInvocationEndPointAnalyzer.class.getName());
			logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
						+ " to convert " + beanName + " to property source: " + e.getMessage());
			return opProps;
		}
	}
}

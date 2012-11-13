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

package com.springsource.insight.plugin.springweb.controller;

import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * Renders any {@link Map}, {@link Model}, {@link View}, {@link ModelMap}
 * and/or {@link ModelAndView} attributes
 * @see <A HREF="http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/mvc.html#mvc-ann-arguments">Supported method argument types</A>
 * @see <A HREF="http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/mvc.html#mvc-ann-return-types">Supported method return types</A>
 */
public class ControllerOperationCollector extends DefaultOperationCollector {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    /**
     * The name of the {@link OperationMap} used to encode the return value
     * if it is a  {@link Model}, {@link ModelMap}, {@link Map} and/or {@link ModelAndView}
     */
    public static final String	RETURN_VALUE_MODEL_MAP="returnModel";
    /**
     * The name of the (optional) property holding the returned view name
     * if it is a {@link String}, {@link View} or {@link ModelAndView}
     */
    public static final String	RETURN_VALUE_VIEW_NAME="returnView";

    public ControllerOperationCollector() {
		super();
	}

	@Override
	protected void processNormalExit(Operation op, Object returnValue) {
		op.putAnyNonEmpty(RETURN_VALUE_VIEW_NAME, resolveViewName(returnValue));
		if (collectExtraInformation()) {
			collectModelInformation(op, RETURN_VALUE_MODEL_MAP, returnValue);
		}
	}

	/**
	 * Checks if the value is a {@link String}, {@link View} or {@link ModelAndView}
	 * and resolves the view's name. <B>Note:</B> for a {@link View} the simple
	 * class name is returned as its name
	 * @param value The value to be checked
	 * @return The resolved view name - <code>null</code> if none
	 */
	static final String resolveViewName (Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof View) {
			return value.getClass().getSimpleName();
		} else if (value instanceof ModelAndView) {
			return ((ModelAndView) value).getViewName();
		} else {
			return null;
		}
	}
	/**
	 * Goes over all the arguments until it encounters a {@link Model},
	 * {@link ModelMap}, {@link Map} and/or {@link ModelAndView}. If such an argument
	 * is encountered then its contents are encoded in an {@link OperationMap}
	 * @param op The {@link Operation} in which to encode the model
	 * @param modelName The name for the created {@link OperationMap}
	 * @param args The arguments to be checked - <B>Note:</B> the <U>first</U>
	 * argument 
	 * @return The created {@link OperationMap} - <code>null</code> if no
	 * {@link Model}, {@link ModelMap}, {@link Map} and/or {@link ModelAndView} found.
	 */
	static final OperationMap collectModelInformation (Operation op, String modelName, Object ... args) {
		if (ArrayUtil.length(args) <= 0) {
			return null;
		}

		for (Object argVal : args) {
			if (argVal instanceof Model) {
				return collectModelMapInformation(op, modelName, ((Model) argVal).asMap());
			} else if (argVal instanceof ModelMap) {
				return collectModelMapInformation(op, modelName, (ModelMap) argVal);
			} else if (argVal instanceof ModelAndView) {
				return collectModelMapInformation(op, modelName, ((ModelAndView) argVal).getModel());
			} else if (argVal instanceof Map<?,?>) {
				return collectModelMapInformation(op, modelName, (Map<?,?>) argVal);
			}
		}

		return null;
	}

	/**
	 * Encodes the model attributes {@link Map} into a {@link OperationMap}
	 * @param op The {@link Operation} in which to encode the model
	 * @param modelName The name for the created {@link OperationMap}
	 * @param modelMap The model attributes {@link Map} - may be <code>null</code>/empty,
	 * in which case an empty {@link OperationMap} is returned
	 * @return The created {@link OperationMap}
	 */
	static final OperationMap collectModelMapInformation (Operation op, String modelName, Map<?,?> modelMap) {
		return collectModelMapInformation(op.createMap(modelName), modelMap);
	}

	static final OperationMap collectModelMapInformation (OperationMap map, Map<?,?> modelMap) {
		if (MapUtil.size(modelMap) <= 0) {
			return map;
		}

		for (Map.Entry<?,?> ee : modelMap.entrySet()) {
			String	key=String.valueOf(ee.getKey());
			Object	value=resolveCollectedValue(ee.getValue());
			if (value instanceof String) {
				map.put(key, (String) value);
			} else {
				map.putAny(key, value);
			}
		}

		return map;
	}

	static final Object resolveCollectedValue (Object value) {
		if (StringFormatterUtils.isPrimitiveWrapper(value)) {
			return value;
		} else {
			return StringUtil.chopTailAndEllipsify(String.valueOf(value), StringFormatterUtils.MAX_PARAM_LENGTH);
		}
	}

	static final boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }
}

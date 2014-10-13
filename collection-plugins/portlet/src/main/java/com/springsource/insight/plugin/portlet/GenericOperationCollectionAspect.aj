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

package com.springsource.insight.plugin.portlet;

import java.util.Arrays;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

/**
 * This aspect create insight operation for general Portlet
 */
abstract aspect GenericOperationCollectionAspect extends AbstractOperationCollectionAspect {
    /**
     * Placeholder used if portlet name could not be resolved
     */
    public static final String UNKNOWN_PORTLET_NAME = "<unknown>";

    protected GenericOperationCollectionAspect() {
        super();
    }

    @Override
    public final String getPluginName() {
        return PortletPluginRuntimeDescriptor.NAME;
    }

    protected Operation createOperation(JoinPoint jp, OperationCollectionTypes opType) {
        String portletName = null;
        Object jpThis = jp.getThis();
        if (jpThis instanceof GenericPortlet) {
            GenericPortlet portlet = (GenericPortlet) jpThis;
            portletName = portlet.getPortletName();
        }

        if (StringUtil.isEmpty(portletName)) {
            portletName = UNKNOWN_PORTLET_NAME;
        }

        Object[] args = jp.getArgs();
        PortletRequest req = (PortletRequest) args[0];
        PortletPreferences preferences = req.getPreferences();
        Map<String, String[]> preferencesMap = preferences.getMap();
        Map<String, String[]> params = req.getParameterMap();

        Operation operation = new Operation().type(opType.type)
                .label("Portlet: '" + portletName + "' [" + opType.label + "]")
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("name", portletName)
                .putAnyNonEmpty("scheme", req.getScheme())
                .putAnyNonEmpty("server", req.getServerName())
                .put("port", req.getServerPort())
                .putAnyNonEmpty("contextPath", req.getContextPath())
                .putAnyNonEmpty(OperationFields.URI, createRequestURI(req))
                .put("mode", String.valueOf(req.getPortletMode()))
                .put("winState", String.valueOf(req.getWindowState()));

        try {
            //portlet2 support
            operation.putAnyNonEmpty("winId", req.getWindowID());
        } catch (Error e) {
            // ignored
        }

        createMap(operation, "preferences", preferencesMap);
        createMap(operation, "params", params);

        return operation;
    }

    static String createRequestURI(PortletRequest req) {
        if (req == null) {
            return null;
        }

        String scheme = req.getScheme(), server = req.getServerName();
        if (StringUtil.isEmpty(scheme) || StringUtil.isEmpty(server)) {
            return null;
        }

        int port = req.getServerPort();
        String contextPath = req.getContextPath();
        StringBuilder sb = new StringBuilder(scheme.length() + 4 + server.length() + 6 + StringUtil.getSafeLength(contextPath));
        sb.append(scheme).append("://").append(server);
        if (port > 0) {
            sb.append(':').append(port);
        }

        if (StringUtil.getSafeLength(contextPath) > 0) {
            sb.append(contextPath);
        }

        return sb.toString();
    }

    static OperationMap createMap(Operation op, String name, Map<String, String[]> values) {
        if ((values == null) || values.isEmpty()) {
            return null;
        }

        OperationMap opMap = op.createMap(name);
        for (Map.Entry<String, String[]> item : values.entrySet()) {
            String key = item.getKey();
            String[] value = item.getValue();
            if (value == null) {
                value = ArrayUtil.EMPTY_STRINGS;
            }
            opMap.put(key, Arrays.toString(value));
        }

        return opMap;
    }
}

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
package com.springsource.insight.plugin.socket;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.logging.InsightLogManager;

/**
 * Need it as a separate class mainly due to testing issues - but it is also more convenient
 */
class SocketCollectOperationContext implements CollectionSettingsUpdateListener {
    /**
     * The {@link CollectionSettingName} used to configured obscured address patterns
     */
    public static final CollectionSettingName OBSCURED_ADDRESSES_PATTERN_SETTING =
            new CollectionSettingName("obscured.addresses.pattern", SocketPluginRuntimeDescriptor.PLUGIN_NAME, "Regexp used to obscure addresses");
    /**
     * Special setting value used to signal that no obscuring pattern is required
     */
    public static final String NO_PATTERN_VALUE = "NONE";

    private volatile Pattern obscuredAddressesPattern /* =null - i.e., no obscuring */;
    private ObscuredValueMarker obscuredMarker;
    private HttpObfuscator obfuscator;

    public SocketCollectOperationContext() {
        this(HttpObfuscator.getInstance());

        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(this);
        registry.register(OBSCURED_ADDRESSES_PATTERN_SETTING, NO_PATTERN_VALUE);
    }

    SocketCollectOperationContext(HttpObfuscator hdrsObfuscator) {
        obfuscator = hdrsObfuscator;
        obscuredMarker = hdrsObfuscator.getSensitiveValueMarker();
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (OBSCURED_ADDRESSES_PATTERN_SETTING.equals(name)) {
            String curValue = (obscuredAddressesPattern == null) ? NO_PATTERN_VALUE : obscuredAddressesPattern.pattern();
            String newValue = StringUtil.safeToString(value);
            if (StringUtil.safeCompare(curValue, newValue) != 0) {
                Pattern newPattern = (StringUtil.isEmpty(newValue) || NO_PATTERN_VALUE.equalsIgnoreCase(newValue))
                        ? null
                        : CollectionSettingsRegistry.getPatternSettingValue(value);
                InsightLogManager.getLogger(getClass().getName())
                        .info("incrementalUpdate(" + name + "): " + curValue + " => " + newValue);
                obscuredAddressesPattern = newPattern;
            }
        } else if (HttpObfuscator.OBFUSCATED_HEADERS_SETTING.equals(name)) {
            obfuscator.incrementalUpdate(name, value);    // make sure change is propagated
        }
    }

    ObscuredValueMarker getObscuredValueMarker() {
        return obscuredMarker;
    }

    void setObscuredValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }

    /**
     * @param addr Address value to be checked if requires obfuscation
     * @return <code>true</code> if address has been marked as obscured value
     */
    boolean updateObscuredAddressValue(String addr) {
        if (StringUtil.isEmpty(addr) || (obscuredAddressesPattern == null)) {
            return false;
        }

        Matcher matcher = obscuredAddressesPattern.matcher(addr);
        if (matcher.matches()) {
            obscuredMarker.markObscured(addr);
            return true;
        }

        return false;
    }

    boolean updateObscuredHeaderValue(String name, String value) {
        if (!obfuscator.processHeader(name, value)) {
            return false;
        }

        ObscuredValueMarker curMarker = getObscuredValueMarker();
        ObscuredValueMarker httpMarker = obfuscator.getSensitiveValueMarker();
        /*
         * Check if substituted the marker (e.g., for addresses obscuring).
         * If so, then inform the substituted marker as well of the
         * obscured header value
         */
        if (curMarker != httpMarker) {
            curMarker.markObscured(value);
        }

        return true;
    }
}

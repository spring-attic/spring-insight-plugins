/**
 * Copyright 2009-2010 the original author or authors.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * Need it as a separate class mainly due to testing issues - but it is also more convenient
 */
class SocketCollectOperationContext {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    public static final CollectionSettingName    OBSCURED_ADDRESSES_PATTERN_SETTING=
            new CollectionSettingName("obscured.addresses.pattern", "socket", "Regexp used to obscure addresses");
    protected static volatile Pattern  OBSCURED_ADDRESSES_PATTERN /* =null - i.e., no obscuring */;

    static {
        CollectionSettingsRegistry  registry=CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
            public void incrementalUpdate(CollectionSettingName name, Serializable value) {
               Logger   LOG=Logger.getLogger(SocketCollectOperationContext.class.getName());
               if (OBSCURED_ADDRESSES_PATTERN_SETTING.equals(name)) {
                   Pattern  newPattern=CollectionSettingsRegistry.getPatternSettingValue(value);
                   LOG.info("incrementalUpdate(" + name + "): " + OBSCURED_ADDRESSES_PATTERN + " => " + value);
                   OBSCURED_ADDRESSES_PATTERN = newPattern;
               } else if (LOG.isLoggable(Level.FINE)) {
                   LOG.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
               }
            }
        });
    }

    private ObscuredValueMarker obscuredMarker =
            new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder());

    public SocketCollectOperationContext() {
        super();
    }

    ObscuredValueMarker getObscuredValueMarker () {
        return obscuredMarker;
    }

    void setObscuredValueMarker (ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }

    /**
     * @param addr Address value to be checked if requires obfuscation
     * @return <code>true</code> if address has been marked as obscured value
     */
    boolean updateObscuredAddressValue (String addr) {
        if ((addr != null) && (addr.length() > 0) && (OBSCURED_ADDRESSES_PATTERN != null)) {
            Matcher matcher=OBSCURED_ADDRESSES_PATTERN.matcher(addr);
            if (matcher.matches()) {
                obscuredMarker.markObscured(addr);
                return true;
            }
        }
        
        return false;
    }

}

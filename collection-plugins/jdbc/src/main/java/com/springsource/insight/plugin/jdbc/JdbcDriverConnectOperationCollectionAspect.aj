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
package com.springsource.insight.plugin.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.util.StringUtil;

/**
 * Captures the calls to {@link Driver#connect(String, java.util.Properties)}
 * so we can also run some metrics on them
 */
public aspect JdbcDriverConnectOperationCollectionAspect extends AbstractOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    static final CollectionSettingName  OBFUSCATED_PROPERTIES_SETTING=
            new CollectionSettingName("obfuscated.connect.properties", "jdbc", "Comma separated list driver connection properties to be obscured");
    /**
     * Default properties being obfuscated
     * @see Driver#connect(String, Properties)
     */
    static final String DEFAULT_OBFUSCATED_PROPERTIES_LIST="user,password";
    // NOTE: using a synchronized set in order to allow modification while running
    static final Set<String>    OBFUSCATED_PROPERTIES=
            Collections.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
                private static final long serialVersionUID = 1L;

                {
                    addAll(StringUtil.explode(DEFAULT_OBFUSCATED_PROPERTIES_LIST, ","));
                }
            });
    // register a collection setting update listener to update the obfuscated headers
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
                public void incrementalUpdate (CollectionSettingName name, Serializable value) {
                   Logger   LOG=Logger.getLogger(JdbcDriverConnectOperationCollectionAspect.class.getName());
                   if (OBFUSCATED_PROPERTIES_SETTING.equals(name) && (value instanceof String)) {
                       if (OBFUSCATED_PROPERTIES.size() > 0) { // check if replacing or populating
                           LOG.info("incrementalUpdate(" + name + ")" + OBFUSCATED_PROPERTIES + " => [" + value + "]");
                           OBFUSCATED_PROPERTIES.clear();
                       }

                       OBFUSCATED_PROPERTIES.addAll(StringUtil.explode((String) value, ","));
                   } else if (LOG.isLoggable(Level.FINE)) {
                       LOG.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
                   }
                }
            });
    }

    private ObscuredValueMarker obscuredMarker =
            new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder());

    public JdbcDriverConnectOperationCollectionAspect () {
        super(new JdbcDriverConnectOperationCollector());
    }

    @Override
    public String getPluginName() {
        return "jdbc";
    }

    ObscuredValueMarker getSensitiveValueMarker () {
        return obscuredMarker;
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }

    public pointcut connect () : execution(* Driver+.connect(String,Properties));
    public pointcut collectionPoint() : connect();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object      target=jp.getTarget();
        Object[]    args=jp.getArgs();
        String      url=(String) args[0];
        Operation   op=ConnectionsTracker.createOperation(jp, url, "create")
                            .put("driverClass", target.getClass().getName())
                            ;
        if (collectExtraInformation()) {
            OperationMap    props=addConnectionProperties(op, (Properties) args[1]);
            for (String key : props.keySet()) {
                if (OBFUSCATED_PROPERTIES.contains(key)) {
                    Object  value=props.get(key);
                    obscuredMarker.markObscured(value);
                }
            }
        }

        return op;
    }

    boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    static OperationMap addConnectionProperties (Operation op, Properties props) {
        if ((props == null) || props.isEmpty()) {   // OK if no properties specified...
            return null;
        }

        OperationMap    connProps=op.createMap("params");
        for (Map.Entry<?,?> pe : props.entrySet()) {
            Object  key=pe.getKey(), value=pe.getValue();
            if (!(key instanceof String)) {
                continue;
            }

            connProps.putAnyNonEmpty((String) key, value);
        }
        
        return connProps;
    }

    static class JdbcDriverConnectOperationCollector extends DefaultOperationCollector {
        private final ConnectionsTracker    tracker=ConnectionsTracker.getInstance();
        JdbcDriverConnectOperationCollector () {
            super();
        }

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            if (returnValue instanceof Connection) {
                tracker.startTracking((Connection) returnValue, op);
            }
        }
    }
}

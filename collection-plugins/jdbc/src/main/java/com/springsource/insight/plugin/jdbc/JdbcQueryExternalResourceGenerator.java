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
package com.springsource.insight.plugin.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.plugin.names.CollectionSettingNames;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.logging.AbstractLoggingClass;

public final class JdbcQueryExternalResourceGenerator extends AbstractLoggingClass implements CollectionSettingsUpdateListener {
    private final CollectionSettingsRegistry registry;
    private final Collection<ApplicationName> disabledApps = Collections.synchronizedSet(new TreeSet<ApplicationName>());
    private final Collection<ApplicationName> knownApps = Collections.synchronizedSet(new TreeSet<ApplicationName>());
    private final AtomicBoolean active = new AtomicBoolean();

    private static final class LazyFieldHolder {
        @SuppressWarnings("synthetic-access")
        static final JdbcQueryExternalResourceGenerator INSTANCE = new JdbcQueryExternalResourceGenerator();
    }

    public static final JdbcQueryExternalResourceGenerator getInstance() {
        return LazyFieldHolder.INSTANCE;
    }

    private JdbcQueryExternalResourceGenerator() {
        this(CollectionSettingsRegistry.getInstance());
    }

    // package visibility for unit tests
    JdbcQueryExternalResourceGenerator(CollectionSettingsRegistry reg) {
        if ((registry = reg) == null) {
            throw new IllegalStateException("No registry provide");
        }

        registry.addListener(this);
        registry.register(CollectionSettingNames.CS_QUERY_EXRTERNAL_RESOURCE_NAME, Boolean.FALSE);
    }

    public Collection<ExternalResourceDescriptor> createAndAddQueryExternalResourceDescriptors(Collection<ExternalResourceDescriptor> dbDescriptors, Trace trace) {
        ApplicationName appName = trace.getAppName();
        registerApplicationNameIfNeeded(appName);

        if (!(active.get() && shouldGenerateQueryExternalResources(dbDescriptors, appName))) {
            return dbDescriptors;
        }

        Collection<ExternalResourceDescriptor> newCollection = new ArrayList<ExternalResourceDescriptor>(dbDescriptors);

        for (ExternalResourceDescriptor dbDescriptor : dbDescriptors) {
            Frame frame = dbDescriptor.getFrame();

            if (frame == null) {
                continue;
            }

            Operation op = frame.getOperation();

            if (op == null) {
                continue;
            }

            String sql = extractSqlFromOperation(op);

            if (!StringUtil.isEmpty(sql)) {
                String jdbcHash = MD5NameGenerator.getName(sql);

                ExternalResourceDescriptor queryDescriptor = new ExternalResourceDescriptor(
                        frame,
                        dbDescriptor.getName() + ":" + jdbcHash,
                        sql,
                        ExternalResourceType.QUERY.name(),
                        dbDescriptor.getVendor(),
                        dbDescriptor.getHost(),
                        dbDescriptor.getPort(),
                        dbDescriptor.getColor(),
                        dbDescriptor.isIncoming(),
                        dbDescriptor);

                dbDescriptor.setChildren(Collections.singletonList(queryDescriptor));
                newCollection.add(queryDescriptor);
            }
        }

        return newCollection;
    }

    private boolean shouldGenerateQueryExternalResources(Collection<ExternalResourceDescriptor> dbDescriptors, ApplicationName appName) {
        if (ListUtil.size(dbDescriptors) == 0) {
            return false;
        }

        return !disabledApps.contains(appName);
    }

    private boolean registerApplicationNameIfNeeded(ApplicationName appName) {
        if (knownApps.add(appName)) {
            CollectionSettingName name = CollectionSettingNames.createApplicationCollectionSettingName(appName);
            registry.register(name, Boolean.TRUE);

            return true;
        } else {
            return false;
        }
    }

    public final void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if ((name == null) || (value == null)) {
            return;
        }

        if (CollectionSettingNames.CS_QUERY_EXRTERNAL_RESOURCE_NAME.equals(name)) {
            boolean newValue = CollectionSettingsRegistry.getBooleanSettingValue(value);
            boolean prevValue = active.getAndSet(newValue);
            if (prevValue != newValue) {
                _logger.info("incrementalUpdate(" + name + ") " + prevValue + " => " + newValue);
            }
            return;
        }

        String key = name.getKey();
        if (!key.startsWith(CollectionSettingNames.APP_QUERY_EXRTERNAL_RESOURCE_KEY_NAME)) {
            return;
        }

        String appNameStr = key.substring(CollectionSettingNames.APP_QUERY_EXRTERNAL_RESOURCE_KEY_NAME.length());
        ApplicationName appName = ApplicationName.valueOf(appNameStr);
        if (knownApps.add(appName)) {
            _logger.info("incrementalUpdate(" + appName + ") new application");
        }

        boolean newEnabledValue = CollectionSettingsRegistry.getBooleanSettingValue(value);
        boolean prevEnabledValue = newEnabledValue ? disabledApps.remove(appName) : disabledApps.add(appName);
        if (prevEnabledValue != newEnabledValue) {
            _logger.info("incrementalUpdate(" + appName + ") " + prevEnabledValue + " => " + newEnabledValue);
        }
    }

    public static String extractSqlFromOperation(Operation op) {
        if (op == null) {
            return null;
        }

        String sql = op.get("sql", String.class);

        if (!StringUtil.isEmpty(sql)) {
            return sql;
        }

        OperationType type = op.getType();

        if (type.equals(JdbcDriverExternalResourceAnalyzer.TYPE)) {
            return op.getLabel();
        } else {
            return null;
        }

    }

    public boolean isGeneratingExternalResources() {
        return active.get();
    }

    public boolean isGeneratingExternalResourceForApplication(ApplicationName appName) {
        return isGeneratingExternalResources() && !disabledApps.contains(appName);
    }

    public Collection<ApplicationName> getDisabledApplicationNames() {
        return Collections.unmodifiableCollection(disabledApps);
    }

    public Collection<ApplicationName> getKnownApplicationNames() {
        return Collections.unmodifiableCollection(knownApps);
    }

    public boolean isApplicationNameKnown(ApplicationName appName) {
        return knownApps.contains(appName);
    }
}

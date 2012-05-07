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
package com.springsource.insight.plugin.hibernate;

import com.springsource.insight.idk.config.OperationGroupDefinition;
import com.springsource.insight.idk.config.OperationViewDefinition;
import com.springsource.insight.intercept.plugin.PluginDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hibernate Plugin Configuration Bean
 * An alternative to XML Configuration
 *
 * Three beans must be defined by this configuration:
 *    PluginDescriptor informs the agent and dashboard about your plugin
 *    OperationGroupDefinition associates the plugin with a group
 *    OperationViewDefinition associates the plugin with a view
 *
 * We should be able to simplify this to a single bean.
 */
@Configuration
public class HibernateConfig {
    private static final String PLUGIN_NAME = "hibernate";

    // Create statically so it can be quickly retrieved quickly
    private static final PluginDescriptor plugin
            = new PluginDescriptor(PLUGIN_NAME, "1.9.0-CI-SNAPSHOT", "VMware", "http://www.springsource.org/insight");

    @Bean
    public PluginDescriptor plugin() {
        return plugin;
    }

    @Bean
    public OperationGroupDefinition group() {
        return new OperationGroupDefinition("Database", PLUGIN_NAME);
    }

    // The view definition requires a specific bean name in the form of operation.[plugin]
    @Bean(name = OperationViewDefinition.OPERATION_BEAN_NAME_PREFIX + PLUGIN_NAME)
    public OperationViewDefinition view() {
        return new OperationViewDefinition(PLUGIN_NAME, "com/springsource/insight/plugin/hibernate/hibernate.ftl");
    }
}

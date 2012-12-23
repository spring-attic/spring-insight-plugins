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
import java.util.logging.Logger;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * Creates DB external resources and if needed will also create a QUERY external resources as children
 */
public class JdbcOperationExternalResourceAnalyzer extends DatabaseJDBCURIAnalyzer implements CollectionSettingsUpdateListener {
    public static final OperationType   TYPE=OperationType.valueOf("jdbc");
    
    public static final String PLUGINS          = "plugins";
    public static final String GENERATE_STR     = JdbcRuntimePluginDescriptor.PLUGIN_NAME + ".generate-query-external-resource";
    public static final String GENERATE_APP_STR = GENERATE_STR + ".application";
    
    public static final CollectionSettingName CS_NAME = new CollectionSettingName(GENERATE_STR + ".active", PLUGINS);
    
    public static final CollectionSettingName APP_CS_NAME = new CollectionSettingName(GENERATE_APP_STR, PLUGINS);
    
    public static final String APP_KEY_NAME = APP_CS_NAME.getKey() + ".";
    
    private static final JdbcOperationExternalResourceAnalyzer INSTANCE = new JdbcOperationExternalResourceAnalyzer();
    
    private final CollectionSettingsRegistry registry;
    private final Collection<ApplicationName> disabledApps;
    private final Collection<ApplicationName> knownApps;
    private final AtomicBoolean active;
    
    private JdbcOperationExternalResourceAnalyzer() {
        this(CollectionSettingsRegistry.getInstance());
    }
    
    //package visibility for unit tests
    JdbcOperationExternalResourceAnalyzer(CollectionSettingsRegistry reg) {
        super(TYPE);
        this.registry = reg;
        
        this.disabledApps = Collections.synchronizedSet(new TreeSet<ApplicationName>());
        this.knownApps = Collections.synchronizedSet(new TreeSet<ApplicationName>());
        this.active = new AtomicBoolean();
        
        initListener();
    }
    
    private void initListener() {
    	registry.addListener(this);
    	
    	Boolean value = null;
    	
    	try {
    		value = registry.getBooleanSetting(CS_NAME);
    	} catch (RuntimeException e) {
    		Logger.getLogger(getClass().getName()).warning("initListener() - invalid value [" + registry.get(CS_NAME) + "] for [" + CS_NAME  + "]");
    	}
    	
    	if (value == null) {
    		registry.set(CS_NAME, Boolean.FALSE);
    	} else {
    		active.set(value.booleanValue());
    	}
    }

    public static final JdbcOperationExternalResourceAnalyzer getInstance() {
    	return INSTANCE;
    }
    
    @Override
    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> dbFrames) {
    	return createAndAddQueryExternalResourceDescriptors(super.locateExternalResourceName(trace, dbFrames), trace);
    }
    
    private Collection<ExternalResourceDescriptor> createAndAddQueryExternalResourceDescriptors(Collection<ExternalResourceDescriptor> dbDescriptors, Trace trace) {
    	ApplicationName appName = trace.getAppName();
    	registerApplicationNameIfNeeded(appName);
    	
    	if (!(active.get() && shouldGenerateQueryExternalResources(dbDescriptors, appName))) {
    		return dbDescriptors;
    	}
    	
    	Collection<ExternalResourceDescriptor> newCollection = new ArrayList<ExternalResourceDescriptor>(dbDescriptors);
    	
    	for(ExternalResourceDescriptor dbDescriptor : dbDescriptors) {
    		Frame frame = dbDescriptor.getFrame();
    		
    		if (frame == null) {
    			continue;
    		}
    		
    		Operation op = frame.getOperation();
    		
    		if (op == null) {
    			continue;
    		}
    		
    		String sql = op.get("sql", String.class);
    		
    		if (!StringUtil.isEmpty(sql)) {
    			String jdbcHash = MD5NameGenerator.getName(sql);
                
    			ExternalResourceDescriptor queryDescriptor=new ExternalResourceDescriptor(frame, 
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
    		CollectionSettingName name = createApplicationCollectionSettingName(appName);
    		registry.register(name, Boolean.TRUE);
    		
    		return true;
    	} else {
    		return false;
    	}
    }
    
	public final void incrementalUpdate(CollectionSettingName name, Serializable value) {
		if (name == null || value == null || !(value instanceof Boolean)) {
			return;
		}
		
		boolean booleanValue = ((Boolean) value).booleanValue();
		
		if (CS_NAME.equals(name)) {
			active.set(booleanValue);
		} else {
			String key = name.getKey();
			
			if (key.startsWith(APP_KEY_NAME)) {
				String appNameStr = key.substring(APP_KEY_NAME.length());
				
				ApplicationName appName = ApplicationName.valueOf(appNameStr);
				knownApps.add(appName);
				
				if (booleanValue) {
					disabledApps.remove(appName);
				} else {
					disabledApps.add(appName);
				}
			}
		}
	}
	
	static CollectionSettingName createApplicationCollectionSettingName(ApplicationName appName) {
		CollectionSettingName name = new CollectionSettingName(
												GENERATE_APP_STR + "." + appName.getName(), 
												PLUGINS);
		return name;
	}
	
	public final boolean isGeneratingExternalResources() {
		return active.get();
	}
	
	public final boolean isGeneratingExternalResourceForApplication(ApplicationName appName) {
		return isGeneratingExternalResources() && !disabledApps.contains(appName);
	}
	
	public final Collection<ApplicationName> getDisabledApplicationNames() {
		return Collections.unmodifiableCollection(disabledApps);
	}
	
	public final Collection<ApplicationName> getKnownApplicationNames() {
		return Collections.unmodifiableCollection(knownApps);
	}
	
	public final boolean isApplicationNameKnown(ApplicationName appName) {
		return knownApps.contains(appName);
	}
}

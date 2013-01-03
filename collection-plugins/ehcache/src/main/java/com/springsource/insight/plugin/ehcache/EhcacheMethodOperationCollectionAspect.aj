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
package com.springsource.insight.plugin.ehcache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.props.BeanPropertiesSource;

/**
 * 
 */
public abstract aspect EhcacheMethodOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
	private static BeanPropertiesSource	managerSource, configSource;
	protected final Logger	logger=Logger.getLogger(getClass().getName());

	static {
        try {
        	managerSource = new BeanPropertiesSource(CacheManager.class);
        } catch(Throwable t) {
        	Logger	LOG=Logger.getLogger(EhcacheMethodOperationCollectionAspect.class.getName());
        	LOG.warning("Failed (" + t.getClass().getSimpleName() + ")"
        			  + " to retrieve CacheManager properties: " + t.getMessage());
        }

        try {
        	configSource = new BeanPropertiesSource(CacheConfiguration.class);
        } catch(Throwable t) {
        	Logger	LOG=Logger.getLogger(EhcacheMethodOperationCollectionAspect.class.getName());
        	LOG.warning("Failed (" + t.getClass().getSimpleName() + ")"
        				+ " to retrieve CacheConfiguration properties: " + t.getMessage());
        }
	}

    protected EhcacheMethodOperationCollectionAspect() {
    	this(new DefaultOperationCollector());
    }

    protected EhcacheMethodOperationCollectionAspect(OperationCollector collector) {
        super(collector);
    }

    Operation initCommonFields (Operation op, Ehcache cache, String method, Object key) {
         op.type(EhcacheDefinitions.CACHE_OPERATION)
           .put(EhcacheDefinitions.METHOD_ATTRIBUTE, method)
           .putAnyNonEmpty(EhcacheDefinitions.KEY_ATTRIBUTE, (key!=null)?key.toString():null)
           .putAnyNonEmpty(EhcacheDefinitions.NAME_ATTRIBUTE, cache.getName())
           ;

         String	keyValue=op.get(EhcacheDefinitions.KEY_ATTRIBUTE, String.class);
         if (StringUtil.isEmpty(keyValue)) {
        	 op.label(method);
         } else {
        	 op.label(method + " " + keyValue);
         }

         if (collectExtraInformation()) {
        	 String	cacheName=op.get(EhcacheDefinitions.NAME_ATTRIBUTE,String.class);
        	 try {
        		 initCacheConfiguration(op.createMap(CacheConfiguration.class.getSimpleName()), cache.getCacheConfiguration());
        	 } catch(Error e) {
        		 logger.warning("initCommonFields(" + cacheName + ")[" + method + "]"
        				 	 + " failed (" + e.getClass().getSimpleName() + ")"
        				     + " to initialize cache configuration: " + e.getMessage());
        	 }

        	 try {
        		 initCacheManager(op.createMap(CacheManager.class.getSimpleName()), cache.getCacheManager());
        	 } catch(Error e) {
        		 logger.warning("initCommonFields(" + cacheName + ")[" + method + "]"
    				 	 	  + " failed (" + e.getClass().getSimpleName() + ")"
        				      + " to initialize manager configuration: " + e.getMessage());
        	 }
         }
         
         return op;
    }

    // extra properties not available to all ehcache versions
    static final List<String>	MANAGER_EXTRA_PROPS=
    		Collections.unmodifiableList(Arrays.asList("status", "diskStorePath", "activeConfigurationText"));
    OperationMap initCacheManager (OperationMap map, CacheManager manager) {
        // according to the Javadoc: "For a newly created cache this will be null until it has been added to a CacheManager"
        if (manager == null) {
            return map;
        } else {
        	return updateExtraProps(map.put(EhcacheDefinitions.NAME_ATTRIBUTE, manager.getName()), manager, managerSource, MANAGER_EXTRA_PROPS);
        }
    }

    // extra properties not available to all ehcache versions
    static final List<String>	CONFIG_EXTRA_PROPS=
    		Collections.unmodifiableList(Arrays.asList("maxElementsInMemory", "timeToLiveSeconds", "transactionalMode"));
    OperationMap initCacheConfiguration (OperationMap map, CacheConfiguration config) {
        if (config == null) { // can happen for programmatic initialization
            return map;
        } else {
        	return updateExtraProps(map, config, configSource, CONFIG_EXTRA_PROPS);
        }
    }

    OperationMap updateExtraProps (OperationMap map, Object target, BeanPropertiesSource beanSource, Collection<String> props) {
    	if (target == null) {
    		return map;
    	}

    	final Map<String,?>	valsMap;
    	try {
    		valsMap = beanSource.getProperties(target, props);
    		if (MapUtil.size(valsMap) <= 0) {
    			return map;
    		}
    	} catch(Exception e) {
    		Class<?>	beanClass=beanSource.getBeanClass();
    		logger.warning("updateExtraProps(" + beanClass.getSimpleName() + ")[" + props + "]"
    				     + " failed (" + e.getClass().getSimpleName() + ")"
    				     + " to retrieve values: " + e.getMessage());
    		return map;
    	}

    	for (Map.Entry<String,?> ve : valsMap.entrySet()) {
    		String	name=ve.getKey();
    		Object	value=ve.getValue();
    		map.putAnyNonEmpty(name, value);
    	}

    	return map;
    }

    public boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return EhcacheDefinitions.PLUGIN_NAME;
    }
    
    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }

}

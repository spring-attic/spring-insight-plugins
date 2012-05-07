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
package com.springsource.insight.plugin.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;

/**
 * 
 */
public abstract aspect EhcacheMethodOperationCollectionAspect extends MethodOperationCollectionAspect {
    protected EhcacheMethodOperationCollectionAspect() {
        super();
    }

    @Override
    public String getPluginName() {
        return EhcacheDefinitions.PLUGIN_NAME;
    }

    protected EhcacheMethodOperationCollectionAspect(OperationCollector collector) {
        super(collector);
    }

    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    Operation initCommonFields (Operation op, Ehcache cache, String method, Object key) {
         op.type(EhcacheDefinitions.CACHE_OPERATION)
           .put(EhcacheDefinitions.METHOD_ATTRIBUTE, method)
           .putAnyNonEmpty(EhcacheDefinitions.KEY_ATTRIBUTE, key)
           .label(method + " " + op.get(EhcacheDefinitions.KEY_ATTRIBUTE))
           .putAnyNonEmpty(EhcacheDefinitions.NAME_ATTRIBUTE, cache.getName())
           ;

         if (collectExtraInformation()) {
             initCacheConfiguration(op.createMap(CacheConfiguration.class.getSimpleName()), cache.getCacheConfiguration());
             initCacheManager(op.createMap(CacheManager.class.getSimpleName()), cache.getCacheManager());
         }
         
         return op;
    }

    OperationMap initCacheManager (OperationMap map, CacheManager manager) {
        // according to the Javadoc: "For a newly created cache this will be null until it has been added to a CacheManager"
        if (manager == null) {
            return map;
        }

        return map.put(EhcacheDefinitions.NAME_ATTRIBUTE, manager.getName())
                  .put("status", String.valueOf(manager.getStatus()))
                  // according to the Javadoc "This may be null if no caches need a DiskStore and none was configured"
                  .putAnyNonEmpty("diskStorePath", manager.getDiskStorePath())
                  .put("activeConfigText", manager.getActiveConfigurationText())
                  ;
    }

    OperationMap initCacheConfiguration (OperationMap map, CacheConfiguration config) {
        if (config == null) { // can happen for programmatic initialization
            return map;
        }

        // TODO add more properties of interest (no sense in adding all of them - too many)
        return map.putAnyNonEmpty("diskStorePath", config.getDiskStorePath())
                  .put("maxElementsInMemory", config.getMaxElementsInMemory())
                  .put("TTLSeconds", config.getTimeToLiveSeconds())
                  .putAnyNonEmpty("transactionalMode", config.getTransactionalMode())
                  ;
                  
    }

    public boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }
}

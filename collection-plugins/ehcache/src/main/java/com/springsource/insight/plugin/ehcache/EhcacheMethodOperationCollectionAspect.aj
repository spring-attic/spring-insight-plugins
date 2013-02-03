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

import net.sf.ehcache.Ehcache;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public abstract aspect EhcacheMethodOperationCollectionAspect extends MethodOperationCollectionAspect {
    protected EhcacheMethodOperationCollectionAspect() {
        this(new DefaultOperationCollector());
    }

    protected EhcacheMethodOperationCollectionAspect(final OperationCollector collector) {
        super(collector);
    }

    protected abstract pointcut ehcacheCollectionPoint();

    protected pointcut ehcacheExecutionCall() : execution(* Ehcache+.*(..));

    public final pointcut collectionPoint() : ehcacheCollectionPoint() && (!cflowbelow(ehcacheExecutionCall()));

    Operation initCommonFields(final Operation op, final Ehcache cache, final String method, final Object key) {
        op.type(EhcacheDefinitions.CACHE_OPERATION)
          .put(EhcacheDefinitions.METHOD_ATTRIBUTE, method)
          .putAnyNonEmpty(EhcacheDefinitions.KEY_ATTRIBUTE, (key != null) ? key.getClass().getSimpleName() : null)
          .putAnyNonEmpty(EhcacheDefinitions.NAME_ATTRIBUTE, cache.getName());

        final String keyValue = op.get(EhcacheDefinitions.KEY_ATTRIBUTE, String.class);
        if (StringUtil.isEmpty(keyValue)) {
            op.label(method);
        } else {
            op.label(method + " " + keyValue);
        }

        return op;
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

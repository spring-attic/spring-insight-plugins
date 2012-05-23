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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect EhcacheGetOperationCollectionAspect extends EhcacheMethodOperationCollectionAspect {
    public EhcacheGetOperationCollectionAspect () {
        super(new EhcacheGetOperationCollector());
    }

    @Override
    public String getPluginName() {
        return EhcacheDefinitions.PLUGIN_NAME;
    }

    public pointcut getValueFlow ()
        : execution(* Ehcache+.get(..))
       || execution(* Ehcache+.getQuiet(..))
        ;
    public pointcut collectionPoint ()
        : getValueFlow()
       && (!cflowbelow(getValueFlow()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createGetOperation(super.createOperation(jp).type(EhcacheDefinitions.CACHE_OPERATION),
                                  (Ehcache) jp.getTarget(),
                                  jp.getArgs()[0]);
    }

    Operation createGetOperation (Operation op, Ehcache cache, Object key) {
        return initCommonFields(op, cache, EhcacheDefinitions.GET_METHOD, key);
    }

    static class EhcacheGetOperationCollector extends DefaultOperationCollector {
        public EhcacheGetOperationCollector() {
            super();
        }

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            if (returnValue instanceof Element) {
            	Object value=((Element) returnValue).getObjectValue();
                op.putAnyNonEmpty(EhcacheDefinitions.VALUE_ATTRIBUTE, (value!=null)?value.toString():null);
            }
            super.processNormalExit(op, returnValue);
        }
    }
}

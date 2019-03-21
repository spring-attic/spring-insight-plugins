/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
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

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public aspect EhcachePutOperationCollectionAspect extends EhcacheMethodOperationCollectionAspect {
	public EhcachePutOperationCollectionAspect () {
		super();
	}

    public pointcut ehcacheCollectionPoint ()
        : execution(* Ehcache+.put(..))
       || execution(* Ehcache+.putWithWriter(..))
       || execution(* Ehcache+.putQuiet(..))
       || execution(* Ehcache+.putIfAbsent(..))
        ;

    @Override
    protected Operation createOperation(final JoinPoint jp) {
        return createPutOperation(super.createOperation(jp), (Ehcache) jp.getTarget(), (Element) jp.getArgs()[0]);
    }

    Operation createPutOperation (final Operation op, final Ehcache cache, final Element elem) {
    	final Object value=elem.getObjectValue();
        return initCommonFields(op, cache, EhcacheDefinitions.PUT_METHOD, elem.getObjectKey())
                    .putAnyNonEmpty(EhcacheDefinitions.VALUE_ATTRIBUTE, (value!=null)?value.getClass().getSimpleName():null)
                    ;
    }
}

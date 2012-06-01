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

import org.aspectj.lang.JoinPoint;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect EhcachePutOperationCollectionAspect extends EhcacheMethodOperationCollectionAspect {

    public pointcut putValueFlow ()
        : execution(* Ehcache+.put(..))
       || execution(* Ehcache+.putWithWriter(..))
       || execution(* Ehcache+.putQuiet(..))
       || execution(* Ehcache+.putIfAbsent(..))
        ;

    public pointcut collectionPoint ()
        : putValueFlow()
       && (!cflowbelow(putValueFlow()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createPutOperation(super.createOperation(jp), (Ehcache) jp.getTarget(), (Element) jp.getArgs()[0]);
    }

    Operation createPutOperation (Operation op, Ehcache cache, Element elem) {
    	Object value=elem.getObjectValue();
        return initCommonFields(op, cache, EhcacheDefinitions.PUT_METHOD, elem.getObjectKey())
                    .putAnyNonEmpty(EhcacheDefinitions.VALUE_ATTRIBUTE, (value!=null)?value.toString():null)
                    ;
    }
}

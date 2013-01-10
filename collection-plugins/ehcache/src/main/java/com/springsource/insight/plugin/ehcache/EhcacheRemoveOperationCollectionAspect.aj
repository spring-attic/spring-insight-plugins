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
import net.sf.ehcache.Element;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect EhcacheRemoveOperationCollectionAspect extends EhcacheMethodOperationCollectionAspect {
	public EhcacheRemoveOperationCollectionAspect () {
		super();
	}

    public pointcut removeValueFlow ()
        : execution(* Ehcache+.remove(..))
       || execution(* Ehcache+.removeQuiet(..))
       || execution(* Ehcache+.removeWithWriter(Object))
       || execution(* Ehcache+.removeElement(Element))
        ;

    public pointcut ehcacheCollectionPoint ()
        : removeValueFlow()
       && (!cflowbelow(removeValueFlow()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createRemoveOperation(super.createOperation(jp), (Ehcache) jp.getTarget(), jp.getArgs()[0]);
    }

    Operation createRemoveOperation (Operation op, Ehcache cache, Object obj) {
        Object key=(obj instanceof Element) ? ((Element) obj).getObjectKey() : obj;
        return initCommonFields(op, cache, EhcacheDefinitions.REM_METHOD, key);
    }

}

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

package com.springsource.insight.plugin.jndi;

import javax.naming.Context;
import javax.naming.Name;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public aspect JndiBindOperationCollectionAspect extends JndiOperationCollectionSupport {
	public JndiBindOperationCollectionAspect () {
		super(JndiEndpointAnalyzer.BIND);
	}
    /*
     * Using call instead of execution since usually JDK core classes are used
     * - e.g., InitialDirContext - and we cannot instrument them
     */
	public pointcut bindCalls ()
		: call(* Context+.bind(String,Object))
	   || call(* Context+.bind(Name,Object))
	   || call(* Context+.rebind(String,Object))
	   || call(* Context+.rebind(Name,Object))
	   || call(* Context+.unbind(String))
	   || call(* Context+.unbind(Name))
		;

	// NOTE: we use cflowbelow because the methods might delegate to one another
	public pointcut collectionPoint()
		: bindCalls() && (!cflowbelow(bindCalls()))
		;

	@Override
	protected Operation createOperation(JoinPoint jp) {
		Operation	op=super.createOperation(jp);
		Object[]	args=jp.getArgs();
		if (ArrayUtil.length(args) > 1) {
			Object	value=args[1];
			op.put("value", StringUtil.trimWithEllipsis(StringUtil.safeToString(value), StringFormatterUtils.MAX_PARAM_LENGTH));
		}

		return op;
	}
}

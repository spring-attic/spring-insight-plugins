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

package com.springsource.insight.plugin.jndi;

import javax.naming.Context;
import javax.naming.Name;

/**
 * Intercepts {@link Context} <code>lookup</code> and <code>lookupLink</code> calls
 */
public aspect JndiLookupOperationCollectionAspect extends JndiOperationCollectionSupport {
	public JndiLookupOperationCollectionAspect () {
		super(JndiEndpointAnalyzer.LOOKUP);
	}

    /*
     * Using call instead of execution since usually JDK core classes are used
     * - e.g., InitialDirContext - and we cannot instrument them
     */
	public pointcut lookupCalls ()
		: call(* Context+.lookup(String))
	   || call(* Context+.lookup(Name))
	   || call(* Context+.lookupLink(String))
	   || call(* Context+.lookupLink(Name))
		;

	// NOTE: we use cflowbelow because the methods might delegate to one another
	public pointcut collectionPoint()
		: lookupCalls() && (!cflowbelow(lookupCalls()))
		;
}

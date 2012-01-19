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

package com.springsource.insight.plugin.gemfire;

import org.aspectj.lang.JoinPoint;

import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.springsource.insight.intercept.operation.Operation;

public aspect GemFireQueryCollectionAspect extends AbstractGemFireCollectionAspect {
	
    public pointcut collectionPoint(): execution(* Query.execute*(..));

    public GemFireQueryCollectionAspect() {
		super(GemFireDefenitions.TYPE_QUERY);
	}
    
    @Override
    protected Operation createOperation(final JoinPoint jp) {
    	Operation op = createBasicOperation(jp);
   
    	DefaultQuery query = (DefaultQuery) jp.getThis();
        op.put(GemFireDefenitions.FIELD_QUERY, query.getQueryString());
        
        
        return op;
    }
}

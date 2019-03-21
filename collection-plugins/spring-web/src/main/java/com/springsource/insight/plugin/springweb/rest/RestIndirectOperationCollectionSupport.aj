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

package com.springsource.insight.plugin.springweb.rest;

import org.aspectj.lang.JoinPoint;
import org.springframework.http.HttpMethod;

import com.springsource.insight.util.StringUtil;

/**
 * Used for <code>exchange</code> and <code>execute</code> where method
 * is specified as an extra argument
 */
public abstract aspect RestIndirectOperationCollectionSupport extends RestOperationCollectionSupport {
	protected RestIndirectOperationCollectionSupport (String accessType) {
		super(accessType);
	}
	
	@Override
	protected String resolveOperationMethod(JoinPoint jp) {
		Object[]	args=jp.getArgs();
		Object		method=args[1];	// all exchange methods have the method as the 2nd argument
		if (method instanceof HttpMethod) {
			return ((HttpMethod) method).name();
		} else {
			return super.resolveOperationMethod(jp);
		}
	}
	
	@Override
	protected String resolveOperationLabel(String method, String uri, JoinPoint jp) {
		return createIndirectLabel(getMethod(), method, super.resolveOperationLabel(method, uri, jp));
	}
	
	static String createIndirectLabel (String aspectMethod, String opMethod, String orgLabel) {
		if (StringUtil.safeCompare(aspectMethod, opMethod, false) == 0) {
			return orgLabel;
		} else {
			return new StringBuilder(StringUtil.getSafeLength(aspectMethod) + 2 + StringUtil.getSafeLength(orgLabel))
						.append(aspectMethod)
						.append(": ")
						.append(orgLabel)
					.toString();
		}
	}
}

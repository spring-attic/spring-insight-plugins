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

package com.springsource.insight.plugin.jndi;

import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.collection.TrailingAbstractOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public abstract aspect JndiOperationCollectionSupport extends TrailingAbstractOperationCollectionAspect {
    protected static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    protected static final String UNKNOWN_NAME="<unknown>";
    protected final OperationType	type;
    protected final JndiResourceCollectionFilter filter;
    
	protected JndiOperationCollectionSupport (OperationType opType) {
		this(opType, JndiResourceCollectionFilter.getIntance());
	}

	protected JndiOperationCollectionSupport (OperationType opType, JndiResourceCollectionFilter resourceFilter) {
		if ((type=opType) == null) {
			throw new IllegalStateException("No operation type specified");
		}

		if ((filter=resourceFilter) == null) {
			throw new IllegalStateException("No resource filter specified");
		}
	}

	@Override
	protected Operation createOperation(JoinPoint jp) {
		String		name=getName(jp);
		if (StringUtil.isEmpty(name) || (!filter.accept(name))) {
			return null;
		}

		Signature	sig=jp.getSignature();
		String		action=sig.getName();
		Operation	op=new Operation()
							.type(type)
							.sourceCodeLocation(getSourceCodeLocation(jp))
							.label(StringUtil.capitalize(action) + " " + (StringUtil.isEmpty(name) ? UNKNOWN_NAME : name))
							.put("action", action)
							.putAnyNonEmpty("name", name)
							;
		if (collectExtraInformation()) {
			Context	ctx=(Context) jp.getTarget();
			try {
				Map<?,?>		env=ctx.getEnvironment();
				OperationMap	map=op.createMap("environment");
				map.putAnyAll(env);
			} catch(NamingException e) {
				// ignored
			}
		}

		return op;
	}

	protected boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

	protected String getName (JoinPoint jp) {
		return getName(jp.getArgs());
	}

	protected String getName (Object ... args) {
		if (ArrayUtil.length(args) <= 0) {
			return null;
		}

		Object	arg0=args[0];
		if ((arg0 instanceof String) || (arg0 instanceof Name)) {
			return arg0.toString();
		} else {
			return null;
		}
	}

	@Override
	public final String getPluginName() {
		return JndiPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}

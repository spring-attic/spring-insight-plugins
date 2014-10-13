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

package com.springsource.insight.plugin.jmx;

import javax.management.Attribute;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringFormatterUtils;

/**
 *
 */
public aspect JmxSetSingleAttributeOperationCollectionAspect extends JmxSingleAttributeOperationCollectionSupport {
    public JmxSetSingleAttributeOperationCollectionAspect() {
        super(JmxPluginRuntimeDescriptor.SET_ACTION);
    }

	/* We use cflowbelow in case calls are delegated - theoretically, one
	 * might make a case against the cflowbelow - e.g., if the server accesses
	 * some other attributes or transforms the name. However, this is considered
	 * (a) highly unlikely, (b) not really useful information and (c) considerable
	 * trace size increase
	 */
    public pointcut collectionPoint()
            : setAttributeValue()
            && (!cflowbelow(setAttributeValue()))
            && (!cflowbelow(setAttributesList()))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Attribute attr = ArrayUtil.findFirstInstanceOf(Attribute.class, args);
        String name = (attr == null) ? null : attr.getName();
        Object value = (attr == null) ? null : attr.getValue();
        return createAttributeOperation(jp, getObjectName(args), name)
                .putAnyNonEmpty(JmxPluginRuntimeDescriptor.ATTR_VALUE_PROP, StringFormatterUtils.formatObject(value))
                ;
    }

}

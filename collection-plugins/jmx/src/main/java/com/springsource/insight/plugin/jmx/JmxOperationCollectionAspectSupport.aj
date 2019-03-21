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

package com.springsource.insight.plugin.jmx;

import javax.management.ObjectName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;

/**
 * Serves as base class for all JMX instrumentation aspects
 */
public abstract aspect JmxOperationCollectionAspectSupport extends MethodOperationCollectionAspect {
    protected JmxOperationCollectionAspectSupport() {
        super();
    }

    protected JmxOperationCollectionAspectSupport(OperationCollector collector) {
        super(collector);
    }

    protected Operation createBeanOperation(JoinPoint jp, ObjectName name) {
        Operation op = super.createOperation(jp);
        putObjectName(op, name);
        return op;
    }

    protected String putObjectName(Operation op, ObjectName name) {
        String canonName = (name == null) ? null : name.getCanonicalName();
        op.putAnyNonEmpty(JmxPluginRuntimeDescriptor.BEAN_NAME_PROP, canonName);
        return canonName;
    }

    protected ObjectName getObjectName(JoinPoint jp) {
        return getObjectName(jp.getArgs());
    }

    protected ObjectName getObjectName(Object... args) {
        return ArrayUtil.findFirstInstanceOf(ObjectName.class, args);
    }

    @Override
    public final String getPluginName() {
        return JmxPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}

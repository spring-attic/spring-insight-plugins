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

package com.springsource.insight.plugin.springcore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.ReflectionUtils;

/**
 *
 */
public abstract aspect SpringEventReferenceCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
    static final Set<Class<?>> nonContextEvents = Collections.synchronizedSet(new HashSet<Class<?>>());
    static final Map<Class<?>, Method> contextMethods = Collections.synchronizedMap(new HashMap<Class<?>, Method>());

    protected SpringEventReferenceCollectionAspect(OperationType opType) {
        super(opType);
    }

    @Override
    protected String resolveEventData(Object event) {
        if (event == null) {
            return Object.class.getName();
        }

        Class<?> eventType = event.getClass();
        ApplicationContext context = getApplicationContext(event);
        if (context != null) {
            return eventType.getSimpleName() + ": " + context.getDisplayName();
        } else {
            return eventType.getName();
        }
    }

    /**
     * Due to <A HREF="https://issuetracker.springsource.com/browse/METRICS-3169">METRICS-3169</A>
     * and in order to support pre-<I>3.x</I> versions we need to extract the
     * application context using reflection API
     * @param event The event that <U>might</U> be carrying an application context
     * @return The extracted {@link ApplicationContext} - <code>null</code>
     * if none available
     */
    static ApplicationContext getApplicationContext(Object event) {
        if (!(event instanceof ApplicationEvent)) {
            return null;
        }

        Class<?> eventType = event.getClass();
        if (nonContextEvents.contains(eventType)) {
            return null;
        }

        Method method = contextMethods.get(eventType);
        if (method == null) {
            if ((method = findContextMethod(eventType)) == null) {
                nonContextEvents.add(eventType);
            } else {
                contextMethods.put(eventType, method);
            }
        }

        if (method == null) {
            return null;
        }

        try {
            return ExtraReflectionUtils.invoke(method, event, ApplicationContext.class);
        } catch (RuntimeException e) {
            nonContextEvents.add(eventType);    // mark it as unreliable from here on
            return null;
        }
    }

    static Method findContextMethod(Class<?> eventType) {
        if (eventType == null) {
            return null;
        }

        Method method = ReflectionUtils.findMethod(eventType, "getApplicationContext", ArrayUtil.EMPTY_CLASSES);
        if (method == null) {
            return null;
        }

        int mod = method.getModifiers();
        if (Modifier.isStatic(mod) || (!Modifier.isPublic(mod))) {
            return null;
        }

        Class<?> returnType = method.getReturnType();
        if (!ApplicationContext.class.isAssignableFrom(returnType)) {
            return null;
        }

        return method;
    }
}

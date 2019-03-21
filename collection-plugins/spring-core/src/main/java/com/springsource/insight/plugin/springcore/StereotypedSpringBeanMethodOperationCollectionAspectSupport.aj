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

package com.springsource.insight.plugin.springcore;

import java.lang.annotation.Annotation;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public abstract aspect StereotypedSpringBeanMethodOperationCollectionAspectSupport
        extends SpringCoreOperationCollectionAspect {
    public static final String COMP_TYPE_ATTR = "componentType";
    protected final Class<? extends Annotation> stereoTypeClass;

    protected StereotypedSpringBeanMethodOperationCollectionAspectSupport(Class<? extends Annotation> annClass) {
        if ((stereoTypeClass = annClass) == null) {
            throw new IllegalStateException("No stereotype class provided");
        }
    }

    protected pointcut excludedLifecyclePointcuts()
            : ApplicationListenerMethodOperationCollectionAspect.appListener()
            || InitializingBeanOperationCollectionAspect.beanInitialization()
            || EventPublisingOperationCollectionAspect.publishingPoint()
            || AnnotationDrivenMethodOperationCollectionAspect.collectionPoint()
            /*
            * We exclude all Insight beans since if we want insight-on-insight we
            * cannot use this aspect as it may cause infinite recursion
            */
            || within(com.springsource.insight..*)
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp)
                .put(COMP_TYPE_ATTR, stereoTypeClass.getSimpleName())
                ;
    }
}

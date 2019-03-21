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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.InitializingBean;

import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;

public aspect InitializingBeanOperationCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
    public InitializingBeanOperationCollectionAspect() {
        super(SpringLifecycleMethodEndPointAnalyzer.BEAN_LIFECYLE_TYPE);
    }

    public pointcut afterPropertiesSet()
        : execution(* InitializingBean+.afterPropertiesSet());

    public pointcut postConstruct()
        : execution(@PostConstruct * *(..));

    public pointcut beanInitialization()
    	: afterPropertiesSet() || postConstruct();

    public pointcut collectionPoint() :
    	beanInitialization() &&
        !AnnotationDrivenMethodOperationCollectionAspect.collectionPoint();

	@Override
	protected String resolveEventData(Object event) {
		return PostConstruct.class.getSimpleName();
	}
}


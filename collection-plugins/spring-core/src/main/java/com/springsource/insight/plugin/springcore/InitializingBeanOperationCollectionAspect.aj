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

package com.springsource.insight.plugin.springcore;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import org.springframework.beans.factory.InitializingBean;
import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;

import javax.annotation.PostConstruct;

public aspect InitializingBeanOperationCollectionAspect extends MethodOperationCollectionAspect {
    public pointcut afterPropertiesSet()
        : execution(* InitializingBean+.afterPropertiesSet(..));

    public pointcut postConstruct()
        : execution(@PostConstruct * *(..));

    public pointcut collectionPoint() :
        (afterPropertiesSet() || postConstruct()) &&
        !AnnotationDrivenMethodOperationCollectionAspect.collectionPoint();

    @Override
    public String getPluginName() {
        return "spring-core";
    }
}


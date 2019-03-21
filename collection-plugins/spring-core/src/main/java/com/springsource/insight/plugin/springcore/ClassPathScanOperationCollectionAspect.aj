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

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.springsource.insight.util.StringUtil;


public privileged aspect ClassPathScanOperationCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
    public ClassPathScanOperationCollectionAspect() {
        super(SpringCorePluginRuntimeDescriptor.CLASSPATH_SCAN_TYPE);
    }

    public pointcut findPathMatchingResources()
            : execution(* PathMatchingResourcePatternResolver+.findPathMatchingResources(String));

    public pointcut findCandidateComponents()
            : execution(* ClassPathScanningCandidateComponentProvider+.findCandidateComponents(String));

    public pointcut collectionPoint()
            : findPathMatchingResources()
            || findCandidateComponents()
            ;

    @Override
    protected String resolveEventData(Object event) {
        String location = StringUtil.safeToString(event);
        if (StringUtil.isEmpty(location)) {
            return "<unknown>";
        } else {
            return location;
        }
    }
}

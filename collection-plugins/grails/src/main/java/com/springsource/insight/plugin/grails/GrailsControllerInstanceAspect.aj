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

package com.springsource.insight.plugin.grails;

import java.util.List;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import com.springsource.insight.util.KeyValPair;
import com.springsource.insight.util.StringFormatterUtils;

import groovy.lang.GroovyObject;

/**
 * This aspect works in combination with the GrailsControllerMetricCollectionAspect.  
 * 
 * This aspect steals off the action parameters before the handler method would be invoked.
 */
public aspect GrailsControllerInstanceAspect {

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(GroovyObject controllerInstance) : GrailsControllerPointcuts.getControllerInstanceMethod(controllerInstance) {
        GrailsParameterMap paramsMap = (GrailsParameterMap)controllerInstance.getProperty("params");
        List<KeyValPair<String, String>>params = StringFormatterUtils.formatMapAsSorted(paramsMap);
        GrailsControllerStateKeeper.setThreadLocalActionParams(params);
    }
}

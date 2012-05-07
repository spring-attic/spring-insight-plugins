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

package com.springsource.insight.plugin.grails;

import groovy.lang.GroovyObject;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.springframework.web.servlet.ModelAndView;

public aspect GrailsControllerPointcuts {

    public pointcut getControllerClassMethod() 
        : execution(GrailsControllerClass
                    (GrailsControllerHelper || org.codehaus.groovy.grails.web.servlet.mvc.AbstractGrailsControllerHelper)+.getControllerClassByURI(String));
                                              
    public pointcut handleURIMethod()
        : execution(ModelAndView
                    (GrailsControllerHelper || org.codehaus.groovy.grails.web.servlet.mvc.AbstractGrailsControllerHelper)+.handleURI(String, GrailsWebRequest, Map));

    public pointcut getControllerInstanceMethod(GroovyObject controllerInstance)
        : execution(Object
                    (GrailsControllerHelper || org.codehaus.groovy.grails.web.servlet.mvc.AbstractGrailsControllerHelper)+.handleAction(GroovyObject, *, HttpServletRequest, HttpServletResponse, Map))
            && args(controllerInstance, *, HttpServletRequest, HttpServletResponse, Map);
}

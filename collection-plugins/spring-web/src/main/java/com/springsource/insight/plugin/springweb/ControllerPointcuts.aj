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

package com.springsource.insight.plugin.springweb;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public aspect ControllerPointcuts {
    public pointcut controllerMethod() 
        : execution(* (@Controller *).*(..));
    
    // Specifying generic parameters causes a mismatch for Spring 2.5 applications
    public pointcut renderView() 
        : execution(void View.render(Map/*<String, ?>*/, HttpServletRequest, HttpServletResponse));
    
    public pointcut resolveView() 
        : execution(View ViewResolver.resolveViewName(String, Locale));
    
    public pointcut initBinder() 
        : execution(@InitBinder * *(..)); 
    
    
    public pointcut modelAttributeRetrieval()
        : execution(@ModelAttribute !@RequestMapping !void *(..));

    public pointcut validation()
        : execution(void Validator.validate(Object, Errors));
    
    public pointcut controllerHandlerMethod()
        : execution(@RequestMapping * *(..));
        
}

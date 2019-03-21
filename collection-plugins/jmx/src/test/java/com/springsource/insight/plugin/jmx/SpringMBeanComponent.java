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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component(SpringMBeanComponent.BEAN_NAME)
@ManagedResource(objectName = SpringMBeanComponent.RESOURCE_NAME, description = "Test Spring MBean")
public class SpringMBeanComponent {
    public static final String DOMAIN_NAME = "com.springsource.insight.plugin.jmx";
    public static final String BEAN_NAME = "SpringMBeanComponent";
    public static final String RESOURCE_NAME = DOMAIN_NAME + ":name=" + BEAN_NAME;

    private Number number = Integer.valueOf(0);
    private String str = "";
    private final Logger logger;

    public SpringMBeanComponent() {
        logger = LoggerFactory.getLogger(getClass());
    }

    @ManagedAttribute(description = "Number value getter")
    public Number getNumberValue() {
        return number;
    }

    @ManagedAttribute(description = "Number value setter")
    public void setNumberValue(Number n) {
        number = n;
        logger.info("setNumberValue(" + n + ")");
    }

    @ManagedAttribute(description = "String value getter")
    public String getStringValue() {
        return str;
    }

    @ManagedAttribute(description = "String value setter")
    public void setStringValue(String s) {
        str = s;
        logger.info("setStringValue(" + s + ")");
    }

    @ManagedOperation(description = "Updates both values")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "number", description = "Number value"),
            @ManagedOperationParameter(name = "string", description = "String value")
    })
    public void updateValues(Number n, String s) {
        number = n;
        str = s;
        logger.info("updateValues(" + n + "/" + s + ")");
    }
}

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

package com.foo.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

/**
 *
 */
public abstract class AbstractBean implements Runnable,
        ApplicationListener<ApplicationEvent>,
        InitializingBean,
        ApplicationEventPublisher,
        ApplicationEventMulticaster {
    protected final Log logger = LogFactory.getLog(getClass());

    protected AbstractBean() {
        super();
    }

    public void afterPropertiesSet() throws Exception {
        logger.info("afterPropertiesSet()");
    }

    @SuppressWarnings("rawtypes")
    public void addApplicationListener(ApplicationListener listener) {
        // ignored
    }

    public void addApplicationListenerBean(String listenerBeanName) {
        // ignored
    }

    @SuppressWarnings("rawtypes")
    public void removeApplicationListener(ApplicationListener listener) {
        // ignored
    }

    public void removeApplicationListenerBean(String listenerBeanName) {
        // ignored
    }

    public void removeAllListeners() {
        // ignored
    }

    public void multicastEvent(ApplicationEvent event) {
        logger.info("multicastEvent(" + event.getClass().getSimpleName() + ")@" + event.getTimestamp());
    }

    public void publishEvent(ApplicationEvent event) {
        logger.info("publishEvent(" + event.getClass().getSimpleName() + ")@" + event.getTimestamp());
    }

    public void onApplicationEvent(ApplicationEvent event) {
        logger.info("onApplicationEvent(" + event.getClass().getSimpleName() + ")@" + event.getTimestamp());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

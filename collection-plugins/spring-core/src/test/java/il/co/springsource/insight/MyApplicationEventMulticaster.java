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

package il.co.springsource.insight;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

/**
 * 
 */
public class MyApplicationEventMulticaster
		extends MyEventHolder
		implements ApplicationEventMulticaster {

	public MyApplicationEventMulticaster() {
		super();
	}

	public void addApplicationListener(@SuppressWarnings("rawtypes") ApplicationListener listener) {
		// ignored
	}

	public void addApplicationListenerBean(String listenerBeanName) {
		// ignored
	}

	public void removeApplicationListener(@SuppressWarnings("rawtypes") ApplicationListener listener) {
		// ignored
	}

	public void removeApplicationListenerBean(String listenerBeanName) {
		// ignored
	}

	public void removeAllListeners() {
		// ignored
	}

	public void multicastEvent(ApplicationEvent event) {
		lastEvent = event;
	}
}

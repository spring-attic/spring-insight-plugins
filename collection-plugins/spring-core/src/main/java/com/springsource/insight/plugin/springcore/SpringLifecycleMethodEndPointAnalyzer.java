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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.endpoint.AbstractEndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public class SpringLifecycleMethodEndPointAnalyzer extends AbstractEndPointAnalyzer {
    /**
     * The <U>static</U> score assigned to operations that do not contain
     * interesting enough Spring core API(s) - it is just slightly
     * above that of a servlet and/or queue operation
     */
	public static final int	LIFECYCLE_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE + 1;

	public static final OperationType	APP_LISTENER_TYPE=OperationType.valueOf("spring_app_listener");
	public static final OperationType	CLASSPATH_SCAN_TYPE=OperationType.valueOf("spring_classpath_scan");
	public static final OperationType	EVENT_PUBLISH_TYPE=OperationType.valueOf("spring_event_publish");
	public static final OperationType	BEAN_LIFECYLE_TYPE=OperationType.valueOf("spring_bean_lifecycle");

	public static final List<OperationType>	TYPES=	// NOTE: order is important
			Collections.unmodifiableList(Arrays.asList(CLASSPATH_SCAN_TYPE, APP_LISTENER_TYPE, EVENT_PUBLISH_TYPE, BEAN_LIFECYLE_TYPE));

	private static final SpringLifecycleMethodEndPointAnalyzer	INSTANCE=new SpringLifecycleMethodEndPointAnalyzer();

	private SpringLifecycleMethodEndPointAnalyzer () {
		super(TYPES);
	}

	public static final SpringLifecycleMethodEndPointAnalyzer getInstance() {
		return INSTANCE;
	}

	@Override
	protected int getDefaultScore(int depth) {
		return LIFECYCLE_SCORE;
	}
}

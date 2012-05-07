/**
 * Copyright 2009-2010 the original author or authors.
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
package com.springsource.insight.plugin.apache.http.hc3;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public final class HttpClientDefinitions {
    private HttpClientDefinitions() {
        // no instance
    }

    public static final OperationType   TYPE=OperationType.valueOf("apache-hc3");
    /**
     * Special HTTP method name used as placeholder in operations where the
     * <I>HttpMethod</I> value was <code>null</code>
     */
    public static final String  PLACEHOLDER_METHOD_NAME="<PLACEHOLDER>";
    /**
     * Special HTTP method URI value used as placeholder in operations where the
     * <I>HttpMethod</I> value was <code>null</code>
     */
    public static final String  PLACEHOLDER_URI_VALUE="http://127.0.0.1/placeholder";
    /**
     * Placeholder status code value used in case the <code>execute</code>
     * failed in the <code>around</code> advice due to an exception
     */
    public static final int FAILED_CALL_STATUS_CODE=(-1);
}

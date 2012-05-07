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
package com.springsource.insight.plugin.jaxrs;

/**
 */
public final class RestServiceDefinitions {
    private RestServiceDefinitions () {
        // no instance
    }

    public static final String  ROOT_PATH="rest-test",
                                NOW_PARAM_NAME="now",
                                YESTERDAY_PATH="yesterday/{" + NOW_PARAM_NAME + "}",
                                TOMORROW_PATH="tomorrow/{" + NOW_PARAM_NAME + "}";
}

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

package com.springsource.insight.plugin.ehcache;

import com.springsource.insight.intercept.operation.OperationType;

/**
 *
 */
public final class EhcacheDefinitions {
    private EhcacheDefinitions() {
        // no instance
    }

    public static final String PLUGIN_NAME = "ehcache";
    public static final OperationType CACHE_OPERATION = OperationType.valueOf("ehcache-operation");
    public static final String VENDOR_NAME = PLUGIN_NAME;

    // various names of fields and values used in cache operation encoding
    public static final String KEY_ATTRIBUTE = "key",
            VALUE_ATTRIBUTE = "value",
            NAME_ATTRIBUTE = "name",
            METHOD_ATTRIBUTE = "method",
            GET_METHOD = "Ehcache Get",
            PUT_METHOD = "Ehcache Put",
            REM_METHOD = "Ehcache Remove",
            RPL_METHOD = "Ehcache Replace";
}

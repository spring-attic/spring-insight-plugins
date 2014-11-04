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

package com.springsource.insight.plugin.rmi;

import com.springsource.insight.intercept.operation.OperationType;

/**
 *
 */
public final class RmiDefinitions {

    /**
     *
     */
    private RmiDefinitions() {
        throw new UnsupportedOperationException("Instance NA");
    }

    public static final OperationType RMI_ACTION = OperationType.valueOf("rmi-action");
    public static final OperationType RMI_LIST = OperationType.valueOf("rmi-list");

    public static final String ACTION_ATTR = "action";

    public static final String NAME_ATTR = "name";

    public static final String LIST_ATTR = "list";
}

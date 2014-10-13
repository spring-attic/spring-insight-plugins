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

package com.springsource.insight.plugin.springweb.controller;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebEndPointAnalyzer;

/**
 * This trace analyzer simply looks at a Trace and returns a
 * ControllerEndPointAnalysis about what it found.
 * <p/>
 * For a trace to be analyzed, it must be of the following format:
 * <p/>
 * - HttpOperation
 * ..
 * .. (arbitrary nesting)
 * .. ControllerMethodOperation
 */
public class ControllerEndPointAnalyzer extends AbstractSpringWebEndPointAnalyzer {
    public static final OperationType CONTROLLER_METHOD_TYPE = OperationType.valueOf("controller_method");
    /**
     * The property used to mark legacy controller operations
     */
    public static final String LEGACY_PROPNAME = "legacyController";
    /**
     * The <U>static</U> score assigned to legacy controllers - it is just slightly
     * above that of a servlet and/or queue operation
     */
    public static final int LEGACY_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE + 1;
    public static final int DEFAULT_CONTROLLER_SCORE = 0;
    private static final ControllerEndPointAnalyzer INSTANCE = new ControllerEndPointAnalyzer();

    private ControllerEndPointAnalyzer() {
        super(CONTROLLER_METHOD_TYPE);
    }

    public static final ControllerEndPointAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    protected int getOperationScore(Operation op, int depth) {
        Boolean legacyFlag = op.get(LEGACY_PROPNAME, Boolean.class);
        if ((legacyFlag != null) && legacyFlag.booleanValue()) {
            return LEGACY_SCORE;
        } else {
            return super.getOperationScore(op, depth);
        }
    }

    @Override
    protected int getDefaultScore(int depth) {
        return DEFAULT_CONTROLLER_SCORE;
    }
}

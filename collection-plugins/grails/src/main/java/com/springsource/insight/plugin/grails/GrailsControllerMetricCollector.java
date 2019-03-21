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

package com.springsource.insight.plugin.grails;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.plugin.grails.GrailsControllerStateKeeper.State;
import com.springsource.insight.util.KeyValPair;

/**
 *
 */
public class GrailsControllerMetricCollector extends DefaultOperationCollector {
    public static final String UNKNOWN_ACTION = "unknownAction",
            UNKNOWN_CONTROLLER = "UnknownController";

    GrailsControllerMetricCollector() {
        super();
    }

    @Override
    public void processNormalExit(Operation op, Object returnValue) {
        State state = GrailsControllerStateKeeper.getAndDestroyThreadLocalState();
        op.label(buildLabel(state))
                .sourceCodeLocation(buildSourceCodeLocation(state))
        ;

        OperationList actionParams = op.createList("actionParams");
        if ((state != null) && (state.actionParams != null)) {
            for (KeyValPair<String, String> param : state.actionParams) {
                actionParams.createMap().put("key", param.getKey()).put("value", param.getValue());
            }
        }
    }

    @Override
    protected void processAbnormalExit(Operation op, Throwable throwable) {
        State state = GrailsControllerStateKeeper.getAndDestroyThreadLocalState();
        op.label(buildLabel(state))
                .sourceCodeLocation(buildSourceCodeLocation(state))
        ;
    }

    static String buildLabel(State state) {
        String actionName = getActionName(state);
        /*
         * NOTE:
         *  (1) state may be null if GrailsControllerStateKeeper.getAndDestroyThreadLocalState() called
         *  without an initial getState() call
         *  
         *  (2) state.shortControllerName may be null if GrailsControllerStateKeeper.setThreadLocalController
         *   not called
         */
        if ((state != null) && (state.shortControllerName != null)) {
            return state.shortControllerName + "#" + actionName;
        } else {
            return UNKNOWN_CONTROLLER + "#" + actionName;
        }
    }

    static SourceCodeLocation buildSourceCodeLocation(State state) {
        String actionName = getActionName(state);
        /*
         * NOTE:
         *  (1) state may be null if GrailsControllerStateKeeper.getAndDestroyThreadLocalState() called
         *  without an initial getState() call
         *  
         *  (2) state.fullControllerName may be null if GrailsControllerStateKeeper.setThreadLocalController
         *  not called
         */
        if ((state != null) && (state.fullControllerName != null)) {
            return new SourceCodeLocation(state.fullControllerName, actionName, 1);
        } else {
            return new SourceCodeLocation(UNKNOWN_CONTROLLER, actionName, 1);
        }
    }

    static String getActionName(State state) {
        /*
         * NOTE:
         *  (1) state may be null if GrailsControllerStateKeeper.getAndDestroyThreadLocalState() called
         *  without an initial getState() call
         *  
         *  (2) state.request may be null if GrailsControllerStateKeeper.setThreadLocalWebRequest
         *  not called
         */
        GrailsWebRequest request = (state == null) ? null : state.request;
        String actionName = (request == null) ? null : request.getActionName();
        if (actionName == null) {
            actionName = UNKNOWN_ACTION;
        }

        return actionName;
    }
}

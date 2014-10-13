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

package com.springsource.insight.plugin.grails;

import java.util.List;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;

import com.springsource.insight.util.KeyValPair;

public final class GrailsControllerStateKeeper {
    private static final ThreadLocal<State> localData = new ThreadLocal<State>();

    private GrailsControllerStateKeeper() {
        throw new UnsupportedOperationException("No instance");
    }

    public static void setThreadLocalController(String shortName, String fullName) {
        State state = getState();
        state.shortControllerName = shortName;
        state.fullControllerName = fullName;
    }

    public static void setThreadLocalActionParams(List<KeyValPair<String, String>> params) {
        getState().actionParams = params;
    }

    public static void setThreadLocalWebRequest(GrailsWebRequest request) {
        getState().request = request;
    }

    static State getState() {
        State state = localData.get();
        if (state == null) {
            state = new State();
            localData.set(state);
        }
        return state;
    }

    public static State getAndDestroyThreadLocalState() {
        State res = localData.get();
        localData.remove();
        return res;
    }

    public static class State {
        String shortControllerName;
        String fullControllerName;
        List<KeyValPair<String, String>> actionParams;
        GrailsWebRequest request;
    }
}

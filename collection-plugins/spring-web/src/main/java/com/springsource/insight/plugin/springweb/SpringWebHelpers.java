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

package com.springsource.insight.plugin.springweb;


import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;

import java.net.URI;

public class SpringWebHelpers {

    private SpringWebHelpers() {

    }

    public static int resolvePort(URI uri) {
        if (uri == null) {
            return (-1);
        }

        int port = uri.getPort();
        if (port <= 0) {
            if ("http".equals(uri.getScheme()))
                port = 80;
            else
                port = 443;
        }

        return port;
    }

    public static String createName(String name) {
        return MD5NameGenerator.getName(name);
    }

    public static Operation getRootFrameOperation(Frame start) {

        Frame rootFrame = null;
        if (start.isRoot())
            rootFrame = start;
        else
            rootFrame = FrameUtil.getRoot(start);

        if (rootFrame != null)
            return rootFrame.getOperation();
        return null;

    }
    public static String findUnresolvedURI(Operation rootFrameOperation, String resolvedURL) {

        OperationMap resolvedMap = rootFrameOperation.get(OperationFields.UNRESOLVED_URI, OperationMap.class);
        if (resolvedMap != null) {
            return resolvedMap.get(resolvedURL, String.class);
        }
        return null;
    }
}

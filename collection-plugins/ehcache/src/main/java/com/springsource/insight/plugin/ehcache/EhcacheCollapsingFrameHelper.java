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
package com.springsource.insight.plugin.ehcache;

import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.collapse.AbstractCollapsingFrameHelper;
import com.springsource.insight.intercept.trace.collapse.CollapsingFrameHelper;
import com.springsource.insight.util.ListUtil;

public final class EhcacheCollapsingFrameHelper extends AbstractCollapsingFrameHelper implements CollapsingFrameHelper {

    private static final EhcacheCollapsingFrameHelper INSTANCE = new EhcacheCollapsingFrameHelper();

    private EhcacheCollapsingFrameHelper() {
    }

    public static CollapsingFrameHelper getInstance() {
        return INSTANCE;
    }

    public boolean shouldCollapse(final Frame currentFrame) {
        final Frame parentFrame = currentFrame.getParent();
        final List<Frame> children = parentFrame.getChildren();
        if (ListUtil.size(children) < 2) {
            return false;
        }
        final Frame lastChildFrame = children.get(children.size() - 2);
        return sameMethod(currentFrame, lastChildFrame);
    }

    private boolean sameMethod(final Frame currentFrame, final Frame lastChildFrame) {
        final Operation lastOperation = lastChildFrame.getOperation();
        final String lastMethod = getMethodAttribute(lastOperation);
        final Operation currentOperation = currentFrame.getOperation();
        final String currentMethod = getMethodAttribute(currentOperation);
        return lastMethod.equals(currentMethod);
    }

    private String getMethodAttribute(final Operation operation) {
        return operation.get(EhcacheDefinitions.METHOD_ATTRIBUTE, String.class, "");
    }

}

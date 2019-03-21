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
package com.springsource.insight.plugin.jcr;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import com.springsource.insight.util.ArrayUtil;

public final class JCRCollectionUtils {
    private JCRCollectionUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    public static String getWorkspaceName(Object targ) {
        try {
            if (targ instanceof Item) {
                return getWorkspaceName(((Item) targ).getSession());
            } else if (targ instanceof Session) {
                return getWorkspaceName(((Session) targ).getWorkspace());
            } else if (targ instanceof Workspace) {
                return ((Workspace) targ).getName();
            }
        } catch (RepositoryException e) {
            //ignore
        }

        return null;
    }

    public static String safeToString(char... chars) {
        if (ArrayUtil.length(chars) <= 0) {
            return "";
        } else {
            return new String(chars);
        }
    }
}

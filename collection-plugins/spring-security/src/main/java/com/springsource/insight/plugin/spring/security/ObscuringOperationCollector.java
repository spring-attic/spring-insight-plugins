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

package com.springsource.insight.plugin.spring.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.util.ListUtil;

/**
 *
 */
public abstract class ObscuringOperationCollector extends DefaultOperationCollector {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    public static final String GRANTED_AUTHS_LIST_NAME = "grantedAuthorities";
    protected ObscuredValueMarker obscuredMarker;

    protected ObscuringOperationCollector() {
        this(new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder()));
    }

    protected ObscuringOperationCollector(ObscuredValueMarker marker) {
        setSensitiveValueMarker(marker);
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Obscured value marker cannot be null");
        }
        this.obscuredMarker = marker;
    }

    boolean collectExtraInformation() {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    // fill in some data from the return value
    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        markSensitiveReturnValueAttributes(op, returnValue);
    }

    protected abstract void markSensitiveReturnValueAttributes(Operation op, Object returnValue);

    static OperationList updateGrantedAuthorities(Operation op, Collection<? extends GrantedAuthority> authsList) {
        return updateGrantedAuthorities(op.createList(GRANTED_AUTHS_LIST_NAME), authsList);
    }

    static OperationList updateGrantedAuthorities(OperationMap op, Collection<? extends GrantedAuthority> authsList) {
        return updateGrantedAuthorities(op.createList(GRANTED_AUTHS_LIST_NAME), authsList);
    }

    static OperationList updateGrantedAuthorities(OperationList grantedList, Collection<? extends GrantedAuthority> authsList) {
        if (ListUtil.size(authsList) > 0) {
            for (final GrantedAuthority ga : authsList) {
                grantedList.add(ga.getAuthority());
            }
        }

        return grantedList;
    }
}

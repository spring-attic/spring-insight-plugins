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

package com.springsource.insight.plugin.gemfire;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.gemfire.GemFireDefenitions.GemFireType;
import com.springsource.insight.util.StringFormatterUtils;


public abstract aspect AbstractGemFireCollectionAspect extends AbstractOperationCollectionAspect {

    private OperationType type;
    private String labelPrefix;

    public AbstractGemFireCollectionAspect(GemFireType gemFireType) {
        this.type = gemFireType.getType();
        labelPrefix = new StringBuilder(GemFireDefenitions.GEMFIRE.length() + gemFireType.getLabel().length() + 3)
                .append(GemFireDefenitions.GEMFIRE)
                .append(": ")
                .append(gemFireType.getLabel())
                .append('.')
                .toString();
    }

    protected Operation createBasicOperation(final JoinPoint jp) {
        Signature sig = jp.getSignature();
        Object[] args = jp.getArgs();

        String label = createLabel(sig);
        Operation op = new Operation().label(label)
                .type(type)
                .sourceCodeLocation(getSourceCodeLocation(jp));

        if (addArgs()) {
            OperationList opList = op.createList("args");

            for (Object arg : args) {
                opList.add(StringFormatterUtils.formatObjectAndTrim(arg));
            }
        }

        return op;
    }

    protected String createLabel(final Signature sig) {
        String sigName = sig.getName();
        return new StringBuilder(labelPrefix.length() + sigName.length() + 2)
                .append(labelPrefix)
                .append(sig.getName())
                .append("()")
                .toString();
    }

    protected boolean addArgs() {
        return true;
    }

    @Override
    public String getPluginName() {
        return GemFireLightPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }


}

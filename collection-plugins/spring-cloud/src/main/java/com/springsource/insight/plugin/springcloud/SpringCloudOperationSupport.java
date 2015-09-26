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

package com.springsource.insight.plugin.springcloud;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.errorhandling.CollectionErrors;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.util.StringFormatterUtils;
import org.aspectj.lang.JoinPoint;

import java.util.logging.Level;

public class SpringCloudOperationSupport extends OperationCollectionAspectSupport {

    public SpringCloudOperationSupport(SpringCloudOperationCollector springCloudOperationCollector) {
        super(springCloudOperationCollector);
    }

    protected Operation fillInException(JoinPoint jp, Exception e) {

        //create a dummy operation to maintain a valid frame tree structure
        Operation operation = new Operation()
                .type(CollectionErrors.ERROR_TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(getClass().getSimpleName() + " - " + e.getClass().getSimpleName() + ": " + e.getMessage())
                .put(OperationFields.CLASS_NAME, getClass().getName())
                .put(OperationFields.SHORT_CLASS_NAME, JoinPointBreakDown.getShortClassName(getClass().getName()))
                .put(OperationFields.METHOD_NAME, "createOperation")
                .put(OperationFields.METHOD_SIGNATURE, JoinPointBreakDown.createMethodParamsSignature(JoinPoint.class))
                .put(OperationFields.EXCEPTION, StringFormatterUtils.formatStackTrace(e))
                ;

        JoinPoint.StaticPart staticPart = jp.getStaticPart();
        //log the exception
        _logger.log(Level.SEVERE, "Error swallowed in advice " + staticPart, e);

        //mark it
        CollectionErrors.markCollectionError(this, e);

        return operation;

    }

    @Override
    public String getPluginName() {
        return SpringCloudPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    protected static boolean contains(OperationList list, String item) {
        for(int i = 0; i < list.size(); i++) {
            String listItem = list.get(i, String.class);
            if (item.equals(listItem))
                return true;
        }
        return false;
    }


}

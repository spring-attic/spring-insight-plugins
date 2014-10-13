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

package com.springsource.insight.plugin.webflow;

import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.AnnotatedAction;


public privileged aspect OperationCollectionUtils {
    public OperationCollectionUtils() {
        super();
    }

    /*
	 * get action's expression string
	 * @param Action
	 */
    public static String getActionExpression(Action p_action) {
        String result = null;
        String expression = null;

        Action action = ((AnnotatedAction) p_action).getTargetAction();
        if (action instanceof EvaluateAction) {
            //evaluate action
            EvaluateAction evalAction = (EvaluateAction) action;
            expression = evalAction.expression.toString();
            try {
                if (evalAction.resultExpression != null)
                    result = evalAction.resultExpression.toString();
            } catch (Error e) {
                // evalAction.resultExpression is not exists in prev webflow API

                String tmp[] = evalAction.toString().split("(result =|,)");
                if (tmp.length > 2 && !"[null]".equals(tmp[2])) {
                    result = tmp[2];
                }
            }
        } else if (action instanceof SetAction) {
            // set action
            SetAction setAction = (SetAction) action;
            result = setAction.nameExpression.toString();
            expression = setAction.valueExpression.toString();
        } else {
            expression = action.toString();
        }

        if (result != null)
            expression = result + "=" + expression;

        return expression;
    }
}

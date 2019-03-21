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

package com.springsource.insight.plugin.struts2;

import org.apache.struts2.StrutsTestCase;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionProxy;
import com.springsource.insight.plugin.struts2.test.action.RegisterAction;
import com.springsource.insight.plugin.struts2.test.action.RegisterValidationAction;

/**
 * Struts2 tests set
 */
public class Struts2Tests extends StrutsTestCase {
    private static Struts2Tests instance = new Struts2Tests();

    public static Struts2Tests getInstance() {
        return instance;
    }

    private Struts2Tests() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed (" + e.getClass().getSimpleName() + ") to initialize the Struts2 context: " + e.getMessage());
        }
    }

    /**
     * tests Struts2 action flow without validation
     *
     * @throws Exception
     */
    public void testExecutePasses() throws Exception {
        super.initServletMockObjects();
        assertNotNull("The servlet context is not initiated", request);

        request.setParameter("personBean.firstName", "Bruce");
        request.setParameter("personBean.lastName", "Phillips");
        request.setParameter("personBean.email", "bphillips@ku.edu");
        request.setParameter("personBean.age", "19");
        ActionProxy actionProxy = getActionProxy("/register2.action");
        RegisterAction action = (RegisterAction) actionProxy.getAction();
        assertNotNull("The action is null but should not be.", action);

        String result = actionProxy.execute();
        assertEquals("The execute method did not return " + Action.SUCCESS + " but should have.", Action.SUCCESS, result);
    }

    /**
     * tests Struts2 action flow with validation
     *
     * @throws Exception
     */
    public void testExecuteValidationFailsMissingFirstName() throws Exception {
        super.initServletMockObjects();
        assertNotNull("The servlet context is not initiated", request);

        request.setParameter("personBean.lastName", "Phillips");
        request.setParameter("personBean.email", "bphillips@ku.edu");
        request.setParameter("personBean.age", "19");
        ActionProxy actionProxy = getActionProxy("/register.action");
        RegisterValidationAction action = (RegisterValidationAction) actionProxy.getAction();
        assertNotNull("The action is null but should not be.", action);

        String result = actionProxy.execute();
        assertEquals("The execute method did not return " + Action.INPUT + " but should have.", Action.INPUT, result);

    }
}

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

package com.springsource.insight.plugin.portlet;

import java.io.File;

import javax.portlet.PortletMode;

import net.sf.portletunit2.PortletUnitTestCase;
import net.sourceforge.jwebunit.junit.WebTester;

import org.apache.pluto.core.PortletServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

import com.springsource.insight.util.FileUtil;

/**
 * Portlet1 test case
 */
public class ExamplePortletTester extends PortletUnitTestCase {
	public static final String CONTEXT_NAME = "test";
	public static final String	TEST_HOST="localhost";
	public static final int	TEST_PORT=8989;
	public static final String	TEST_URL="http://" + TEST_HOST + ":" + TEST_PORT + "/" + CONTEXT_NAME;

	private Server server;
	
    public ExamplePortletTester() {
        super(ExamplePortlet.NAME);
    }
    
    @Override
	public void setUp() throws Exception {
    	if (tester==null) {
    		tester = new WebTester();
    	}

    	File	tmpDir=FileUtil.getTmpDir();
    	if (!tmpDir.exists()) {
    		if (!tmpDir.mkdirs()) {
    			System.err.println("Failed to ensure existence of " + tmpDir.getAbsolutePath());
    		} else {
    			System.out.println("Created " + tmpDir.getAbsolutePath());
    		}
    	}

    	System.setProperty("org.apache.pluto.embedded.portletId", ExamplePortlet.NAME);
        server = new Server(TEST_PORT);
        WebAppContext webapp = new WebAppContext("src/test/webapp", "/" + CONTEXT_NAME);
        webapp.setDefaultsDescriptor("/WEB-INF/jetty-pluto-web-default.xml");
        ServletHolder portletServlet = new ServletHolder(new PortletServlet());
        portletServlet.setInitParameter("portlet-name", ExamplePortlet.NAME);
        portletServlet.setInitParameter("scratchdir", tmpDir.getAbsolutePath());
        portletServlet.setInitOrder(1);
        webapp.addServlet(portletServlet, "/PlutoInvoker/" + ExamplePortlet.NAME);
        server.addHandler(webapp);
        server.start();
        getTestContext().setBaseUrl(TEST_URL);
    }
    
    @Override
	public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test of doView method, of class MyPortlet.
     */
    public void doView() throws Exception {
        renderPortlet();
        assertTextPresent("Welcome, this is the example portlet in view mode");
    }
    
    public void doEdit() throws Exception {
        renderPortlet(PortletMode.EDIT);
        assertTextPresent("This is the example portlet in edit mode");
    }
    
    public void doAction() throws Exception {
        renderPortlet();
        assertTextPresent("Welcome, this is the example portlet in view mode");
        
        assertFormPresent("answerForm");
        // This form should have two text fields. We'll populate these with data
        setWorkingForm("answerForm");
        setTextField("answer", "999");
        submit();
        assertTextPresent("Your answer was");
    }
}

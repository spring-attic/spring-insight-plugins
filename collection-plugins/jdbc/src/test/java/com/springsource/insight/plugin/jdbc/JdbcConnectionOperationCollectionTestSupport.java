/**
 * Copyright 2009-2011 the original author or authors.
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
package com.springsource.insight.plugin.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.BeforeClass;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

/**
 * 
 */
public abstract class JdbcConnectionOperationCollectionTestSupport
        extends OperationCollectionAspectTestSupport {
    protected static String       connectUrl;
    protected static Driver       connectDriver;
    protected static final Properties   connectProps=new Properties();
    protected static final ConnectionsTracker  tracker=ConnectionsTracker.getInstance();

    protected JdbcConnectionOperationCollectionTestSupport() {
        super();
    }

    @BeforeClass
    public static void initDrivers ()
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Thread      curThread=Thread.currentThread();
        ClassLoader loader=curThread.getContextClassLoader();
        Properties  props=new Properties();
        InputStream in=loader.getResourceAsStream("jdbc.properties");
        assertNotNull("Cannot find driver propreties file", in);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        String  driverClassName=props.getProperty("jdbc.driverClassName");
        if ((connectDriver=findDriverInstance(driverClassName)) == null) {
            Class<?>    driverClass=loader.loadClass(driverClassName);
            connectDriver = (Driver) driverClass.newInstance();
            DriverManager.registerDriver(connectDriver);
            System.out.println("Registered " + driverClassName);
        }

        connectUrl = props.getProperty("jdbc.url");
        assertTrue("Missing jdbc connect url", (connectUrl != null) && (connectUrl.length() > 0));

        String  username = props.getProperty("jdbc.username");
        if (username != null) {
            connectProps.put("user", username);
        }

        String  password=props.getProperty("jdbc.password");
        if (password != null)
            connectProps.put("password", password);
    }

    protected Operation assertConnectDetails (String url, String action) {
        return assertConnectDetails(getLastEntered(), url, action);
    }

    protected static Operation assertConnectDetails (Operation op, String url, String action) {
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type", JdbcDriverExternalResourceAnalyzer.TYPE, op.getType());
        assertEquals("Mismatched connect URL", url, op.get(OperationFields.CONNECTION_URL, String.class));
        assertEquals("Mismatched action", action, op.get(OperationFields.METHOD_NAME, String.class));
        return op;
    }

    protected String assertTrackedConnection (Connection conn, String url) {
        String result=tracker.checkTrackingState(conn);
        assertEquals("Mismatched tracked value", url, result);
        return url;
    }

    protected Connection assertConnectionNotTracked (Connection conn) {
        String result=tracker.checkTrackingState(conn);
        assertNull("Connection marked as tracking " + result, result);
        return conn;
    }

    private static Driver findDriverInstance (String driverClassName) {
        assertTrue("Missing driver class name", (driverClassName != null) && (driverClassName.length() > 0));
        for (Enumeration<Driver>    drivers=DriverManager.getDrivers();
               (drivers != null) && drivers.hasMoreElements(); ) {
           Driver  driver=drivers.nextElement();
           String  driverType=driver.getClass().getName();
           System.out.println("Found driver " + driverType);
           if (driverClassName.equals(driverType))
               return driver;
        }
        
        return null;
    }
}

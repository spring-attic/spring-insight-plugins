/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.springframework.security.ldap.server.ApacheDSContainer;
import org.springframework.util.FileSystemUtils;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.ClassUtil;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.time.TimeRange;

/**
 * A spring equivalent to <code>OperationCollectionAspectTestSupport</code>
 */
public abstract class LdapOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    private static final Log    LOG=LogFactory.getLog(LdapOperationCollectionAspectTestSupport.class);
    // Use non-default port to avoid conflicts if built on servers that have a running sever
    protected static final int TEST_PORT=53899;
    // NOTE: the DN(s) should match the one in the LDIF file
    protected static final String   ROOT_DN="dc=springframework,dc=org",
                                    LDAP_URL="ldap://localhost:" + TEST_PORT + "/" + ROOT_DN;
    // NOTE: these are the defaults - haven't figured out how to override them...
    protected static final String   LDAP_USERNAME="uid=admin,ou=system",
                                    LDAP_PASSWORD="secret";
    // NOTE: taken from a Spring security sample code
    protected static final String   LDIF_LOCATION="META-INF/testUsers.ldif",
                                    LDIF_RESUOURCE="classpath:" + LDIF_LOCATION;
    protected static Collection<Map<String,Set<String>>>   LDIF_ENTRIES;
    private static ApacheDSContainer   dsContainer;

    protected final Log logger=LogFactory.getLog(getClass());
    protected static final LdapExternalResourceAnalyzer analyzer=new LdapExternalResourceAnalyzer();

    protected LdapOperationCollectionAspectTestSupport() {
        super();
    }

    @BeforeClass
    public static final void startLdapServer() throws Exception {
        File    apacheWorkDir=resolveApacheWorkDir(LdapOperationCollectionAspectTestSupport.class);
        // see ApacheDSContainer#afterPropertiesSet() as to why this is necessary
        if (FileSystemUtils.deleteRecursively(apacheWorkDir)) {
            System.out.println("Deleted " + apacheWorkDir.getAbsolutePath());
        }

        dsContainer = new ApacheDSContainer(ROOT_DN, LDIF_RESUOURCE);
        dsContainer.setPort(TEST_PORT);
        dsContainer.setWorkingDirectory(apacheWorkDir);
        
        DefaultDirectoryService dsService=dsContainer.getService();
        dsService.setShutdownHookEnabled(true);

        LOG.info("Starting LDAP server on port " + TEST_PORT);
        dsContainer.afterPropertiesSet();
        LOG.info("LDAP server started");

        LDIF_ENTRIES = readLdifEntries(LDIF_LOCATION);
        LOG.info("Read " + LDIF_ENTRIES.size() + " LDIF entries from " + LDIF_LOCATION);
    }

    @AfterClass
    public static final void stopLdapServer () throws Exception {
        if (dsContainer == null) {
            LOG.warn("No current LDAP server instance running...");
            return;
        }

        LOG.info("Stopping LDAP server ...");
        dsContainer.destroy();
        LOG.info("LDAP server stopped...");
    }

    protected Operation assertContextOperation (String testName, String lookupName, Map<?,?> environment) {
        Operation   op=getLastEntered();
        Assert.assertNotNull(testName + ": No operation generated", op);
        Assert.assertEquals(testName + ": Mismatched operation type", LdapDefinitions.LDAP_OP, op.getType());
        Assert.assertEquals(testName + ": Mismatched lookup name",
                            lookupName, op.get(LdapDefinitions.LOOKUP_NAME_ATTR, String.class));

        LdapOperationCollectionAspectSupport    aspectInstance=
                (LdapOperationCollectionAspectSupport) getAspect();
        Assert.assertEquals(testName + ": Mismatched action",
                            aspectInstance.action, op.get(OperationFields.METHOD_NAME, String.class));

        Class<?>    contextClass=aspectInstance.contextClass;
        Assert.assertEquals(testName + ": Mismatched short class name",
                            contextClass.getSimpleName(), op.get(OperationFields.SHORT_CLASS_NAME, String.class));
        Assert.assertEquals(testName + ": Mismatched full class name",
                contextClass.getName(), op.get(OperationFields.CLASS_NAME, String.class));
        return op;
    }

    protected static final List<ExternalResourceDescriptor> assertExternalResourceAnalysis (
                String testName, Operation op, String ldapUrl)
             throws URISyntaxException {
        Frame   frame=new SimpleFrame(FrameId.valueOf("0"), null, op, TimeRange.FULL_RANGE, Collections.<Frame>emptyList());
        Trace   trace=new Trace(ServerName.valueOf("fake-server"),
                                ApplicationName.valueOf("fake-app"),
                                new Date(System.currentTimeMillis()),
                                TraceId.valueOf("0"),
                                frame);
        List<ExternalResourceDescriptor>    result=analyzer.locateExternalResourceName(trace);
        Assert.assertNotNull(testName + ": No external resources recovered", result);
        Assert.assertEquals(testName + ": Mismatched number of results", 1, result.size());

        ExternalResourceDescriptor  desc=result.get(0);
        Assert.assertSame(testName + ": Mismatched result frame", frame, desc.getFrame());
        Assert.assertEquals(testName + ": Mismathed name",
                            MD5NameGenerator.getName(ldapUrl), desc.getName());
        Assert.assertEquals(testName + ": Mismatched vendor", ldapUrl, desc.getVendor());
        Assert.assertEquals(testName + ": Mismatched label", ldapUrl, desc.getLabel());
        Assert.assertEquals(testName + ": Mismatched type",
                            ExternalResourceType.LDAP.name(), desc.getType());
        
        URI uri=new URI(ldapUrl);
        Assert.assertEquals(testName + ": Mismatched host", uri.getHost(), desc.getHost());
        Assert.assertEquals(testName + ": Mismatched port",
                            LdapExternalResourceAnalyzer.resolvePort(uri), desc.getPort());
        return result;
    }

    private static File resolveApacheWorkDir (Class<?> anchorClass) {
        // see ApacheDSContainer#afterPropertiesSet
        String apacheWorkDir = System.getProperty("apacheDSWorkDir");
        if (apacheWorkDir != null) {
            LOG.info("resolveApacheWorkDir(" + anchorClass.getSimpleName() + ") using pre-defined " + apacheWorkDir);
            return new File(apacheWorkDir);
        }

		File	anchorFile=ClassUtil.getClassContainerLocationFile(anchorClass);
        for (File classPath=anchorFile; classPath != null; classPath = classPath.getParentFile()) {
            if ("target".equals(classPath.getName()) && classPath.isDirectory()) {
                classPath = new File(classPath, "apacheDSWorkDir");
                LOG.info("resolveApacheWorkDir(" + anchorClass.getSimpleName() + ") location: " + classPath);
                return classPath;
            }
        }

        throw new IllegalStateException("No target folder for " + anchorClass.getSimpleName() + " at " + anchorFile);
    }

    private static Collection<Map<String,Set<String>>> readLdifEntries (String location)
            throws IOException {
        ClassLoader cl=ClassUtil.getDefaultClassLoader();
        InputStream in=cl.getResourceAsStream(location);
        Assert.assertNotNull("No LDIF input at " + location, in);

        BufferedReader  rdr=new BufferedReader(new InputStreamReader(in));
        try {
            return readLdifEntries(rdr);
        } finally {
            rdr.close();
        }
    }

    private static Collection<Map<String,Set<String>>> readLdifEntries (BufferedReader rdr)
                throws IOException {
        Collection<Map<String,Set<String>>>  result=new LinkedList<Map<String,Set<String>>>();
        Map<String,Set<String>>  curEntry=null;
        for (String line=rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            if (LOG.isTraceEnabled()) {
                LOG.trace("readLdifEntries - " + line);
            }

            // empty line signals end of entry
            if (StringUtil.isEmpty(line)) {
                if (curEntry != null) {
                    result.add(curEntry);
                }
                
                curEntry = null;
                continue;
            }
            
            if (curEntry == null) {
                curEntry = new TreeMap<String,Set<String>>(String.CASE_INSENSITIVE_ORDER);
            }
            
            int     namePos=line.indexOf(':');
            String  name=line.substring(0, namePos),
                    value=line.substring(namePos + 2).trim();
            if (StringUtil.isEmpty(name) || StringUtil.isEmpty(value)) {
                throw new IllegalArgumentException("Bad line: " + line);
            }

            if ("dn".equalsIgnoreCase(name)) {
                Assert.assertTrue("DN(" + value + ") not subset of root (" + ROOT_DN + ")",
                                  value.toLowerCase().endsWith(ROOT_DN.toLowerCase()));
            }

            Set<String> values=curEntry.get(name);
            if (values == null) {
                values = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                curEntry.put(name, values);
            }
            
            if (!values.add(value)) {
                throw new IllegalStateException("Duplicate values for name=" + name + " at line " + line);
            }
        }
        
        return result;
    }

    protected static final Hashtable<String,Object> createEnvironment () {
        Hashtable<String,Object>    env=new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, LDAP_URL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
        return env;
    }
}

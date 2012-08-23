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
package com.springsource.insight.plugin.ldap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;

public class DirContextSearchCollectionAspectTest
        extends LdapOperationCollectionAspectTestSupport {

    public DirContextSearchCollectionAspectTest() {
        super();
    }

    @Test
    public void testResolveScopesCoverage() {
        final Map<String,Integer>   knownScopes=new TreeMap<String, Integer>();
        ReflectionUtils.doWithFields(SearchControls.class, new FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                int mod=field.getModifiers();
                if (Modifier.isPublic(mod)
                 && Modifier.isStatic(mod)
                 && Modifier.isFinal(mod)) {
                    String  name=field.getName();
                    if (name.endsWith("_SCOPE")) {
                        assertNull("Multiple mappings for field=" + name,
                                          knownScopes.put(name, Integer.valueOf(field.getInt(null))));
                    }
                }
            }
        });
        
        assertEquals("Mismatched supported num of scopes",
                            knownScopes.size(),
                            DirContextSearchCollectionAspect.scopes.size());
        for (Map.Entry<String,Integer> se : knownScopes.entrySet()) {
            String          name=se.getKey();
            Integer         value=se.getValue();
            SearchControls  sc=createSearchControls(value.intValue(), 0L, false, false, 1);
            String  expScope=DirContextSearchCollectionAspect.scopes.get(value),
                    actScope=DirContextSearchCollectionAspect.resolveScope(sc);
            assertEquals("Mismatched scope names for " + name, expScope, actScope);
        }
    }

    @Test
    public void testLiveLdapSearches () throws Exception {
        runLiveSearchControlsActions(new DirContextCreator() { 
                public InitialDirContext createDirContext () throws NamingException {
                    return new InitialDirContext(createEnvironment());
                }
        });
    }

    @Test
    public void testMissingProviderUrlLdapSearches () throws NamingException {
        DirContext  context=new TestLdapContext(new Hashtable<Object,Object>());
        try {
            runNonLdapSearchControlsActions(context);
        } finally {
            context.close();
        }
    }

    @Test
    public void testNonLdapProviderUrlSearches () throws NamingException {
        DirContext  context=new TestLdapContext(new Hashtable<Object,Object>()
            { 
                private static final long serialVersionUID = 1L;
    
                {
                    put(Context.PROVIDER_URL, "jndi:test/url");
                }
            });

        try {
            runNonLdapSearchControlsActions(context);
        } finally {
            context.close();
        }
    }

    @Override
    public DirContextSearchCollectionAspect getAspect() {
        return DirContextSearchCollectionAspect.aspectOf();
    }

    private void runNonLdapSearchControlsActions (DirContext context) throws NamingException {
        OperationCollectionAspectSupport    aspectInstance=getAspect();

        for (final SearchControlsActions action : SearchControlsActions.values()) {
            aspectInstance.setCollector(new OperationCollector() {
                    public void enter(Operation operation) {
                        fail(action + ": Unexpected enter call");
                    }
    
                    public void exitNormal() {
                        fail(action + ": Unexpected exitNormal call");
                    }
    
                    public void exitNormal(Object returnValue) {
                        fail(action + ": Unexpected exitNormal call with value");
                    }
    
                    public void exitAbnormal(Throwable throwable) {
                        fail(action + ": Unexpected exitAbnormal call");
                    }
    
                    public void exitAndDiscard() {
                        fail(action + ": Unexpected exitAndDiscard call");
                    }
    
                    public void exitAndDiscard(Object returnValue) {
                        fail(action + ": Unexpected exitAndDiscard call with value");
                    }
                });
            NamingEnumeration<SearchResult> result=
                    action.search(context, "type=test", "blah blah", action);
            try {
                assertNotNull(action + ": no result", result);
                assertFalse(action + ": unexpected result", result.hasMore());
            } finally {
                result.close();
            }
        }
    }

    private void runLiveSearchControlsActions (DirContextCreator creator)
            throws NamingException, URISyntaxException {
        final String DN_PROPNAME="objectclass", DN_PROPVAL="person", BASE_DN="ou=people";
        final String ARGS_FILTER="(&(" + DN_PROPNAME + "=" + DN_PROPVAL + ")(uid={0})(sn={1}))";
        final Format userSearchFilter=new MessageFormat(ARGS_FILTER);

        for (Map<String,Set<String>> ldifEntry : LDIF_ENTRIES) {
            Set<String>  classValues=ldifEntry.get(DN_PROPNAME);
            if (!classValues.contains(DN_PROPVAL)) {
                continue;
            }
            Set<String> values=ldifEntry.get("cn");
            assertNotNull("No CN for " + ldifEntry, values);
            assertEquals("Multiple CB(s) for " + ldifEntry, 1, values.size());

            /*
             * The LDIF is set up in such a way that for person(s), the
             * 'uid' value is same as the 1st name in lowercase, and the
             * 'sn' value is same as the 2nd name
             */
            String      cnValue=values.iterator().next().trim();
            int         spacePos=cnValue.indexOf(' ');
            String      uidValue=cnValue.substring(0, spacePos).toLowerCase();
            String      snValue=cnValue.substring(spacePos + 1);
            Object[]    filterArgs={ uidValue, snValue };
            String      noArgsFilter=userSearchFilter.format(filterArgs);
            for (SearchControlsActions action : SearchControlsActions.values()) {
                final String    TEST_NAME=cnValue + "[" + action + "]";
                final String    TEST_FILTER=action.isRequiredFilterArgs()
                                    ? ARGS_FILTER
                                    : noArgsFilter
                                    ;
                final Object[]  SEARCH_ARGS=action.isRequiredFilterArgs()
                                    ? filterArgs
                                    : null
                                    ;
                logger.info("Running test: " + TEST_NAME);
                DirContext      context=creator.createDirContext();
                Hashtable<?,?>  environment;
                try {
                    // save a copy just in case it changes on context close
                    environment =new Hashtable<Object,Object>(context.getEnvironment());

                    NamingEnumeration<SearchResult> result=
                            action.search(context, BASE_DN, TEST_FILTER, SEARCH_ARGS);
                    assertNotNull(TEST_NAME + ": No result", result);
                    try {
                        if (!checkMatchingSearchResult(result, "cn", cnValue)) {
                            fail(TEST_NAME + ": No match found");
                        }
                    } finally {
                        result.close();
                    }
                } catch(NamingException e) {
                    logger.warn("search(" + TEST_NAME + ")"
                              + " " + e.getClass().getSimpleName()
                              + ": " + e.getMessage(), e);
                    throw e;
                } finally {
                    context.close();
                }

                Operation   op=assertContextOperation(TEST_NAME, BASE_DN, environment);
                assertEquals(TEST_NAME + ": Mismatched filter",
                                    TEST_FILTER, op.get(LdapDefinitions.LOOKUP_FILTER_ATTR, String.class));
                assertExternalResourceAnalysis(TEST_NAME, op, (String) environment.get(Context.PROVIDER_URL));
                Mockito.reset(spiedOperationCollector); // prepare for next iteration
            }
        }
    }

    static boolean checkMatchingSearchResult (NamingEnumeration<SearchResult> result,
                                              String                          attrName,
                                              Object                          expValue)
                                  throws NamingException {
        while ((result != null) && result.hasMore()) {
            SearchResult  sr=result.nextElement();
            Attributes    attrs=sr.getAttributes();

            NamingEnumeration<? extends Attribute>  attrVals=attrs.getAll();
            try {
                while ((attrVals != null) && attrVals.hasMore() ) {
                    Attribute a=attrVals.next();
                    String    attrID=a.getID();
                    if (!attrName.equalsIgnoreCase(attrID)) {
                        continue;
                    }

                    Object    attrVal=a.get();
                    if (expValue.equals(attrVal)) {
                        return true;
                    }
                }
            } finally {
                if (attrVals != null) {
                    attrVals.close();
                }
            }
        }

        return false;
    }

    static enum SearchControlsActions {
        StringAndArgs {
            @Override
            public NamingEnumeration<SearchResult> search(DirContext context,
                    String name, String filterExpr, Object... filterArgs)
                    throws NamingException {
                return context.search(name, filterExpr, filterArgs,
                        createSearchControls());
            }

            @Override
            public boolean isRequiredFilterArgs() {
                return true;
            }
        },
        NameAndArgs {
            @Override
            public NamingEnumeration<SearchResult> search(DirContext context,
                    String name, String filterExpr, Object... filterArgs)
                    throws NamingException {
                return context.search(new LdapName(name), filterExpr,
                        filterArgs, createSearchControls());
            }

            @Override
            public boolean isRequiredFilterArgs() {
                return true;
            }
        },
        NameOnly {
            @Override
            public NamingEnumeration<SearchResult> search(
                        DirContext context, String name, String filterExpr, Object... filterArgs)
                    throws NamingException {
                return context.search(new LdapName(name), filterExpr, createSearchControls());
            }

            @Override
            public boolean isRequiredFilterArgs () {
                return false;
            }
        },
        StringOnly {
            @Override
            public NamingEnumeration<SearchResult> search(
                        DirContext context, String name, String filterExpr, Object... filterArgs)
                    throws NamingException {
                return context.search(name, filterExpr, createSearchControls());
            }

            @Override
            public boolean isRequiredFilterArgs () {
                return false;
            }
        };
        
        public abstract NamingEnumeration<SearchResult> search(
                DirContext context, String name, String filterExpr, Object ... filterArgs)
                        throws NamingException;
        public abstract boolean isRequiredFilterArgs ();

        static SearchControls createSearchControls () {
            SearchControls  sc=new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setTimeLimit((int) TimeUnit.SECONDS.toMillis(30L));
            return sc;
        }
    }

    static SearchControls createSearchControls (
            int scope, long countLimit, boolean derefFlag, boolean retobjFlag, int timeLimit, String ... attrs) {
        SearchControls  sc=new SearchControls();
        sc.setCountLimit(countLimit);
        sc.setDerefLinkFlag(derefFlag);
        sc.setReturningObjFlag(retobjFlag);
        sc.setReturningAttributes(attrs);
        sc.setSearchScope(scope);
        return sc;
    }
}

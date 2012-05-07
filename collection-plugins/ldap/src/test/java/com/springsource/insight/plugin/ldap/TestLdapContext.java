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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class TestLdapContext implements LdapContext {
    private static final NamingEnumeration<SearchResult> EMPTY_SEARCH_RESULT=
            new EmptyNamingEnumeration<SearchResult>();
    private static final NamingEnumeration<NameClassPair>   EMPTY_NAMECLASS_PAIR=
            new EmptyNamingEnumeration<NameClassPair>();
    private static final NamingEnumeration<Binding> EMPTY_BINDINGS=
            new EmptyNamingEnumeration<Binding>();
    private static final NameParser DEFAULT_NAME_PARSER=new NameParser() {
            public Name parse(String name) throws NamingException {
                return createName(name);
            }
        };

    private final Hashtable<Object,Object> environment;
    private static final String[]   EMPTY_STRINGS={ };
    private final Log   logger=LogFactory.getLog(getClass());
    private Control[]   requestControls;
    private boolean closed;

    public TestLdapContext(@SuppressWarnings("hiding") Hashtable<?,?> environment) {
        this.environment = new Hashtable<Object,Object>(environment);
    }

    public Attributes getAttributes(String name) throws NamingException {
        return getAttributes(name, EMPTY_STRINGS);
    }

    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        return getAttributes(createName(name), attrIds);
    }

    public Attributes getAttributes(Name name) throws NamingException {
        return getAttributes(name, EMPTY_STRINGS);
    }

    public Attributes getAttributes(Name name, String[] attrIds)
            throws NamingException {
        ensureOpen();
        logger.info("getAttributes(" + name + ")[" + attrIds + "]");
        return new BasicAttributes();
    }

    public void modifyAttributes(String name, int mod_op, Attributes attrs)
            throws NamingException {
        modifyAttributes(createName(name), mod_op, attrs);
    }

    public void modifyAttributes(String name, ModificationItem[] mods)
            throws NamingException {
        modifyAttributes(createName(name), mods);
    }

    public void modifyAttributes(Name name, ModificationItem[] mods)
            throws NamingException {
        for (ModificationItem item : mods) {
            BasicAttributes attrs=new BasicAttributes();
            attrs.put(item.getAttribute());
            modifyAttributes(name, item.getModificationOp(), attrs);
        }
    }

    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
            throws NamingException {
        ensureOpen();
        logger.info("modifyAttributes(" + name + ")[" + mod_op + "]");
        if (logger.isTraceEnabled()) {
            logAttributes("modifyAttributes(" + name + ")[" + mod_op + "]", attrs);
        }
    }

    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name, obj, new BasicAttributes());
    }

    public void rebind(String name, Object obj) throws NamingException {
        rebind(name, obj, new BasicAttributes());
    }

    public void rebind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        unbind(name);
        bind(name, obj, attrs);
    }

    public void rebind(String name, Object obj, Attributes attrs)
            throws NamingException {
        unbind(name);
        bind(name, obj, attrs);
    }

    public void bind(String name, Object obj) throws NamingException {
        bind(name, obj, new BasicAttributes());
    }

    public void bind(Name name, Object obj) throws NamingException {
        bind(name, obj, new BasicAttributes());
    }

    public void bind(String name, Object obj, Attributes attrs)
            throws NamingException {
        bind(createName(name), obj, attrs);
    }

    public void bind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        ensureOpen();
        logger.info("bind(" + name + ")[" + obj + "]");
        if (logger.isTraceEnabled()) {
            logAttributes("bind(" + name + ")[" + obj + "]", attrs);
        }
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name, new BasicAttributes());
    }

    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(name, new BasicAttributes());
    }

    public DirContext createSubcontext(String name, Attributes attrs)
            throws NamingException {
        return createSubcontext(createName(name), attrs);
    }

    public DirContext createSubcontext(Name name, Attributes attrs)
            throws NamingException {
        ensureOpen();
        logger.info("createSubContext(" + name + ")");
        if (logger.isTraceEnabled()) {
            logAttributes("createSubContext(" + name + ")", attrs);
        }
        return this;
    }

    public DirContext getSchema(String name) throws NamingException {
        return getSchema(createName(name));
    }

    public DirContext getSchema(Name name) throws NamingException {
        ensureOpen();
        logger.info("getSchema(" + name + ")");
        return this;
    }

    public DirContext getSchemaClassDefinition(String name)
            throws NamingException {
        return getSchemaClassDefinition(createName(name));
    }

    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException {
        ensureOpen();
        logger.info("getSchemaClassDefinition(" + name + ")");
        return this;
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        return search(name, matchingAttributes, EMPTY_STRINGS);
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes)
            throws NamingException {
        return search(name, matchingAttributes, EMPTY_STRINGS);
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        return search(createName(name), matchingAttributes, attributesToReturn);
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        ensureOpen();
        logger.info("search(" + name + ") => " + Arrays.asList(attributesToReturn));
        return EMPTY_SEARCH_RESULT;
    }

    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
            throws NamingException {
        return search(name, filter, EMPTY_STRINGS, cons);
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        return search(name, filter, EMPTY_STRINGS, cons);
    }

    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons)
            throws NamingException {
        return search(createName(name), filterExpr, filterArgs, cons);
    }

    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons)
            throws NamingException {
        ensureOpen();
        logger.info("search(" + name + ")[" + filterExpr + "]@" + Arrays.asList(filterArgs));
        return EMPTY_SEARCH_RESULT;
    }

    public Object lookup(String name) throws NamingException {
        return lookup(createName(name));
    }

    public Object lookup(Name name) throws NamingException {
        ensureOpen();
        logger.info("lookup(" + name + ")");
        return null;
    }

    public void unbind(String name) throws NamingException {
        unbind(createName(name));
    }

    public void unbind(Name name) throws NamingException {
        ensureOpen();
        logger.info("unbind(" + name + ")");
    }

    public void rename(String oldName, String newName) throws NamingException {
        rename(createName(oldName), createName(newName));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        ensureOpen();
        logger.info("rename(" + oldName + ") => " + newName);
    }

    public NamingEnumeration<NameClassPair> list(String name)
            throws NamingException {
        return list(createName(name));
    }

    public NamingEnumeration<NameClassPair> list(Name name)
            throws NamingException {
        ensureOpen();
        logger.info("list(" + name + ")");
        return EMPTY_NAMECLASS_PAIR;
    }

    public NamingEnumeration<Binding> listBindings(String name)
            throws NamingException {
        return listBindings(createName(name));
    }

    public NamingEnumeration<Binding> listBindings(Name name)
            throws NamingException {
        ensureOpen();
        logger.info("listBindings(" + name + ")");
        return EMPTY_BINDINGS;
    }

    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(createName(name));
    }

    public void destroySubcontext(Name name) throws NamingException {
        ensureOpen();
        logger.info("destroySubcontext(" + name + ")");
    }

    public Object lookupLink(String name) throws NamingException {
        return lookupLink(createName(name));
    }

    public Object lookupLink(Name name) throws NamingException {
        ensureOpen();
        logger.info("lookupLink(" + name + ")");
        return null;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return createName(composeName(name.toString(), prefix.toString()));
    }

    public String composeName(String name, String prefix)
            throws NamingException {
        ensureOpen();
        logger.info("composeName(" + name + ")[" + prefix + "]");
        return prefix + "," + name;
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        ensureOpen();
        logger.info("getNameParser(" + name + ")");

        return DEFAULT_NAME_PARSER;
    }

    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        return environment.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return environment.remove(propName);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return new Hashtable<Object,Object>(environment);
    }

    public void close() throws NamingException {
        if (!closed) {
            logger.info("close()");
            closed = true;
        }
    }

    public String getNameInNamespace() throws NamingException {
        ensureOpen();
        return getClass().getSimpleName();
    }

    public ExtendedResponse extendedOperation(final ExtendedRequest request)
            throws NamingException {
        ensureOpen();
        logger.info("extendedOperation(" + request.getID() + ")");
        return new ExtendedResponse() {
            private static final long serialVersionUID = 1L;

            public String getID() {
                return request.getID();
            }

            public byte[] getEncodedValue() {
                return request.getEncodedValue();
            }
        };
    }

    public LdapContext newInstance(@SuppressWarnings("hiding") Control[] requestControls)
            throws NamingException {
        setRequestControls(requestControls);
        return this;
    }

    public void reconnect(Control[] connCtls) throws NamingException {
        setRequestControls(connCtls);
    }

    public Control[] getConnectControls() throws NamingException {
        return requestControls;
    }

    public void setRequestControls(@SuppressWarnings("hiding") Control[] requestControls)
            throws NamingException {
        ensureOpen();
        this.requestControls = requestControls;
    }

    public Control[] getRequestControls() throws NamingException {
        return requestControls;
    }

    public Control[] getResponseControls() throws NamingException {
        return requestControls;
    }

    private void ensureOpen () throws NamingException {
        if (closed) {
            throw new NamingException("Context marked as closed");
        }
    }
    
    protected static Name createName (String name) throws InvalidNameException {
        return new LdapName(name);
    }
    
    private void logAttributes (String location, Attributes attrs) throws NamingException {
        NamingEnumeration<? extends Attribute> values=attrs.getAll();
        try {
            while ((values != null) && values.hasMore()) {
                Attribute       aValue=values.next();
                String          id=aValue.getID();
                Collection<?>   valsList=Collections.list(aValue.getAll());
                logger.trace(location + "[" + id + "]: " + valsList);
            }
        } finally {
            values.close();
        }
    }
    
    private static class EmptyNamingEnumeration<V> implements NamingEnumeration<V> {
        EmptyNamingEnumeration () {
            super();
        }

        public boolean hasMoreElements() {
            return false;
        }

        public V nextElement() {
            throw new UnsupportedOperationException("nextElement N/A");
        }

        public V next() throws NamingException {
            throw new NamingException("nextE N/A");
        }

        public boolean hasMore() throws NamingException {
            return false;
        }

        public void close() throws NamingException {
            // ignored
        }
    }
}

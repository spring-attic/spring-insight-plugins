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

package com.springsource.insight.plugin.jndi;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class JndiTestContext implements Context {
	private final Map<String, Object>	bindings=new TreeMap<String, Object>();
	private final Hashtable<String,Object> env=new Hashtable<String, Object>();
	private final boolean ignoreBindings;

	public JndiTestContext() {
		this(false);
	}

	JndiTestContext(boolean bindingsIgnored) {
		ignoreBindings = bindingsIgnored;
	}

	Map<String, Object> getBindings () {
		return bindings;
	}

	void setBindings (Map<String,?> values) {
		if (!bindings.isEmpty()) {
			bindings.clear();
		}

		bindings.putAll(values);
	}

	void clear () {
		if (!bindings.isEmpty()) {
			bindings.clear();
		}

		if (!env.isEmpty()) {
			env.clear();
		}
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(StringUtil.safeToString(name));
	}

	public Object lookup(String name) throws NamingException {
		return bindings.get(name);
	}

	public void rebind(Name name, Object obj) throws NamingException {
		rebind(StringUtil.safeToString(name), obj);
	}

	public void rebind(String name, Object obj) throws NamingException {
		if ((!bindings.containsKey(name)) && (!ignoreBindings)) {
			throw new NameNotFoundException("rebind(" + name + ")[" + obj + "]");
		}

		bindings.put(name, obj);
	}

	public void bind(Name name, Object obj) throws NamingException {
		bind(StringUtil.safeToString(name), obj);
	}

	public void bind(String name, Object obj) throws NamingException {
		if (bindings.containsKey(name) && (!ignoreBindings)) {
			throw new NameAlreadyBoundException("bind(" + name + ")[" + obj + "]");
		}
		bindings.put(name, obj);
	}

	public void unbind(Name name) throws NamingException {
		unbind(StringUtil.safeToString(name));
	}

	public void unbind(String name) throws NamingException {
		Object	prev=bindings.remove(name);
		if ((prev == null) && (!ignoreBindings)) {
			throw new NameNotFoundException("unbind(" + name + ")");
		}
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		rename(StringUtil.safeToString(oldName), StringUtil.safeToString(newName));
	}

	public void rename(String oldName, String newName) throws NamingException {
		throw new ConfigurationException("rename(" + oldName + " => " + newName + ") N/A");
	}

	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		return list(StringUtil.safeToString(name));
	}

	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		throw new ConfigurationException("list(" + name + ") N/A");
	}

	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		return listBindings(StringUtil.safeToString(name));
	}

	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		throw new ConfigurationException("listBindings(" + name + ") N/A");
	}

	public void destroySubcontext(Name name) throws NamingException {
		destroySubcontext(StringUtil.safeToString(name));
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new ConfigurationException("destroySubcontext(" + name + ") N/A");
	}

	public Context createSubcontext(Name name) throws NamingException {
		return createSubcontext(StringUtil.safeToString(name));
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new ConfigurationException("createSubcontext(" + name + ") N/A");
	}

	public Object lookupLink(Name name) throws NamingException {
		return lookupLink(StringUtil.safeToString(name));
	}

	public Object lookupLink(String name) throws NamingException {
		return lookup(name);
	}

	public NameParser getNameParser(Name name) throws NamingException {
		return getNameParser(StringUtil.safeToString(name));
	}

	public NameParser getNameParser(String name) throws NamingException {
		throw new ConfigurationException("getNameParser(" + name + ") N/A");
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		throw new ConfigurationException("composeName(" + name + ")[" + prefix + "] N/A");
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		throw new ConfigurationException("composeName(" + name + ")[" + prefix + "] N/A");
	}

	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		return env.put(propName, propVal);
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		return env.remove(propName);
	}

	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return env;
	}

	void setEnvironment (Map<String,?> values) {
		if (!env.isEmpty()) {
			env.clear();
		}

		env.putAll(values);
	}

	public void close() throws NamingException {
		// ignored
	}

	public String getNameInNamespace() throws NamingException {
		return null;
	}

}

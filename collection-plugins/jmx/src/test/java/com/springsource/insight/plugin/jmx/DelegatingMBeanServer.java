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

package com.springsource.insight.plugin.jmx;

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public class DelegatingMBeanServer implements MBeanServer {
    private final MBeanServer server;

    /**
     *
     */
    public DelegatingMBeanServer(MBeanServer s) {
        if ((server = s) == null) {
            throw new IllegalStateException("No delegate");
        }
    }

    public ObjectInstance createMBean(String className, ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException {
        return server.createMBean(className, name);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
            throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException {
        return server.createMBean(className, name, loaderName);
    }

    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
            throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException {
        return server.createMBean(className, name, params, signature);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException {
        return server.createMBean(className, name, loaderName, params, signature);
    }

    public ObjectInstance registerMBean(Object object, ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        return server.registerMBean(object, name);
    }

    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException {
        server.unregisterMBean(name);
    }

    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException {
        return server.getObjectInstance(name);
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        return server.queryMBeans(name, query);
    }

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        return server.queryNames(name, query);
    }

    public boolean isRegistered(ObjectName name) {
        return server.isRegistered(name);
    }

    public Integer getMBeanCount() {
        return server.getMBeanCount();
    }

    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException {
        return server.getAttribute(name, attribute);
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException {
        AttributeList result = new AttributeList(ArrayUtil.length(attributes));
        for (String a : attributes) {
            try {
                Object value = getAttribute(name, a);
                result.add(new Attribute(a, value));
            } catch (AttributeNotFoundException e) {
                throw new ReflectionException(e, "No attribute: " + name.getCanonicalName() + "[" + a + "]");
            } catch (MBeanException e) {
                throw new ReflectionException(e, "Bad MBean: " + name.getCanonicalName() + "[" + a + "]: " + e.getMessage());
            }
        }
        return result;
    }

    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        server.setAttribute(name, attribute);

    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException {
        for (Object a : attributes) {
            Attribute attr = (Attribute) a;
            try {
                setAttribute(name, attr);
            } catch (AttributeNotFoundException e) {
                throw new ReflectionException(e, "No attribute: " + name.getCanonicalName() + "[" + attr.getName() + "]");
            } catch (InvalidAttributeValueException e) {
                throw new ReflectionException(e, "Bad value for " + name.getCanonicalName() + "[" + attr.getName() + "]: " + attr.getValue());
            } catch (MBeanException e) {
                throw new ReflectionException(e, "Bad MBean: " + name.getCanonicalName() + "[" + attr.getName() + "]=" + attr.getValue() + ": " + e.getMessage());
            }
        }
        return attributes;
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException {
        return server.invoke(name, operationName, params, signature);
    }

    public String getDefaultDomain() {
        return server.getDefaultDomain();
    }

    public String[] getDomains() {
        return server.getDomains();
    }

    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException {
        server.addNotificationListener(name, listener, filter, handback);
    }

    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException {
        server.addNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return server.getMBeanInfo(name);
    }

    public boolean isInstanceOf(ObjectName name, String className)
            throws InstanceNotFoundException {
        return server.isInstanceOf(name, className);
    }

    public Object instantiate(String className) throws ReflectionException, MBeanException {
        return server.instantiate(className);
    }

    public Object instantiate(String className, ObjectName loaderName)
            throws ReflectionException, MBeanException, InstanceNotFoundException {
        return server.instantiate(className, loaderName);
    }

    public Object instantiate(String className, Object[] params, String[] signature)
            throws ReflectionException, MBeanException {
        return server.instantiate(className, params, signature);
    }

    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, MBeanException, InstanceNotFoundException {
        return server.instantiate(className, loaderName, params, signature);
    }

    @Deprecated
    public ObjectInputStream deserialize(ObjectName name, byte[] data)
            throws InstanceNotFoundException, OperationsException {
        return server.deserialize(name, data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className, byte[] data)
            throws OperationsException, ReflectionException {
        return server.deserialize(className, data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data)
            throws InstanceNotFoundException, OperationsException, ReflectionException {
        return server.deserialize(className, loaderName, data);
    }

    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
            throws InstanceNotFoundException {
        return server.getClassLoaderFor(mbeanName);
    }

    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException {
        return server.getClassLoader(loaderName);
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        return server.getClassLoaderRepository();
    }
}

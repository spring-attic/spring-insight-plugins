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
package com.springsource.insight.plugin.ejb3;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.springsource.insight.collection.method.TrailingMethodOperationCollectionAspect;
import com.springsource.insight.collection.strategies.BasicCollectionAspectProperties;
import com.springsource.insight.collection.strategies.CollectionAspectProperties;
import com.springsource.insight.intercept.operation.Operation;

/**
 */
public aspect Ejb3LocalServiceOperationCollectionAspect
        extends TrailingMethodOperationCollectionAspect
        implements CollectionAspectProperties {
    public Ejb3LocalServiceOperationCollectionAspect () {
        super();
    }

    public pointcut statefulBeanExecution ()
        : execution(* (@Stateful *).*(..))
        ;

    public pointcut statelessBeanExecution ()
        : execution(* (@Stateless *).*(..))
        ;

    /*
     * NOTE: a more generic (and code-free) definitions would be 'execution (* @Local *..*+.*(..))'.
     *      However, this exposes all sort of internal proxies and is not as
     *      exact as it should be since it also intercepts calls that are not
     *      part of a @Local annotated interface.
     * @see com.springsource.insight.collection.TrailingAbstractOperationCollectionAspect#collectionPoint()
     */
    public pointcut collectionPoint ()
        : (statefulBeanExecution() || statelessBeanExecution())
       && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        if (checkAnnotatedEjb3LocalCollection(jp)) {
            return super.createOperation(jp).type(Ejb3LocalServiceDefinitions.TYPE);
        }

        return null;    // debug breakpoint
    }
    /**
     * Checks if the invocation pointcut represents a method of an
     * interface marked as {@link Local} as implemented by the target. This is
     * done by checking the method <U>signature</U> against all known such
     * method signatures for the class. 
     * @param jp The intercepted {@link JoinPoint}
     * @return TRUE if the {@link JoinPoint} represents a method of an
     * interface marked as {@link Local} as implemented by the target.
     * @see #getLocalInterfaceMethods(Class)
     * @see #buildMethodKey(Method)
     */
    protected static final boolean checkAnnotatedEjb3LocalCollection (final JoinPoint jp) {
        final Object    target=(jp == null) ? null : jp.getTarget();
        final Class<?>  clazz=(target == null) ? null : target.getClass();
        final Signature sig=(jp == null) ? null : jp.getSignature();
        if ((clazz != null) && (sig instanceof MethodSignature)) {
            final Method        method=((MethodSignature) sig).getMethod();
            final String        methodKey=(method == null) ? null : buildMethodKey(method);
            final Set<String>   intfcMethods=
                ((methodKey == null) || (methodKey.length() <= 0)) ? null : getLocalInterfaceMethods(clazz);
            /*
             * NOTE: we rely on the fact that in Java, 2 methods are considered
             *      the same if they have the same name and same numer and type
             *      of parameters. This means that if we find a method with the
             *      same key in a class that implements a Local interface then
             *      it MUST be the one from the interface.
             * 
             *  P.S. we cannot use Method(s) since the Method#equals() takes
             *  into account the declaring class as well - which in the case
             *  of an EJB implementing a Local interface is either the name
             *  of the implementing class or the proxy that wraps the EJB.
             */
            if ((intfcMethods != null) && (intfcMethods.size() > 0) && intfcMethods.contains(methodKey)) {
                return true;    // debug breakpoint
            }
        }

        return false;
    }

    private static final Map<Class<?>,Set<String>>  _localInterfaceMethodsMap=
            Collections.synchronizedMap(new HashMap<Class<?>,Set<String>>());
    /**
     * @param clazz The executing target {@link Class}
     * @return A {@link Set} of all the method &quot;keys&quot; representing
     * methods of an interface marked as {@link Local} that is implemented
     * (directly or via inheritance) by the {@link Class}. <B>Note:</B> checks
     * the {@link #_localInterfaceMethodsMap} cache to see if already have the
     * required information
     * @see #buildMethodKey(Method)
     * @see #updateLocalInterfaceMethods(Class, Set)
     */
    protected static final Set<String> getLocalInterfaceMethods (final Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptySet();
        }

        // no need to lock the entire Map since parallel executions will yield the same result
        Set<String> intfcMethods=_localInterfaceMethodsMap.get(clazz);
        if (intfcMethods != null) {
            return intfcMethods;
        }

        if ((intfcMethods=updateLocalInterfaceMethods(clazz, null)) == null) {
            intfcMethods = Collections.emptySet();
        }

        final Set<String>   prevMethods=_localInterfaceMethodsMap.put(clazz, intfcMethods);
        if (prevMethods != null) {
            return prevMethods; // debug breakpoint
        }

        return intfcMethods;
    }
    /**
     * @param clazz The executing target {@link Class}
     * @param orgMethods A current {@link Set} of all the method &quot;keys&quot;
     * representing methods of an interface marked as {@link Local} that is
     * implemented (directly or via inheritance) by the {@link Class}.
     * @return The updated {@link Set} - if original was <code>null</code> and
     * need to add names then a new {@link Set} instance is created and returned.
     * @see #updateLocalInterfaceMethods(Set, Method...)
     */
    protected static final Set<String> updateLocalInterfaceMethods (final Class<?> clazz, final Set<String> orgMethods) {
        if (clazz == null) {
            return orgMethods;
        }

        Set<String> retMethods=orgMethods;
        if (clazz.isInterface() && (clazz.getAnnotation(Local.class) != null)) {
            retMethods = updateLocalInterfaceMethods(retMethods, clazz.getDeclaredMethods());
        }

        final Class<?>[]    interfaces=clazz.getInterfaces();
        if ((interfaces != null) && (interfaces.length > 0)) {
            for (final Class<?> intfc : interfaces) {
                retMethods = updateLocalInterfaceMethods(intfc, retMethods);
            }
        }

        final Class<?>  parent=clazz.getSuperclass();
        if (parent != null) {
            retMethods = updateLocalInterfaceMethods(parent, retMethods);
        }

        return retMethods;
    }
    /**
     * @param orgMethods A current {@link Set} of all the method &quot;keys&quot;
     * representing methods of an interface marked as {@link Local} that is
     * implemented (directly or via inheritance) by the {@link Class}.
     * @param methods The {@link Method}-s to be added to the set
     * @return The updated {@link Set} - if original was <code>null</code> and
     * need to add names then a new {@link Set} instance is created and returned.
     */
    protected static final Set<String> updateLocalInterfaceMethods (final Set<String> orgMethods, final Method ...methods) {
        if ((methods == null) || (methods.length <= 0)) {
            return orgMethods;
        }
        
        final Set<String>   retMethods=(orgMethods == null) ? new TreeSet<String>() : orgMethods;
        for (final Method m : methods) {
            final String    mKey=buildMethodKey(m);
            if ((mKey == null) || (mKey.length() <= 0) || (!retMethods.add(mKey))) {
                continue;   // debug breakpoint
            }
        }

        return retMethods;
    }

    // this code is based on JoinPointBreakdown#getMethodString - TODO move it to 'util' JAR
    protected static final String buildMethodKey (final Method m) {
        if (m == null) {
            return null;
        }

        final String        name=m.getName();
        final Class<?>[]    params=m.getParameterTypes();
        final int           numParams=(params == null) ? 0 : params.length;
        final StringBuilder sb=new StringBuilder(name.length() + 2 + numParams * 64)
                                        .append(name)
                                        .append('(');
        for (int    pIndex=0; pIndex < numParams; pIndex++) {
            if (pIndex > 0) {
                sb.append(','); // separate from previous
            }

            final Class<?>  pType=params[pIndex];
            // NOTE: must use the full name since we are doing method matching
            sb.append(pType.getName());
        }

        return sb.append(')').toString();
    }
    
    @Override
    public boolean isEndpoint() { return true; }
    
    @Override
    public String getPluginName() { return "ejb3"; }
}

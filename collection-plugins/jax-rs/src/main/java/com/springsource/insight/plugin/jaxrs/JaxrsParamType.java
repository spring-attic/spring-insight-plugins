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
package com.springsource.insight.plugin.jaxrs;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Provides a useful encapsulation for the various type of JAX-RS parameter type annotations
 */
public enum JaxrsParamType {
    COOKIE(CookieParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((CookieParam) a).value();
            }
        },
    FORM(FormParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((FormParam) a).value();
            }
        },
    HEADER(HeaderParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((HeaderParam) a).value();
            }
        },
    MATRIX(MatrixParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((MatrixParam) a).value();
            }
        },
    PATH(PathParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((PathParam) a).value();
            }
        },
    QUERY(QueryParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue(Annotation a) throws ClassCastException {
                return (a == null) ? null : ((QueryParam) a).value();
            }
        };

    private final Class<? extends Annotation>   _annClass;
    public final Class<? extends Annotation> getAnnotationClass () {
        return _annClass;
    }
    /**
     * Extract the annotation's <I>value</I>
     * @param a The {@link Annotation} instance - may be <code>null</code>
     * @return The extracted value - <code>null</code> if <code>null</code> instance
     * @throws ClassCastException If the annotation is not of the expected type
     */
    public abstract String getValue (Annotation a) throws ClassCastException;

    JaxrsParamType (Class<? extends Annotation> annClass) {
        _annClass = annClass;
    }

    public static final Set<JaxrsParamType> VALUES=
        Collections.unmodifiableSet(EnumSet.allOf(JaxrsParamType.class));
    /**
     * Checks if the provided type name matches the {@link Class#getSimpleName()}
     * of any of the annotation classes encapsulated in the enumeration (case
     * <U>sensitive</U>)
     * @param typeName The type name - may be <code>null</code>/empty
     * @return The matching {@link JaxrsParamType} - <code>null</code> if
     * no match found
     */
    public static final JaxrsParamType fromTypeName (final String typeName) {
        if ((typeName == null) || (typeName.length() <= 0)) {
            return null;
        }

        for (final JaxrsParamType val : VALUES) {
            final Class<? extends Annotation> annClass=(val == null) ? null : val.getAnnotationClass();
            if ((annClass != null) && typeName.equals(annClass.getSimpleName())) {
                return val;
            }
        }

        return null;    // no match
    }

    public static final JaxrsParamType fromAnnotationClass (final Class<? extends Annotation> annClass) {
        if (annClass == null) {
            return null;
        }

        for (final JaxrsParamType val : VALUES) {
            if ((val != null) && (val.getAnnotationClass() == annClass)) {
                return val;
            }
        }

        return null;    // no match
    }
    
    public static final JaxrsParamType fromAnnotation (final Annotation a) {
        if (a == null) {
            return null;
        }

        final Class<?>  proxyClass=a.getClass();
        for (final JaxrsParamType val : VALUES) {
            final Class<? extends Annotation> annClass=(val == null) ? null : val.getAnnotationClass();
            /*
             * NOTE !!! we cannot use class reference equality since annotation
             *      instances are usually represented by Proxy instances of
             *      their respective classes
             */
            if ((annClass != null) && annClass.isAssignableFrom(proxyClass)) {
                return val;
            }
        }

        return null;    // no match
    }
}

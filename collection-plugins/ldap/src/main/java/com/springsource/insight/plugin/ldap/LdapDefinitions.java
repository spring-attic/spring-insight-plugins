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

import javax.naming.Name;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public final class LdapDefinitions {
    private LdapDefinitions() {
        throw new UnsupportedOperationException("No instantiation allowed");
    }

    public static final OperationType   LDAP_OP=OperationType.valueOf("javax-naming-ldap");
    public static final String  LOOKUP_NAME_ATTR="lookupName",
                                    UNKNOWN_NAME_VALUE="<unknown-name>",
                                LOOKUP_FILTER_ATTR="lookupFilter",
                                    UNKNOWN_FILTER_VALUE="<unknown-filter>";

    /**
     * @param args The call arguments
     * @return If the 1st argument is a {@link Name} or a {@link String} if
     * so, then returns its string value - {@link #UNKNOWN_NAME_VALUE} otherwise.
     */
    static String getNameValue (Object ... args) {
        if ((args == null) || (args.length <= 0)) {
            return UNKNOWN_NAME_VALUE;
        }
        
        Object  arg0=args[0];
        if (arg0 instanceof String) {
            return (String) arg0;
        } else if (arg0 instanceof Name) {
            return arg0.toString();
        }
        
        return UNKNOWN_NAME_VALUE;    // none of the above
    }
}

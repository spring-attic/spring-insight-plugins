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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public aspect DirContextSearchCollectionAspect
        extends LdapOperationCollectionAspectSupport {
    static final Map<Integer,String>    scopes=
        Collections.unmodifiableMap(new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1L;
            {
                put(Integer.valueOf(SearchControls.OBJECT_SCOPE), "Object");
                put(Integer.valueOf(SearchControls.ONELEVEL_SCOPE), "OneLevel");
                put(Integer.valueOf(SearchControls.SUBTREE_SCOPE), "SubTree");
            }
        });

    public DirContextSearchCollectionAspect () {
        super(DirContext.class, "search");
    }

    /*
     * Using call instead of execution since usually JDK core classes are used
     * - e.g., InitialDirContext, LdapDirContext - and we cannot instrument them
     */
    public pointcut searchPoint ()
        : call(* DirContext+.search(Name,Attributes,String[]))
       || call(* DirContext+.search(String,Attributes,String[]))
       || call(* DirContext+.search(Name,Attributes))
       || call(* DirContext+.search(String,Attributes))
       || call(* DirContext+.search(Name,String,SearchControls))
       || call(* DirContext+.search(String,String,SearchControls))
       || call(* DirContext+.search(Name,String,Object[],SearchControls))
       || call(* DirContext+.search(String,String,Object[],SearchControls))
        ;

    // NOTE: we use cflowbelow because the methods might delegate to one another
    public pointcut collectionPoint()
        : searchPoint() && (!cflowbelow(searchPoint()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation   op=super.createOperation(jp);
        if (op != null) {
            Object[]    args=jp.getArgs();
            String      filterValue=getFilterValue(args);
            op.label(op.getLabel() + " " + filterValue)
              .put(LdapDefinitions.LOOKUP_FILTER_ATTR, filterValue)
              ;
            if (collectExtraInformation()) {
                extractSearchControls(op.createMap("searchControls"), findLastArgument(SearchControls.class, args));
            }
        }
        
        return op;
    }

    static OperationMap extractSearchControls (OperationMap op, SearchControls sc) {
        if (sc == null) {
            return op;
        }
        
        op.put("searchScope", resolveScope(sc))
          .put("countLimit", sc.getCountLimit())
          .put("derefLinkFlag", sc.getDerefLinkFlag())
          .put("returningObjFlag", sc.getReturningObjFlag())
          .put("timeLimit", sc.getTimeLimit())
          ;

        String[]    retAttrs=sc.getReturningAttributes();
        if ((retAttrs != null) && (retAttrs.length > 0)) {
            OperationList   attrs=op.createList("returningAttributes");
            for (String attrName : retAttrs) {
                attrs.add(attrName);
            }
        }

        return op;
    }

    /**
     * @param args The call arguments
     * @return If the 2nd argument is a {@link String} and more than 2
     * arguments, then returns its string value - {@link LdapDefinitions#UNKNOWN_FILTER_VALUE}
     * otherwise
     */
    static String getFilterValue (Object ... args) {
        if ((args == null) || (args.length <= 2)) {
            return LdapDefinitions.UNKNOWN_FILTER_VALUE;
        }
        
        Object  arg1=args[1];
        if (arg1 instanceof String) {
            return (String) arg1;
        }
        
        return LdapDefinitions.UNKNOWN_FILTER_VALUE;    // none of the above
    }
    
    static String resolveScope (SearchControls sc) {
        int     scopeValue=sc.getSearchScope();
        String  scope=scopes.get(Integer.valueOf(scopeValue));
        if (StringUtil.isEmpty(scope)) {
            return String.valueOf(scopeValue);
        } else {
            return scope;
        }
    }
}

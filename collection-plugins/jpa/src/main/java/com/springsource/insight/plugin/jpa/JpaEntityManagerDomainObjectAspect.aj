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

package com.springsource.insight.plugin.jpa;

import javax.persistence.EntityManager;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 * Collects all the {@link EntityManager} actions related to a domain object
 * (except for <code>find</code> which we assume is delegated to a JDBC query
 * that makes more sense...
 */
public aspect JpaEntityManagerDomainObjectAspect extends JpaEntityManagerCollectionAspect {
    public JpaEntityManagerDomainObjectAspect () {
        super(JpaDefinitions.DOMAIN_GROUP);
    }

    // NOTE: matches 1.0 and 2.0
    public pointcut refresh () : execution(* EntityManager+.refresh(..));
    public pointcut objLock () : execution(* EntityManager+.lock(..));

    public pointcut collectionPoint()
        : execution(* EntityManager+.persist(Object))
       || execution(* EntityManager+.merge(..))
       || execution(* EntityManager+.remove(Object))
       // need the cflowbelow just in case one method delegates to another...
       || (objLock() && (!cflowbelow(objLock())))
       || (refresh() && (!cflowbelow(refresh())))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[]    args=jp.getArgs();
        Class<?>    domainClass=resolveDomainClass(args);
        return super.createOperation(jp)
                    .put(JpaDefinitions.DOMAIN_CLASS_ATTR, domainClass.getName())
                    ;
    }
    
    static Class<?> resolveDomainClass (Object ... args) {
        if ((args == null) || (args.length <= 0)) {
            return void.class;
        }
        
        Object  domainObject=args[0];
        if (domainObject == null) {
            return void.class;
        }
        
        return domainObject.getClass();
    }
}

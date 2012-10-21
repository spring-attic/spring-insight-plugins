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

package com.springsource.insight.plugin.springdata;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.FrameBuilder;

/**
 * <P><B>Note:</B> the basic assumption is that the {@link Repository} interface
 * (and its descendants) are used ONLY for spring-data proxy-ing. In other
 * words, if there is an actual class that implements this interface, then
 * <B><U>all</U></B> its methods will be intercepted and not only those that
 * are defined in an interface derived from the repository one.</P>
 * 
 * <P>It is possible to modify the aspect so as to intercept only methods
 * from interfaces derived from {@link Repository}, but this would be a
 * <B><U>runtime</U></B> filtering. In other words, we would need to use
 * reflection API to inspect each and every call in order to decide whether
 * to generate an intercepted {@link com.springsource.insight.intercept.operation.Operation}.
 * This seems very time-consuming and therefore for the initial version of
 * this aspect we will go with the basic assumption described above</P>
 *  
 * <B>Note(s)</B>:</BR>
 * <UL>
 *      <LI><P>
 *      We must use <code>call</code> since the proxy-ing mechanism does not always
 *      implement the methods but rather directly converts them to executed queries
 *      </P></LI></BR>
 * 
 *      <LI><P>
 *      We use the {@link RepositoryDefinition} annotation as well - even though
 *      the current implementation of spring-data-commons expects the annotated
 *      interface to be a {@link Repository} (which is intercepted anyway) - see
 *      {@link org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#getObject()}
 *      automatic cast. We leave it in anyway in case this is fixed or some other
 *      factory bean is used that does not have this implicit assumption
 *      </P></LI></BR>
 *      
 *      <LI><P>
 *      We use <code>cflowbelow</code> since some implementations actually
 *      provide some generic implementation (e.g., {@link org.springframework.data.jpa.repository.support.SimpleJpaRepsitory}),
 *      so the methods may also being invoked and we don't want double interception
 *      </P></LI></BR>
 * </UL>
 */
public aspect RepositoryMethodOperationCollectionAspect extends MethodOperationCollectionAspect {
    private final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    public RepositoryMethodOperationCollectionAspect () {
        super();
    }

    public pointcut repositoryCall ()
        : call(* Repository+.*(..))
       || call(* (@RepositoryDefinition *)+.*(..))
        ;

    public pointcut collectionPoint()
        : repositoryCall() && (!cflowbelow(repositoryCall()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Query 		query=null;
    	Signature	sig=jp.getSignature();
		// get method @Query annotation
    	if (sig instanceof MethodSignature) {
    		Method	method=((MethodSignature) sig).getMethod();
    		query = method.getAnnotation(Query.class);
    	}

        return super.createOperation(jp)
                    .type(SpringDataDefinitions.REPO_TYPE)
                    .putAnyNonEmpty("query", (query!=null) ? query.value() : null)
                    ;
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }

    @Override
    public String getPluginName() {
        return SpringDataPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    boolean collectExtraInformation() {
        return configuration.getCollectionLevel().equals(FrameBuilder.OperationCollectionLevel.HIGH);
    }
}

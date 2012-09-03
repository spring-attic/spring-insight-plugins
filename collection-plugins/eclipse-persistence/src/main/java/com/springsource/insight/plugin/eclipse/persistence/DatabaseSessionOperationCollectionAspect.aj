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

package com.springsource.insight.plugin.eclipse.persistence;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Login;

import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * 
 */
public aspect DatabaseSessionOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private ObscuredValueMarker obscuredMarker =
            new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder());

    public DatabaseSessionOperationCollectionAspect () {
        super();
    }

    @Override
    public String getPluginName() {
        return EclipsePersistenceDefinitions.PLUGIN_NAME;
    }

    ObscuredValueMarker getSensitiveValueMarker () {
        return obscuredMarker;
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }

    public pointcut loginPoint ()
        : execution(* DatabaseSession+.login())
       || execution(* DatabaseSession+.login(String,String))
       || execution(* DatabaseSession+.login(Login))
        ;

    public pointcut collectionPoint()
        : (loginPoint() && (!cflowbelow(loginPoint()))) // in case the methods delegate from one another
       || execution(* DatabaseSession+.logout())
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        String      methodName=sig.getName();

        /* Obscure the username/password values if the relevant login variant used
         * NOTE !!! we are relying on the fact the the Login argument variant cannot
         * be "stringified" by the current JoinPointFinalizer used by the
         * MethodOperationCollectionAspect
         */
        if ("login".equals(methodName)) {
            Object[]    args=jp.getArgs();
            if ((args != null) && (args.length == 2)) {
                if (args[0] instanceof String) {
                    obscuredMarker.markObscured(args[0]);
                }
                if (args[1] instanceof String) {
                    obscuredMarker.markObscured(args[1]);
                }
            }
        }

        return super.createOperation(jp)
                    .type(EclipsePersistenceDefinitions.DB)
                    .label("Session " + methodName)
                    .put(EclipsePersistenceDefinitions.ACTION_ATTR, methodName)
                    ;
    }

}

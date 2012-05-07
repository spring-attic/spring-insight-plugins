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
package com.springsource.insight.plugin.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.springsource.insight.collection.errorhandling.CollectionErrors;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;

/**
 * Intercepts {@link Connection#close()} call for tracking purposes
 */
public aspect JdbcConnectionCloseOperationCollectionAspect extends OperationCollectionAspectSupport {
    private final ConnectionsTracker    tracker=ConnectionsTracker.getInstance();

    public JdbcConnectionCloseOperationCollectionAspect () {
        super();
    }

    @Override
    public String getPluginName() {
        return "jdbc";
    }

    public pointcut collectionPoint()
        : execution(* Connection+.close());

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Connection conn) throws SQLException
            : collectionPoint()
           && target(conn)
           && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart)) {
        OperationCollector  collector = null;
        try {
            String url=tracker.stopTracking(conn);
            collector=(url == null) /* not tracked */ ? null : getCollector();
            if (collector != null) {
                collector.enter(ConnectionsTracker.createOperation(thisJoinPointStaticPart, url, "close"));
            }
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this.getClass(), t);
        }
        
        try {
            Object returnValue=proceed(conn);
            if (collector != null) {
                collector.exitNormal();
            }

            return returnValue;
        } catch(SQLException e) {
            if (collector != null) {
                collector.exitAbnormal(e);
            }
            throw e;
        }
    }
}

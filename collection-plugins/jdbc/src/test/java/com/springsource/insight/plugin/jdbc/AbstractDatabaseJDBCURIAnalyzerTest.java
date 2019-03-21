/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc;

import java.util.Collections;
import java.util.Date;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractDatabaseJDBCURIAnalyzerTest extends AbstractCollectionTestSupport {

	protected AbstractDatabaseJDBCURIAnalyzerTest() {
		super();
	}

	protected Operation createJdbcOperation(String jdbcUri) {
		Operation op = TestJDBCURIAnalyzer.createOperation();
		op.put(OperationFields.CONNECTION_URL, jdbcUri);
		return op;
	}

	protected Frame createJdbcFrame(Operation op) {
		Frame frame = new SimpleFrame(FrameId.valueOf("0"),
				null,
				op,
				TimeRange.milliTimeRange(0, 1),
				Collections.<Frame>emptyList());
		return frame;
	}

	protected Trace createJdbcTrace(Frame frame) {
		Trace trace = new Trace(ServerName.valueOf("fake-server"),
				ApplicationName.valueOf("fake-app"),
				new Date(),
				TraceId.valueOf("fake-id"),
				frame);
		return trace;
	}

	protected static class TestJDBCURIAnalyzer extends DatabaseJDBCURIAnalyzer {
	    static final OperationType TYPE=OperationType.valueOf("analyzer-test");
	    
	    TestJDBCURIAnalyzer () {
            this(TYPE);
	    }

        TestJDBCURIAnalyzer(OperationType type) {
            super(type);
        }

	    static Operation createOperation () {
	        return createOperation(new Operation());
	    }

	    static Operation createOperation (Operation op) {
	        return op.type(TYPE);
	    }
	}

}
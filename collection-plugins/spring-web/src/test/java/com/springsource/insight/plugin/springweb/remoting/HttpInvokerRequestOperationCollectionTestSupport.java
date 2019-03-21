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

package com.springsource.insight.plugin.springweb.remoting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public abstract class HttpInvokerRequestOperationCollectionTestSupport
        extends OperationCollectionAspectTestSupport {
    protected static final HttpInvokerRequestExecutorExternalResourceAnalyzer extresAnalyzer =
            HttpInvokerRequestExecutorExternalResourceAnalyzer.getInstance();
    protected static final HttpInvokerRequestExecutorTraceErrorAnalyzer errorsAnalyzer =
            HttpInvokerRequestExecutorTraceErrorAnalyzer.getInstance();
    protected static final String TEST_HOST = "localhost";
    protected static final int TEST_PORT = 7365;
    protected static final String TEST_URL = "http://" + TEST_HOST + ":" + TEST_PORT;

    protected HttpInvokerRequestOperationCollectionTestSupport() {
        super();
    }

    protected static List<TraceError> assertTraceError(Operation op, RemoteInvocationResult result) {
        List<TraceError> errors = errorsAnalyzer.locateErrors(creatMockOperationTraceWrapper(op));
        assertEquals("Mismatched number of errors", 1, ListUtil.size(errors));

        TraceError err = errors.get(0);
        Throwable exc = result.getException();
        assertEquals("Mismatched error text", StringFormatterUtils.formatStackTrace(exc), err.getMessage());
        return errors;
    }

    protected static ExternalResourceDescriptor assertExternalResource(Operation op) {
        Frame frame = createMockOperationWrapperFrame(op);
        ExternalResourceDescriptor desc = extresAnalyzer.extractExternalResourceDescriptor(frame);
        Boolean directCall = op.get(HttpInvokerRequestExecutorExternalResourceAnalyzer.DIRECT_CALL_ATTR, Boolean.class);
        if ((directCall != null) && directCall.booleanValue()) {
            String name = TEST_HOST + ":" + TEST_PORT;
            assertNotNull("No resource", desc);
            assertSame("Mismatched external resource frame", frame, desc.getFrame());
            assertEquals("Mismatched external resource host", TEST_HOST, desc.getHost());
            assertEquals("Mismatched external resource port", TEST_PORT, desc.getPort());
            assertEquals("Mismatched external resource name", MD5NameGenerator.getName(name), desc.getName());
            assertEquals("Mismatched external resource label", name, desc.getLabel());
            assertEquals("Mismatched external resource type", ExternalResourceType.WEB_SERVER.name(), desc.getType());
            assertFalse("Unexpected as parent external resource", desc.isParent());
            assertFalse("Not outgoing external resource", desc.isIncoming());
        } else {
            assertNull("Unexpected external resource: " + desc, desc);
        }

        return desc;
    }

    protected Operation assertRemotingOperation(HttpInvokerClientConfiguration config) {
        Operation op = getLastEntered();
        assertNotNull("No operation", op);
        assertEquals("Mismatched type", HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER, op.getType());
        assertEquals("Mismatched URI", config.getServiceUrl(), op.get(OperationFields.URI, String.class));
        assertCodebaseUrls(op.get("codebaseUrls", OperationList.class), config.getCodebaseUrl());
        return op;
    }

    protected static OperationList assertCodebaseUrls(OperationList list, String codebaseUrls) {
        if (StringUtil.isEmpty(codebaseUrls)) {
            assertNull("Unexpected encoded URL(s) list", list);
            return null;
        }

        assertNotNull("No encoded codebase URLs list", list);

        List<String> urls = StringUtil.explode(codebaseUrls, " ", true, true);
        assertEquals("Mismatched encoded list size", ListUtil.size(urls), list.size());

        for (int index = 0; index < list.size(); index++) {
            String expected = urls.get(index), actual = list.get(index, String.class);
            assertEquals("Mismatched codebase URI value at index " + index, expected, actual);
        }

        return list;
    }

    protected static HttpInvokerClientConfiguration createMockConfiguration(String path, String... codebaseUrls) {
        return createMockConfiguration(path, (ArrayUtil.length(codebaseUrls) <= 0) ? Collections.<String>emptyList() : Arrays.asList(codebaseUrls));
    }

    protected static HttpInvokerClientConfiguration createMockConfiguration(String path, Collection<String> codebaseUrls) {
        HttpInvokerClientConfiguration config = Mockito.mock(HttpInvokerClientConfiguration.class);
        Mockito.when(config.getServiceUrl()).thenReturn(TEST_URL + "/" + path);
        Mockito.when(config.getCodebaseUrl()).thenReturn(StringUtil.implode(codebaseUrls, " "));
        return config;
    }
}

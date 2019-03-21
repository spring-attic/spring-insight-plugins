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
package com.springsource.insight.plugin.apache.http.hc4;

import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class HttpPlaceholderRequestTest extends Assert {
    public HttpPlaceholderRequestTest() {
        super();
    }

    @Test
    public void testPlaceholderContents () {
        RequestLine reqLine=HttpPlaceholderRequest.PLACEHOLDER.getRequestLine();
        assertEquals("Mismatched method", HttpClientDefinitions.PLACEHOLDER_METHOD_NAME, reqLine.getMethod());
        assertEquals("Mismatched URI", HttpClientDefinitions.PLACEHOLDER_URI_VALUE, reqLine.getUri());

        ProtocolVersion protoVer=reqLine.getProtocolVersion();
        assertEquals("Mismatched protocol", "HTTP", protoVer.getProtocol());
        assertEquals("Mismatched major version", 1, protoVer.getMajor());
        assertEquals("Mismatched minor version", 1, protoVer.getMinor());
    }

    @Test
    public void testNullOrEmptyArgs () {
        assertSame("Mismatched null values result",
                   HttpPlaceholderRequest.PLACEHOLDER,
                   HttpPlaceholderRequest.resolveHttpRequest((Object[]) null));
        assertSame("Mismatched empty values result",
                   HttpPlaceholderRequest.PLACEHOLDER,
                   HttpPlaceholderRequest.resolveHttpRequest(new Object[] { }));
    }

    @Test
    public void testOnNoRequestValue () {
        assertSame(HttpPlaceholderRequest.PLACEHOLDER, HttpPlaceholderRequest.resolveHttpRequest("hello", Long.valueOf(System.nanoTime())));
    }

    @Test
    public void testOnExistingValue () {
        HttpRequest req=new HttpGet("http://localhost");
        assertSame(req, HttpPlaceholderRequest.resolveHttpRequest("hello", req, Long.valueOf(System.nanoTime())));
    }
}

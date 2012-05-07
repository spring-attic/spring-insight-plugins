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

package com.springsource.insight.plugin.apache.http.hc3;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class HttpPlaceholderMethodTest extends Assert {
    public HttpPlaceholderMethodTest() {
        super();
    }

    @Test
    public void testPlaceholderMethodContents () throws URIException {
        assertEquals("Mismatched method",
                     HttpClientDefinitions.PLACEHOLDER_METHOD_NAME,
                     HttpPlaceholderMethod.PLACEHOLDER.getName());
        assertEquals("Mismatched URI",
                     HttpClientDefinitions.PLACEHOLDER_URI_VALUE,
                     String.valueOf(HttpPlaceholderMethod.PLACEHOLDER.getURI()));
        
        StatusLine  statusLine=HttpPlaceholderMethod.PLACEHOLDER.getStatusLine();
        assertEquals("Mismatched HTTP version", "HTTP/1.1", statusLine.getHttpVersion());
        assertEquals("Mismatched status code", 500, statusLine.getStatusCode());
    }

    @Test
    public void testResolveHttpMethodOnNullOrEmptyArgs () {
        assertSame("Mismatched result for null",
                    HttpPlaceholderMethod.PLACEHOLDER,
                    HttpPlaceholderMethod.resolveHttpMethod((Object[]) null));
        assertSame("Mismatched result for empty",
                HttpPlaceholderMethod.PLACEHOLDER,
                HttpPlaceholderMethod.resolveHttpMethod(new Object[] { }));
    }

    @Test
    public void testResolveHttpMethodOnNoMethod () {
        assertSame(HttpPlaceholderMethod.PLACEHOLDER,
                   HttpPlaceholderMethod.resolveHttpMethod("1234", Long.valueOf(System.currentTimeMillis())));
    }

    @Test
    public void testResolveHttpMethodOnExistingMethod () {
        HttpMethod  method=new GetMethod("http://localhost");
        assertSame(method, HttpPlaceholderMethod.resolveHttpMethod("1234", method, Long.valueOf(System.currentTimeMillis())));
    }

}

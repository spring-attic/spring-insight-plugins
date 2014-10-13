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

package com.springsource.insight.plugin.springweb.view.resolver;

import java.util.Locale;

import org.junit.Test;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ViewResolverOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {

    public ViewResolverOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void viewResolverMonitored() throws Exception {
        ExampleViewResolver testResolver = new ExampleViewResolver();
        View view = testResolver.resolveViewName("testView", Locale.US);

        Operation operation = getLastEntered();
        assertEquals("Resolve view \"testView\"", operation.getLabel());
        assertEquals("testView", operation.get("viewName"));
        assertEquals(view.getContentType(), operation.get("contentType"));
        assertEquals("\"testUrl\"", operation.get("resolvedView"));
        assertEquals(Locale.US.toString(), operation.get("locale"));
    }

    @Test
    public void viewResolverMonitored_nullView() throws Exception {
        NullViewResolver testResolver = new NullViewResolver();
        testResolver.resolveViewName("testView", Locale.US);

        Operation operation = getLastEntered();
        assertEquals("Resolve view \"testView\"", operation.getLabel());
        assertEquals("testView", operation.get("viewName"));
        assertNull(operation.get("contentType"));
        assertNull(operation.get("resolvedView"));
        assertEquals(Locale.US.toString(), operation.get("locale"));
    }

    static class ExampleViewResolver implements ViewResolver {
        ExampleViewResolver() {
            super();
        }

        public View resolveViewName(String viewName, Locale locale) throws Exception {
            return new JstlView("testUrl");
        }
    }

    static class NullViewResolver implements ViewResolver {
        NullViewResolver() {
            super();
        }

        public View resolveViewName(String viewName, Locale locale) throws Exception {
            return null;
        }
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ViewResolverOperationCollectionAspect.aspectOf();
    }

}

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
package com.springsource.insight.plugin.servlet;

import static org.mockito.Mockito.reset;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 */
public class FilterOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public FilterOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testCached() throws Exception {
        DummyFilter dFilter = new DummyFilter();
        dFilter.init(new DummyFilterConfig());
        dFilter.doFilter(null, null, null);
        Operation first = getLastEntered();
        assertNotNull(first);
        reset(spiedOperationCollector);
        dFilter.doFilter(null, null, null);
        Operation second = getLastEntered();

        // We cache the entire Operation, but return different references
        assertTrue(first.equals(second));
        assertEquals(first.getLabel(), second.getLabel());
        assertEquals(first.get("filterClass"), second.get("filterClass"));
        assertEquals(first.get("filterName"), second.get("filterName"));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return FilterOperationCollectionAspect.aspectOf();
    }

    public static class DummyFilterConfig implements FilterConfig {

        public String getFilterName() {
            return "DummyFilter";
        }

        public ServletContext getServletContext() {
            return null;
        }

        public String getInitParameter(String name) {
            if (name.equals("punk")) return "seattle's";
            if (name.equals("rock")) return "watering";
            if (name.equals("beer")) return "hole";
            return null;
        }

        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(Arrays.asList("punk", "rock", "beer"));
        }
    }
}

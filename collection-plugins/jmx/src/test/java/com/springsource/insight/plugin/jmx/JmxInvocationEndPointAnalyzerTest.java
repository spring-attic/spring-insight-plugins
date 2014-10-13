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

package com.springsource.insight.plugin.jmx;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.springsource.insight.collection.method.custom.ScoreGeneratorsFactory;
import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.props.NamedPropertySource;
import com.springsource.insight.util.props.PropertiesUtil;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxInvocationEndPointAnalyzerTest extends AbstractCollectionTestSupport {
    private CollectionSettingsRegistry registry;
    private JmxInvocationEndPointAnalyzer analyzer;

    public JmxInvocationEndPointAnalyzerTest() {
        super();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();

        registry = new CollectionSettingsRegistry();
        analyzer = new JmxInvocationEndPointAnalyzer(registry);
    }

    @Test
    public void testDefaultFormatting() throws Exception {
        assertEndPointAnalysis(mockInvocationOperation("testDefaultFormatting"), 7365);
    }

    @Test
    public void testCustomScoreFormatting() throws Exception {
        for (String scoreType : ScoreGeneratorsFactory.BUILTIN_GENERATORS_NAMES) {
            if (ScoreGeneratorsFactory.DEFAULT_SCORE.equals(scoreType)) {
                continue;
            }

            analyzer.incrementalUpdate(JmxInvocationEndPointAnalyzer.SCORE_VALUE, scoreType);

            Operation op = mockInvocationOperation("testCustomScoreFormattingFor" + StringUtil.capitalize(scoreType));
            assertEndPointAnalysis(op, 3777347);
        }
    }

    private EndPointAnalysis assertEndPointAnalysis(Operation op, int depth) {
        EndPointAnalysis analysis = analyzer.makeEndPoint(createMockOperationWrapperFrame(op), depth);
        assertNotNull("No analysis", analysis);
        assertSame("Mismatched source operation", op, analysis.getSourceOperation());

        NamedPropertySource props = JmxInvocationEndPointAnalyzer.toPropertySource(op);
        EndPointName name = analysis.getEndPointName();
        assertEquals("Mismatched endpoint",
                PropertiesUtil.format(analyzer.getSettingFormat(JmxInvocationEndPointAnalyzer.ENDPOINT_FORMAT), props),
                name.getName());
        assertEquals("Mismatched label",
                PropertiesUtil.format(analyzer.getSettingFormat(JmxInvocationEndPointAnalyzer.LABEL_FORMAT), props),
                analysis.getResourceLabel());
        assertEquals("Mismatched example",
                PropertiesUtil.format(analyzer.getSettingFormat(JmxInvocationEndPointAnalyzer.EXAMPLE_FORMAT), props),
                analysis.getExample());

        String scoreType = analyzer.getSettingFormat(JmxInvocationEndPointAnalyzer.SCORE_VALUE);
        Number expScore = JmxInvocationEndPointAnalyzer.DEFAULT_SCORE_FORMAT.equals(scoreType)
                ? Integer.valueOf(EndPointAnalysis.depth2score(depth))
                : JmxInvocationEndPointAnalyzer.resolveOperationScore(op, scoreType);
        assertNotNull("Cannot resolve score for mode=" + scoreType, expScore);
        assertEquals("Mismatched score", expScore.intValue(), analysis.getScore());
        return analysis;
    }

    private Operation mockInvocationOperation(final String method) throws MalformedObjectNameException {
        return mockInvocationOperation(method,
                new String[]{getClass().getName(), String.class.getName(), Map.Entry.class.getName()},
                new Object[]{this, method, new Map.Entry<String, String>() {
                    public String getKey() {
                        return method;
                    }

                    public String getValue() {
                        return method;
                    }

                    public String setValue(String value) {
                        throw new UnsupportedOperationException("setValue(" + value + ") N/A");
                    }
                }});
    }

    private Operation mockInvocationOperation(String method, String[] paramsType, Object[] argVals) throws MalformedObjectNameException {
        return mockInvocationOperation(getClass(), method, paramsType, argVals);
    }

    private static Operation mockInvocationOperation(Class<?> clazz, String method, String[] paramsType, Object[] argVals) throws MalformedObjectNameException {
        Package pkg = clazz.getPackage();
        ObjectName name = new ObjectName(pkg.getName() + ":" + JmxInvocationEndPointAnalyzer.NAME_KEY + "=" + clazz.getSimpleName());
        return mockInvocationOperation(name, method, paramsType, argVals);
    }

    private static Operation mockInvocationOperation(ObjectName name, String method, String[] paramsType, Object[] argVals) {
        return JmxInvocationEndPointAnalyzer.updateOperation(
                new Operation().put(JmxPluginRuntimeDescriptor.BEAN_NAME_PROP, name.getCanonicalName()), method, paramsType, argVals);
    }
}

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

package com.springsource.insight.plugin.jsf;

import junit.framework.Assert;

import org.junit.Test;

/**
 * This tests the FaceUtils utility class.
 */
public class FacesUtilTest {

	/**
	 * Test getting a bean name from a simple JSF expression.
	 */
	@Test
	public void testBeanNameFromSimpleExpression() throws Exception {
		String expression = "#{myBean.myMethod}";
		String beanName = FacesUtils.extractBeanNameFromExpression(expression);
		Assert.assertEquals("myBean", beanName);
	}

	/**
	 * Test getting a bean name from a complex JSF expression.
	 */
	@Test
	public void testBeanNameFromComplexExpression() throws Exception {
		String expression = "#{myBean.myMethod(anotherBean.anotherMethod)}";
		String beanName = FacesUtils.extractBeanNameFromExpression(expression);
		Assert.assertEquals("myBean", beanName);
	}
	
	/**
	 * Test getting a bean name from a complex JSF expression with nested beans.
	 */
	@Test
	public void testBeanNameFromComplexExpressionNestedBeans() throws Exception {
		String expression = "#{myController.myBean.myMethod(anotherBean.anotherMethod)}";
		String beanName = FacesUtils.extractBeanNameFromExpression(expression);
		Assert.assertEquals("myController.myBean", beanName);
	}

	/**
	 * Test getting a method name from a simple JSF expression.
	 */
	@Test
	public void testMethodNameFromSimpleExpression() throws Exception {
		String expression = "#{myBean.myMethod}";
		String methodName = FacesUtils
				.extractMethodNameFromExpression(expression);
		Assert.assertEquals("myMethod", methodName);
	}

	/**
	 * Test getting a method name from a complex JSF expression.
	 */
	@Test
	public void testMethodNameFromComplexExpression() throws Exception {
		String expression = "#{myBean.myMethod(anotherBean.anotherMethod)}";
		String methodName = FacesUtils
				.extractMethodNameFromExpression(expression);
		Assert.assertEquals("myMethod(anotherBean.anotherMethod)", methodName);
	}
	
	/**
	 * Test getting a method name from a complex JSF expression with nested beans.
	 */
	@Test
	public void testMethodNameFromComplexExpressionNestedBeans() throws Exception {
		String expression = "#{myController.myBean.myMethod(anotherBean.anotherMethod)}";
		String methodName = FacesUtils
				.extractMethodNameFromExpression(expression);
		Assert.assertEquals("myMethod(anotherBean.anotherMethod)", methodName);
	}
}

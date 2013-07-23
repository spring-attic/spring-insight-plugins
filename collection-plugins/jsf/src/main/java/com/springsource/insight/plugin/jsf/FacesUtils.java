package com.springsource.insight.plugin.jsf;

public final class FacesUtils {

	private static final String JSF_EXPRESSION_START = "#{";

	private FacesUtils() {
	}

	/**
	 * Extract the bean name from a JSF expression. For example, in the
	 * expression #{myBean.myMethod} return myBean
	 * 
	 * @param expression
	 *            the JSF expression
	 * @return the name of the bean used in the expression
	 */
	public static String extractBeanNameFromExpression(String expression) {
		String beanName = null;

		if (expression.startsWith(JSF_EXPRESSION_START)) {
			beanName = expression;

			int expressionStartIndex = expression.indexOf(JSF_EXPRESSION_START);
			int methodParameterStartIndex = expression.indexOf("(");

			if (methodParameterStartIndex > -1) {
				// expresison has parameters so strip off
				beanName = beanName.substring(0, methodParameterStartIndex);
			}

			int index = beanName.lastIndexOf('.');

			beanName = beanName.substring(expressionStartIndex
					+ JSF_EXPRESSION_START.length(), index);
		}

		return beanName;
	}

	/**
	 * Extract the method name from a JSF expression. For example, in the
	 * expression #{myBean.myMethod} return myMethod
	 * 
	 * @param expression
	 *            the JSF expression
	 * @return the name of the method used in the expression
	 */
	public static String extractMethodNameFromExpression(String expression) {
		String result = null;

		String beanName = FacesUtils.extractBeanNameFromExpression(expression);
		result = expression.substring(
				(JSF_EXPRESSION_START.length() + beanName.length() + 1),
				expression.length() - 1);

		return result;
	}
}

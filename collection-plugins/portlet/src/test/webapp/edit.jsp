<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<h2>Example Portlet Preferences</h2>

<p>This is the example portlet in edit mode.</p>

<p>Here you can choose which operator to use in equations.</p>

<portlet:actionURL var="submitUrl"/>
<form name="answerForm" method="post" action='<c:out value="${submitUrl}"/>'>
  <p>
    <span class="portlet-form-field-label">Operator:</span>
    <select name="operator">
      <c:choose>
        <c:when test="${operator == '-'}">
            <option value="+">+</option>
            <option value="-" selected="selected">-</option>
        </c:when>
        <c:otherwise>
            <option value="+" selected="selected">+</option>
            <option value="-">-</option>
        </c:otherwise>
      </c:choose>
    </select>
  </p>
  <p><input type="submit" value="Set" /></p>
</form>

<p>You can also switch this portlet to
<a href="<portlet:renderURL portletMode="view"/>">view mode</a> or
<a href="<portlet:renderURL portletMode="help"/>">help mode</a>.</p>

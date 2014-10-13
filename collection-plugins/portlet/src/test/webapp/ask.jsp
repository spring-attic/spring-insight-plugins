<%@page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<h2>Answer Equation</h2>

<c:choose>
    <c:when test="${answerCorrect == true}">
        <p class="portlet-msg-success">Your answer was correct!</p>
    </c:when>
    <c:when test="${answerCorrect == false}">
        <p class="portlet-msg-error">Your answer was incorrect!</p>
    </c:when>
</c:choose>

<p>Welcome, this is the example portlet in view mode. Please answer the following equation.</p>

<form name="answerForm" method="post"
      action='<portlet:actionURL/>?term1=<c:out value="${term1}"/>&term2=<c:out value="${term2}"/>&operator=<c:out value="${operator}"/>'>
    <p>
        <c:out value="${term1}"/>
        <c:out value="${operator}"/>
        <c:out value="${term2}"/>
        = <input type="text" name="answer" size="5"/>
    </p>

    <p>
        <input type="submit" value="Answer"/>
    </p>
</form>

<p>You can also switch this portlet to
    <a href="<portlet:renderURL portletMode="edit"/>">edit mode</a> or
    <a href="<portlet:renderURL portletMode="help"/>">help mode</a>.</p>

<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<h2>Example Portlet Help</h2>

<p>This is the example portlet in help mode.</p>

<p>In <a href="<portlet:renderURL portletMode="view"/>">view mode</a> this
portlet will ask you answers for simple equations.</p>

<p>In <a href="<portlet:renderURL portletMode="edit"/>">edit mode</a> you
can change the operator ('+' or '-') to be used in equations.</p>

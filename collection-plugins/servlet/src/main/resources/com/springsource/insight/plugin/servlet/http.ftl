<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<#assign request = operation.request>
<#assign response = operation.response>


<@insight.title>HTTP Summary</@insight.title>
<code class="raw http">${request.method?html} ${request.uri?html}<#if request.queryString?has_content>?${request.queryString?html}</#if> ${request.protocol?html}</code>
<code class="raw http">${request.protocol?html} ${response.statusCode?html} ${response.reasonPhrase?html}</code>

<@insight.group>
    <@insight.entry name="Requested From" value=request.remoteAddr required="true" />
    <@insight.entry name="Bytes Written" value=response.contentSize required="true" />
    <@insight.entry name="Requested Session" value=request.sessionId />
    <@insight.entry name="Principal" value=request.userPrincipal />
    <@insight.entry name="Locale" value=request.locale />
    <@insight.entry name="Context" value=request.contextPath />
</@insight.group>

<@insight.group label="Request Parameters" if=request.queryParams?has_content collection=request.queryParams ; p>
    <@insight.entry name=p.name value=p.value required="true" />
</@insight.group>

<@insight.group label="Request Headers" if=request.headers?has_content collection=request.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

<@insight.group label="Response Headers" if=response.headers?has_content collection=response.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

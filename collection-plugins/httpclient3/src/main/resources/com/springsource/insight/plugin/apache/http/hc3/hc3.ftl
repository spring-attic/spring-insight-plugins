<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<#assign request = operation.request>
<#assign response = operation.response>

<@insight.title>Apache HC3 Summary</@insight.title>
<code class="raw http">${request.method?html} ${request.uri?html} ${request.protocol?html}</code>
<code class="raw http"><#if request.protocol??>${request.protocol?html}</#if> <#if request.statusCode??>${response.statusCode?html}</#if> <#if request.reasonPhrase??>${response.reasonPhrase?html}</#if></code>

<@insight.group label="Request Headers" if=request.headers?has_content collection=request.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

<@insight.group label="Response Headers" if=response.headers?has_content collection=response.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

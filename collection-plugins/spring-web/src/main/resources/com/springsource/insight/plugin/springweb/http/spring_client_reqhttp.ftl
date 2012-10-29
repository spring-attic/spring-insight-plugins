<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<#assign request = operation.request>
<#assign response = operation.response>

<@insight.title>Spring HTTP Client Request</@insight.title>
<code class="raw http">${request.method?html} ${request.uri?html}</code>
<code class="raw http">${response.statusCode?html} ${response.reasonPhrase?html}</code>

<@insight.group label="Request Headers" if=request.headers?has_content collection=request.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

<@insight.group label="Response Headers" if=response.headers?has_content collection=response.headers ; h>
    <@insight.entry name=h.name value=h.value required="true" />
</@insight.group>

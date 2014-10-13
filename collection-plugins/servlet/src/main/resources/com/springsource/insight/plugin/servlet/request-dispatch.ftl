<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.title>${operation.label}</@insight.title>
<@insight.group>
    <@insight.entry name="Application" value=operation.application required="true" />
    <@insight.entry name="URI" value=operation.uri required="true" />
    <@insight.trace name="Trace" value=operation.traceId/>
</@insight.group>

<#if operation.request??>
    <@insight.group label="Request Parameters" if=operation.request.queryParams?has_content collection=operation.request.queryParams ; p>
        <@insight.entry name=p.name value=p.value required="true" />
    </@insight.group>

    <@insight.group label="Request Headers" if=operation.request.headers?has_content collection=operation.request.headers ; h>
        <@insight.entry name=h.name value=h.value required="true" />
    </@insight.group>

    <@insight.group label="Request Attributes" if=operation.request.requestAttributes?has_content collection=operation.request.requestAttributes ; h>
        <@insight.entry name=h.name value=h.value required="true" />
    </@insight.group>
</#if>

<#if operation.exception??>
    <@insight.group label="Exception Details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

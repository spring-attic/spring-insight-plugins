<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Servlet Context Listener">
    <@insight.entry name="Listener" value=operation.listenerClass />
    <@insight.entry name="Application" value=operation.application />
</@insight.group>

<@insight.group label="Context Params" if=operation.contextParams?has_content collection=operation.contextParams?keys ; p>
    <@insight.entry name=p value=operation.contextParams[p] />
</@insight.group>

<#if operation.exception??>
    <@insight.group label="Exception Details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

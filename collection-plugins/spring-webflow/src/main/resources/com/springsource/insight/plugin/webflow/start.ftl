<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Flow Name" value=operation.flowId/>
</@insight.group>

<#if operation.initParams?has_content>
    <@insight.group label="Init Params" collection=operation.initParams?keys ; p>
        <@insight.entry name=p value=operation.initParams[p] />
    </@insight.group>
</#if>

<#if operation.exception??>
    <@insight.group label="Exception Details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

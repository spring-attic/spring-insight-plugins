<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Init Binder">
    <@insight.entry name="Target Class" value=operation.targetType />
    <@insight.entry name="Required Fields">
        <#if operation.requiredFields?has_content>
            <@insight.list collection=operation.requiredFields />
        <#else>
        <em>none specified</em>
        </#if>
    </@insight.entry>
    <@insight.entry name="Allowed Fields">
        <#if operation.allowedFields?has_content>
            <@insight.list collection=operation.allowedFields />
        <#else>
        <em>none specified</em>
        </#if>
    </@insight.entry>
    <@insight.entry name="Disallowed Fields">
        <#if operation.disallowedFields?has_content>
            <@insight.list collection=operation.disallowedFields />
        <#else>
        <em>none specified</em>
        </#if>
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

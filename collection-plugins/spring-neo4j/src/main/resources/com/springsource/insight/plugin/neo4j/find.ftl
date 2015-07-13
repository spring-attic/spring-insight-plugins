<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Entity Class" value=operation.entityClass/>
    <#if operation.entityId??>
        <@insight.entry name="Entity Id" value=operation.entityId/>
    </#if>
    <#if operation.propertyName??>
        <@insight.entry name="Property Name" value=operation.propertyName/>
    </#if>
    <#if operation.propertyValue??>
        <@insight.entry name="Property Value" value=operation.propertyValue/>
    </#if>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

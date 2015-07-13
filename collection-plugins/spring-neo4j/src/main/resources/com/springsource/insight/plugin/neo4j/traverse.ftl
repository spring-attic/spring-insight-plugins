<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <#if operation.entity??>
        <@insight.entry name="Entity" value=operation.entity />
        <@insight.entry name="Target Type" value=operation.targetType />
    </#if>
    <#if operation.start??>
        <@insight.entry name="start" value=operation.start />
    </#if>
    <@insight.entry name="Traversal Description" value=operation.traversalDescription />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


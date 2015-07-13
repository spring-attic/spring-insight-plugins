<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <#if operation.indexedType??>
        <@insight.entry name="Indexed Type" value=operation.indexedType />
        <@insight.entry name="Property Name" value=operation.propertyName />
        <@insight.entry name="Value" value=operation.value />
    </#if>

    <#if operation.indexName??>
        <@insight.entry name="Index Name" value=operation.indexName />
        <@insight.entry name="Field" value=operation.field />
        <@insight.entry name="Value" value=operation.value />
    </#if>

    <#if operation.query??>
        <@insight.entry name="Query" value=operation.query />
    </#if>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


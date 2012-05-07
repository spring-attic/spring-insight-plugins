<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Hibernate ${operation.method?html} Operation">
    <#if operation.entityCount??>
        <@insight.entry name="Entities managed by current session" value=operation.entityCount />
    </#if>
    <#if operation.collectionCount??>
        <@insight.entry name="Collections managed by current session" value=operation.collectionCount />
    </#if>
    <@insight.entry name="Flush mode" value=operation.flushMode />
</@insight.group>

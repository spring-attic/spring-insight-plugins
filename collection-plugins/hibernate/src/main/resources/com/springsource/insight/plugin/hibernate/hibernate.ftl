<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Hibernate ${operation.method?html} Operation">
    <@insight.entry name="Entities managed by current session" value=operation.entityCount if=operation.entityCount?? />
    <@insight.entry name="Collections managed by current session" value=operation.collectionCount if=operation.collectionCount?? />
    <@insight.entry name="Flush mode" value=operation.flushMode />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Saved Entity Class" value=operation.entityClass/>
    <@insight.entry name="Relationship Type" value=operation.relationship if=operation.relationship?? />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
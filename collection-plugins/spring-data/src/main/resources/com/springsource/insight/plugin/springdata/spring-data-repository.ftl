<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring data call">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Query" value=operation.query if=operation.query?? />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

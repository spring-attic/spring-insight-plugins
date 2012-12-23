<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="RMI Call">
    <@insight.entry name="RMI" value=operation.label />
    <@insight.entry name="Remotes name list" value=operation.list />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

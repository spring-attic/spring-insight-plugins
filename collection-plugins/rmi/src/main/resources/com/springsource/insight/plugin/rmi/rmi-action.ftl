<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="RMI Call">
    <@insight.entry name="RMI" value=operation.label />
    <@insight.entry name="Remote name" value=operation.name />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Input">
    <@insight.entry name="Key" value=operation.key />
    <@insight.entry name="Value" value=operation.value />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


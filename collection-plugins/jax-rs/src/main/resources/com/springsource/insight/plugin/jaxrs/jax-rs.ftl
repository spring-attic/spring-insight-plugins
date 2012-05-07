<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="REST Call">
    <@insight.entry name="REST" value=operation.label />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="File Operation">
    <@insight.entry name="File Operation" value=operation.label />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

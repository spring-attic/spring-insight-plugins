<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JWS Call">
    <@insight.entry name="JWS" value=operation.label />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

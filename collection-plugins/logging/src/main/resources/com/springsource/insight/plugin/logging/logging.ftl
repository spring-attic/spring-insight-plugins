<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Error logging">
    <@insight.entry name="Error message" value=operation.label />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

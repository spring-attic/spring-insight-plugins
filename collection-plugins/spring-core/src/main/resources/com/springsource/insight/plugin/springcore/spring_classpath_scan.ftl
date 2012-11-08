<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Components Scan">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Method" value=operation.methodName />
    <@insight.entry name="Location" value=operation.eventInfo />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

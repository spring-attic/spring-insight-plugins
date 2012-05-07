<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Quartz Scheduled Execution">
    <@insight.entry name="Quartz Scheduled Execution" value=operation.label />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

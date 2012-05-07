<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Batch">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Job" value=operation.jobName />
    <@insight.entry name="Step" value=operation.stepName />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Event Publish">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Method" value=operation.actionType />
    <@insight.entry name="Event" value=operation.eventInfo />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Application Event Listener">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Event" value=operation.eventInfo />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

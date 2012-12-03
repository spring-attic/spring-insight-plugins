<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Navigation Details">
    <@insight.entry name="From Action" value=operation.fromAction />
    <@insight.entry name="Outcome" value=operation.outcome />
</@insight.group>

<@insight.group label="Implementation Details">
    <@insight.entry name="Implementation Class" value=operation.className />
    <@insight.entry name="Implementation Method" value=operation.methodName />
</@insight.group>
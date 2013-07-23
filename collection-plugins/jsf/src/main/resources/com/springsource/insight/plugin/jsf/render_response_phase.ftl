<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Phase Details">
    <@insight.entry name="View Id" value=operation.viewId />
</@insight.group>

<@insight.group label="Implementation Details">
    <@insight.entry name="Implementation Class" value=operation.className />
    <@insight.entry name="Implementation Method" value=operation.methodName />
</@insight.group>

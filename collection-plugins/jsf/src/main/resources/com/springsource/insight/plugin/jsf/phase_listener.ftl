<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Phase Listener Details">
    <@insight.entry name="Phase Id" value=operation.phaseId />
    
    <@insight.entry name="Exception" value=operation.exception />
</@insight.group>

<@insight.group label="Implementation Details">
    <@insight.entry name="Implementation Class" value=operation.className />
    <@insight.entry name="Implementation Method" value=operation.methodName />
</@insight.group>
<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Action Details">
    <@insight.entry name="Target Class" value=operation.implementationClass />
    <@insight.entry name="Target Method Signature" value=operation.implementationClassMethodSignature />
</@insight.group>

<@insight.group label="Implementation Details">
    <@insight.entry name="Implementation Class" value=operation.className />
    <@insight.entry name="Implementation Method" value=operation.methodName />
</@insight.group>

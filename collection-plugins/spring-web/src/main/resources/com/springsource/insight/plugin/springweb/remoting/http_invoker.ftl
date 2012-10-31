<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Web Remoting">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Request" value=operation.uri />
    <@insight.entry name="Exception" value=operation.remoteException if=operation.remoteException?? />
</@insight.group>

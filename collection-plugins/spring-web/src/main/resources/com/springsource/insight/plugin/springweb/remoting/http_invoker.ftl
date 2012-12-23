<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Web Remoting">
    <@insight.entry name="Service Interface" value=operation.serviceInterface if=operation.serviceInterface?? />
    <@insight.entry name="Service Method" value=operation.remoteMethodSignature if=operation.remoteMethodSignature?? />
    <@insight.entry name="Request" value=operation.uri />
    <@insight.entry name="Remote Exception" value=operation.remoteException if=operation.remoteException?? />
    <@insight.entry name="Local Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

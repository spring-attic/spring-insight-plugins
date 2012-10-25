<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Integration TCP Connection">
    <@insight.entry name="Host" value=operation.hostAddress />
    <@insight.entry name="Port" value=operation.port />
    <@insight.entry name="Server mode" value=operation.serverMode />
    <@insight.entry name="Connection-ID" value=operation.connectionId if=operation.connectionId?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Akka UntypedActor Operation">
    <@insight.entry name="Actor" value=operation.Actor />
    <@insight.entry name="Actor Full Name" value=operation.className />
    <@insight.entry name="Actor Path" value=operation.ActorPath />
    <@insight.entry name="Message Type" value=operation.MessageType />
    <@insight.entry name="Sender Path" value=operation.SenderPath />
    <@insight.entry name="System" value=operation.System />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />


<@insight.group label="Spring Integration Message Adapter">
    <@insight.entry name=operation.siComponentType value=operation.beanName />
    <@insight.entry name="Listens on Queues" value=operation.listeningOnQueues />
    <@insight.entry name="Output Channel" value=operation.outputChannel />
    <@insight.entry name="Message Exchange" value=operation.messageExchange />
    <@insight.entry name="Message Routing Key" value=operation.messageRoutingKey />
    <@insight.entry name="Message Content Type" value=operation.messageContentType />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
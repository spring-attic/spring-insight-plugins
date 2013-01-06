<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Destination">
    <@insight.entry name="Type" value=operation.destinationType />
    <@insight.entry name="Name" value=operation.destinationName />
</@insight.group>

<@insight.group label="Message Details">
    <@insight.entry name="Type" value=operation.messageType />
    <@insight.entry name="Content" value=operation.messageContent />

    <#if operation.selector??>
        <@insight.entry name="Selector" value=operation.selector />
    </#if>

    <#if operation.listener??>
        <@insight.entry name="listener" value=operation.listener />
    </#if>

    <@insight.entry name="Content Map" if=operation.messageContentMap??>
        <@insight.group collection=operation.messageContentMap?keys ; key>
            <@insight.entry name=key value=operation.messageContentMap[key] />
        </@insight.group>
    </@insight.entry>
</@insight.group>

<#if operation.messageHeaders?has_content>
    <@insight.group label="Message Headers" collection=operation.messageHeaders?keys ; key>
        <@insight.entry name=key value=operation.messageHeaders[key] />
    </@insight.group>
</#if>

<#if operation.messageProperties?has_content>
    <@insight.group label="Message Properties" collection=operation.messageProperties?keys ; key>
        <@insight.entry name=key value=operation.messageProperties[key] />
    </@insight.group>
</#if>

<#if operation.connectionData??>
    <@insight.group label="Connection Data" collection=operation.connectionData?keys ; key>
        <@insight.entry name=key value=operation.connectionData[key] />
    </@insight.group>
</#if>

<#if operation.exception??>
    <@insight.group label="Exception Details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
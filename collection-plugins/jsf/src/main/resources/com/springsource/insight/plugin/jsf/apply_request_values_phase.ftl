<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Request Parameters">
    <@insight.group collection=operation.requestParameters?keys ; key>
        <@insight.entry name=key value=operation.requestParameters[key] />
    </@insight.group>
</@insight.group>

<@insight.group label="Request Headers">
    <@insight.group collection=operation.requestHeaders?keys ; key>
        <@insight.entry name=key value=operation.requestHeaders[key] />
    </@insight.group>
</@insight.group>

<@insight.group label="Implementation Details">
    <@insight.entry name="Implementation Class" value=operation.className />
    <@insight.entry name="Implementation Method" value=operation.methodName />
</@insight.group>

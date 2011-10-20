<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />
<#if operation.method == "get">
    <@insight.group label="${operation.label?html}">
        <@insight.entry name="Key" value=operation.key if=operation.key?? />
    </@insight.group>
</#if>

<#if operation.method == "put" || operation.method == "putIfAbsent">
    <@insight.group label="${operation.label?html}">
        <@insight.entry name="Key" value=operation.key if=operation.key?? />
        <@insight.entry name="Value" value=operation.value if=operation.value?? />
    </@insight.group>
</#if>

<#if operation.method == "putAll">
    <@insight.group label="${operation.label?html}">
        <@insight.entry name="Size" value=operation.size if=operation.size?? />
    </@insight.group>
</#if>

<#if operation.method == "remove">
    <@insight.group label="${operation.label?html}">
        <@insight.entry name="Key" value=operation.key if=operation.key?? />
        <#--<#if operation.arglen == 2>-->
            <#--<@insight.entry name="Value" value=operation.value if=operation.value?? />-->
        <#--</#if>-->
    </@insight.group>
</#if>

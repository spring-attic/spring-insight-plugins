<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />
<#if operation.method == "add">
    <@insight.group label="${operation.label?html}">
        <@insight.entry name="Value" value=operation.value if=operation.value?? />
    </@insight.group>
</#if>

<#if operation.method == "addAll">
    <@insight.group label="${operation.label?html}">
    <@insight.entry name="Size" value=operation.size if=operation.size?? />
    </@insight.group>
</#if>

<#if operation.method == "remove">
    <@insight.group label="${operation.label?html}">
    <@insight.entry name="Value" value=operation.value if=operation.value?? />
    </@insight.group>
</#if>

<#if operation.method == "removeAll">
    <@insight.group label="${operation.label?html}">
    <@insight.entry name="Size" value=operation.size if=operation.size?? />
    </@insight.group>
</#if>


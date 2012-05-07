<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Grails Controller">
    <@insight.entry name="Request">
        ${operation.requestMethod?html} ${operation.requestUri?html}
    </@insight.entry>
    <@insight.entry name="Params">
        <#if operation.actionParams?has_content>
            <@insight.group collection=operation.actionParams ; param>
                <@insight.entry name=param.key value=param.value />
            </@insight.group>
         <#else>
            <em>empty</em>
        </#if>
    </@insight.entry>
</@insight.group>

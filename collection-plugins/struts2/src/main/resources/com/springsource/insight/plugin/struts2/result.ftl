<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Result" value=operation.resultCode />
    <@insight.entry name="View" value=operation.view />
</@insight.group>

<#if operation.errs?has_content>
    <@insight.group label="Errors" collection=operation.errs?keys ; p>
        <@insight.entry name=p value=operation.errs[p] />
    </@insight.group>
</#if>

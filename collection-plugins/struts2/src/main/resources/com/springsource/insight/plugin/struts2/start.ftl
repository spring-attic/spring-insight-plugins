<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="ActionName" value=operation.actionName />
</@insight.group>

<#if operation.params?has_content>
    <@insight.group label="Parameters" collection=operation.params?keys ; p>
        <@insight.entry name=p value=operation.params[p] />
    </@insight.group>
</#if>

<#if operation.exception??>
	<@insight.group label="Exception Details">
		<@insight.entry name="Exception" value=operation.exception/>
	</@insight.group>
</#if>

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="On" value=operation.codeId />
    <@insight.entry name="To" value=operation.stateId />
</@insight.group>

<#if operation.attribs?has_content>
    <@insight.group label="Attributes" collection=operation.attribs?keys ; p>
        <@insight.entry name=p value=operation.attribs[p] />
    </@insight.group>
</#if>

<#if operation.actions?has_content>
    <@insight.group label="Actions" collection=operation.actions ; p>
        <@insight.entry name="Expression" value=p />
    </@insight.group>
</#if>

<#if operation.exception??>
	<@insight.group label="Exception Details">
		<@insight.entry name="Exception" value=operation.exception/>
	</@insight.group>
</#if>

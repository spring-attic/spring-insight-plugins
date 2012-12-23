<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
	<@insight.entry name="Name" value=operation.stateId />
    <#if operation.view??>
    	<@insight.entry name="View" value=operation.view />
    </#if>
</@insight.group>

<#if operation.binds?has_content>
    <@insight.group label="Binding" collection=operation.binds ; p>
        <@insight.entry name="Property" value=p />
    </@insight.group>
</#if>

<#if operation.attribs?has_content>
    <@insight.group label="Attributes" collection=operation.attribs?keys ; p>
        <@insight.entry name=p value=operation.attribs[p] />
    </@insight.group>
</#if>

<#if operation.entryActions?has_content>
    <@insight.group label="Entry Actions" collection=operation.entryActions ; p>
        <@insight.entry name="Expression" value=p />
    </@insight.group>
</#if>
<#if operation.actions?has_content>
    <@insight.group label="Actions" collection=operation.actions ; p>
        <@insight.entry name="Expression" value=p />
    </@insight.group>
</#if>
<#if operation.exitActions?has_content>
    <@insight.group label="Exit Actions" collection=operation.exitActions ; p>
        <@insight.entry name="Expression" value=p />
    </@insight.group>
</#if>

<#if operation.exception??>
	<@insight.group label="Exception Details">
		<@insight.entry name="Exception" value=operation.exception/>
	</@insight.group>
</#if>

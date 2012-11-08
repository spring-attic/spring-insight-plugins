<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
	<@insight.entry name="Entity Class" value=operation.entityClass/>
	<#if operation.entityId??>
	   <@insight.entry name="Entity Id" value=operation.entityId/>
	</#if>
	
	<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
</@insight.group>


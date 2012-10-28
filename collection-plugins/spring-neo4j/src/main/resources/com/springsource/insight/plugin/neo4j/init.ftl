<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <#if operation.service??>
	   <@insight.entry name="DatabaseService" value=operation.service/>
	</#if>
	<#if operation.serviceUri??>
       <@insight.entry name="DatabaseServiceURI" value=operation.serviceUri/>
    </#if>  
	
	<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
</@insight.group>


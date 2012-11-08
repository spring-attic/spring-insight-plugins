<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
	<@insight.entry name="statement" value=operation.statement/>
	
	<#if operation.params?has_content>
        <@insight.group label="Parameters" collection=operation.params?keys ; p>
            <@insight.entry name=p value=operation.params[p] />
        </@insight.group>
    </#if>
    
    <@insight.sourceCodeLocation location=operation.sourceCodeLocation />
</@insight.group>


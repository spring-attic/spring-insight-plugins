<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="GemFire Remote Operation">
    <@insight.entry name="Message Type">
    	${operation.message_type?html}
    </@insight.entry>
    <@insight.entry name="Host">
    	${operation.host?html}
    </@insight.entry>
    <@insight.entry name="Port">
    	${operation.port?html}
    </@insight.entry>    
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="GemFire Region Operation">
    <@insight.entry name="Arguments" if=operation.args?has_content>
        <@insight.list type="ordered" collection=operation.args />
    </@insight.entry>
    <@insight.entry name="Return Value">
    	${operation.returnValue?html}
    </@insight.entry>
    <@insight.entry name="Full Path">
    	${operation.fullPath?html}
    </@insight.entry>
    <@insight.entry name="Server" if=operation.servers?has_content>
        <@insight.list type="ordered" collection=operation.servers />
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />
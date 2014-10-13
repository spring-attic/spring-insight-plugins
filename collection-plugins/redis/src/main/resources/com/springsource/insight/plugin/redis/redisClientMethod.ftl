<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.title>
Redis Client
</@insight.title>

<@insight.group>
    <@insight.entry name="Arguments" if=operation.arguments?has_content>
        <@insight.list type="ordered" collection=operation.arguments />
    </@insight.entry>
    <@insight.entry name="Return Value" value=operation.returnValue />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

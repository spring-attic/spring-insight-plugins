<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JPA Transaction Entity Operation">
    <@insight.entry name="Action" value=operation.methodName />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

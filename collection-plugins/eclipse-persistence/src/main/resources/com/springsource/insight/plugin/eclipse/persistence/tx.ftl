<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Eclipse Persistence Transaction">
    <@insight.entry name="Action" value=operation.action />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

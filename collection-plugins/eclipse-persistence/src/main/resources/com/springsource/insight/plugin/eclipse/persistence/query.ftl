<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Eclipse Persistence Query">
    <@insight.entry name="Query name" value=operation.action />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

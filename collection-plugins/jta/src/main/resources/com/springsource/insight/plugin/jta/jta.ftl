<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JTA Transaction">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Type" value=operation.shortClassName />
    <@insight.entry name="Action" value=operation.action />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

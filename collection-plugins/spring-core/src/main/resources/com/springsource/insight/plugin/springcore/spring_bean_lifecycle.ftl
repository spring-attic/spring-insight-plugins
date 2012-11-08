<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Bean Initialization">
    <@insight.entry name="Label" value=operation.label />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

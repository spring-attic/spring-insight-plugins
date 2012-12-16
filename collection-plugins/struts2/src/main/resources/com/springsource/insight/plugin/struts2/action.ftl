<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Action" value=operation.action />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
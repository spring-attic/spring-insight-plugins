<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Interceptor" value=operation.interceptor />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>
<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Web Dispatch">
    <@insight.entry name="Request">
    ${operation.method?html} ${operation.uri?html}
    </@insight.entry>
    <@insight.entry name="Request Error" value=operation.requestErrorStack if=operation.error />
    <@insight.entry name="Local Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JSP Compilation">
    <@insight.entry name="JSP" value=operation.jspName />
    <@insight.entry name="Compiler" value=operation.compiler />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

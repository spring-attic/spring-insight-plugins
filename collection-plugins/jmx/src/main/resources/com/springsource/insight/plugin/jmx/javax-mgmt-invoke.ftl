<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JMX Method Invocation">
    <@insight.entry name="Bean name" value=operation.beanName />
    <@insight.entry name="Method" value=operation.label />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

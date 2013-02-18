<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JMX Attribute Access">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Bean name" value=operation.beanName />
    <@insight.entry name="Attribute(s)" value=operation.attrName />
    <@insight.entry name="Set Value" value=operation.attrValue if=operation.attrValue?? />
    <@insight.entry name="Retrieved Value" value=operation.returnValue if=operation.returnValue?? />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.group label="Multiple Attributes Access" if=operation.attrList?has_content collection=operation.attrList ; p>
    <@insight.entry name=p.name value=p.value required="true" />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

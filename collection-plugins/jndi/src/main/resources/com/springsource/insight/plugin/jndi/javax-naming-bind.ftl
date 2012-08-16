<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JNDI Binding">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Name" value=operation.name />
    <@insight.entry name="Value" value=operation.value />
    <@insight.entry name="Environment" if=operation.environment??>
        <@insight.group collection=operation.environment?keys ; key>
            <@insight.entry name=key value=operation.environment[key] />
        </@insight.group>
    </@insight.entry>
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

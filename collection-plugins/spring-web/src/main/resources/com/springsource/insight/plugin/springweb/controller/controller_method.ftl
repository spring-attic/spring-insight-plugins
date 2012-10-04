<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Web Controller">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Model argument" if=operation.modelArgument??>
        <@insight.group collection=operation.modelArgument?keys ; key>
            <@insight.entry name=key value=operation.modelArgument[key] />
        </@insight.group>
    </@insight.entry>
    <@insight.entry name="Return model" if=operation.returnModel??>
        <@insight.group collection=operation.returnModel?keys ; key>
            <@insight.entry name=key value=operation.returnModel[key] />
        </@insight.group>
    </@insight.entry>
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Input">
    <@insight.entry name="Key" value=operation.key />
    <@insight.entry name="Values" if=operation.values?has_content>
        <@insight.list type="ordered" collection=operation.values />
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


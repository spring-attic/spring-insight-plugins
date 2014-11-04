<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="DBCursor">
    <@insight.entry name="Params" if=operation.args?has_content>
        <@insight.list type="ordered" collection=operation.args />
    </@insight.entry>
    <@insight.entry name="Query">
    ${operation.query?html}
    </@insight.entry>
    <@insight.entry name="Keys Wanted">
    ${operation.keysWanted?html}
    </@insight.entry>
    <@insight.entry name="Return">
    ${operation.returnValue?html}
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

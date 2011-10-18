<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="DBCollection">
    <@insight.entry name="Collection">
    	${operation.collection?html}
    </@insight.entry>
    <@insight.entry name="Params" if=operation.args?has_content>
        <@insight.list type="ordered" collection=operation.args />
    </@insight.entry>
    <@insight.entry name="Return">
    	${operation.returnValue?html}
    </@insight.entry>
</@insight.group>


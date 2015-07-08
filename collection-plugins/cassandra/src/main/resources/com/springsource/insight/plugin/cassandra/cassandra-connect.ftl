<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="JDBC Connect">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Connection URL" value=operation.connectionUrl />
    <@insight.entry name="Parameters" if=operation.params??>
    <!-- jdbc parameters can be indexed or mapped -->
        <#if operation.params?is_enumerable>
            <@insight.list type="ordered" collection=operation.params />
        <#elseif operation.params?is_hash>
            <@insight.group collection=operation.params?keys ; key>
                <@insight.entry name=key value=operation.params[key] />
            </@insight.group>
        </#if>
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

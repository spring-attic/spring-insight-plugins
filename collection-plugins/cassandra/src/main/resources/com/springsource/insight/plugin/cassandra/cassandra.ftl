<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="CQL Query">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="CQL">
    <code>${springBeans.cqlToHtml.toHtml(operation.cql, 'sql-keyword')}</code>
    </@insight.entry>
    <@insight.entry name="Parameters" if=operation.params??>
        <#if operation.params?is_hash>
            <@insight.group collection=operation.params?keys ; key>
                <@insight.entry name=key value=operation.params[key] />
            </@insight.group>
        </#if>
    </@insight.entry>
    <@insight.entry name="Cluster" value=operation.clustername />
    <@insight.entry name="Keyspace" value=operation.keyspace />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

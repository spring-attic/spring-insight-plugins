<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Server" value=operation.server />
    
    <@insight.entry name="CQL Query" value=operation.query />
    <@insight.entry name="Compression" value=operation.compression />
    <@insight.entry name="Query ID" value=operation.queryId />
    
    <@insight.entry name="Parameters" if=operation.params?has_content>
        <@insight.list type="ordered" collection=operation.params />
    </@insight.entry>
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


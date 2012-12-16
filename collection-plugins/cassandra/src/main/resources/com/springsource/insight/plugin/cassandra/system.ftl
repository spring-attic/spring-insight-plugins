<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Server" value=operation.server />    
    <@insight.entry name="Keyspace" value=operation.keyspace />
    <@insight.entry name="Strategy Class" value=operation.class />
    <@insight.entry name="ColumnFamily" value=operation.columnFamily />
    <#if operation.columnFamilyDef?has_content>
        <@insight.group label="ColumnFamily Definition" collection=operation.columnFamilyDef?keys ; k>
            <@insight.entry name=k value=operation.columnFamilyDef[k] />
        </@insight.group>
    </#if>
    <@insight.entry name="Columns Definition" if=operation.columnsDef?has_content>
        <@insight.list type="ordered" collection=operation.columnsDef />
    </@insight.entry>
    <#if operation.credentials?has_content>
        <@insight.group label="Credentials" collection=operation.credentials?keys ; k>
            <@insight.entry name=k value=operation.credentials[k] />
        </@insight.group>
    </#if>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


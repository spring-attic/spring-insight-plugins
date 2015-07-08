<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Server" value=operation.server />
    <@insight.entry name="Key" value=operation.key />
    <@insight.entry name="ColumnFamily" value=operation.columnFamily />
    <@insight.entry name="SuperColumn" value=operation.superColumn />
    <@insight.entry name="Column Name" value=operation.colName />
    <@insight.entry name="Column Value" value=operation.colValue />
    <@insight.entry name="Column Timestamp" value=operation.colTimestamp />
    <#if operation.tables?has_content>
        <@insight.group label="Tables" collection=operation.tables?keys ; k>
            <@insight.entry name=k value=operation.tables[k] />
        </@insight.group>
    </#if>
    <@insight.entry name="Consistency Level" value=operation.consistLevel />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


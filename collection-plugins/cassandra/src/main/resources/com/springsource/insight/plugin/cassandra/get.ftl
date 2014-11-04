<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Server" value=operation.server />

    <@insight.entry name="Key" value=operation.key />
    <@insight.entry name="ColumnFamily" value=operation.columnFamily />
    <@insight.entry name="SuperColumn" value=operation.superColumn />
    <@insight.entry name="Column Name" value=operation.colName />
    <@insight.entry name="Start Column Name" value=operation.startColumn />

    <@insight.entry name="Keys" if=operation.keys?has_content>
        <@insight.list type="ordered" collection=operation.keys />
    </@insight.entry>

    <@insight.entry name="Columns" if=operation.columns?has_content>
        <@insight.list type="ordered" collection=operation.columns />
    </@insight.entry>

    <#if operation.range?has_content>
        <@insight.group label="Range" collection=operation.range?keys ; k>
            <@insight.entry name=k value=operation.range[k] />
        </@insight.group>
    </#if>

    <@insight.entry name="Rows Filter" if=operation.rowFilter?has_content>
        <@insight.list type="ordered" collection=operation.rowFilter />
    </@insight.entry>

    <@insight.entry name="Start Key" value=operation.startKey />
    <@insight.entry name="Count" value=operation.count />

    <@insight.entry name="Index Expressions" if=operation.indexExp?has_content>
        <@insight.list type="ordered" collection=operation.indexExp />
    </@insight.entry>

    <@insight.entry name="Consistency Level" value=operation.consistLevel />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


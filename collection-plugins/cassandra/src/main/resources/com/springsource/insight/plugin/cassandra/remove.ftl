<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Server" value=operation.server />   
    <@insight.entry name="Key" value=operation.key />
    <@insight.entry name="ColumnFamily" value=operation.columnFamily />
    <@insight.entry name="SuperColumn" value=operation.superColumn />
    <@insight.entry name="Column Name" value=operation.colName />
    <@insight.entry name="Timestamp" value=operation.timestamp />
    <@insight.entry name="Consistency Level" value=operation.consistLevel />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />


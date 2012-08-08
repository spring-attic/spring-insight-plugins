<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Job Name" value=operation.jobName />
    <@insight.entry name="Job Config File" value=operation.jobConfigFile />
</@insight.group>

<#if operation.config?has_content>
    <@insight.group label="Configuration" collection=operation.config?keys ; k>
        <@insight.entry name=k value=operation.config[k] />
    </@insight.group>
</#if>

<@insight.group label="Input">
    <@insight.entry name="Format Class" value=operation.inputFormat />
    <@insight.entry name="Data Path" if=operation.inputPath?has_content>
        <@insight.list type="ordered" collection=operation.inputPath />
    </@insight.entry>
</@insight.group>

<@insight.group label="Output">
    <@insight.entry name="Format Class" value=operation.outputFormat />
    <@insight.entry name="Data Path" value=operation.outputPath />
</@insight.group>

<@insight.group label="Mapper">
    <@insight.entry name="Class" value=operation.mapper />
    <@insight.entry name="Tasks number" value=operation.mapper_tasks />
    <@insight.entry name="Output Key Class" value=operation.mapperOutKey />
    <@insight.entry name="Output Value Class" value=operation.mapperOutValue />
</@insight.group>

<@insight.group label="Reducer">
    <@insight.entry name="Class" value=operation.reducer />
    <@insight.entry name="Tasks number" value=operation.reducer_tasks />
    <@insight.entry name="Output Key Class" value=operation.reducerOutKey />
    <@insight.entry name="Output Value Class" value=operation.reducerOutValue />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

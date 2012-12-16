<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Execute runnable">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Id" value=operation.runnerId />
    <@insight.entry name="Fork location" if=operation.spawnLocation??>
        <@insight.entry name="Class" value=operation.spawnLocation['className'] />
        <@insight.entry name="Method" value=operation.spawnLocation['methodName'] />
        <@insight.entry name="Line" value=operation.spawnLocation['lineNumber'] />
    </@insight.entry>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

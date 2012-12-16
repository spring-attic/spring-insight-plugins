<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Workspace" value=operation.workspace/>
    <@insight.entry name="Path" value=operation.path/>
    <@insight.entry name="Relative Path" value=operation.relPath/>
    <@insight.entry name="Absolute Path" value=operation.absPath/>
    <@insight.entry name="Name Pattern" value=operation.namePattern/>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>    

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

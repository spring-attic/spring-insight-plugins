<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Repository" value=operation.repository/>
    <@insight.entry name="Workspace" value=operation.workspace/>
    <@insight.entry name="Path" value=operation.path/>
    
    <@insight.entry name="Source Workspace" value=operation.srcWorkspace/>
    <@insight.entry name="Relative Path" value=operation.relPath/>
</@insight.group>    

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Destination Workspace" value=operation.workspace/>
    <@insight.entry name="Destination Absolute Path" value=operation.destAbsPath/>
    <@insight.entry name="Source Workspace" value=operation.srcWorkspace/>
    <@insight.entry name="Source Absolute Path" value=operation.srcAbsPath/>
    <@insight.entry name="Overwrite" value=operation.removeExisting/>
    <@insight.entry name="Save and Refresh" value=operation.keepChanges/>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>    

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

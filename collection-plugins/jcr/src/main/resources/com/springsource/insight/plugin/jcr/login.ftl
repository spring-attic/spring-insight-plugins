<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Workspace" value=operation.workspace/>
    
    <@insight.entry name="Username" value=operation.user/>
    <@insight.entry name="Password" value=operation.pass/>
</@insight.group>    

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Details">
    <@insight.entry name="Portlet Name" value=operation.name/>
    <@insight.entry name="Portlet Mode" value=operation.mode/>

    <@insight.entry name="Render Part" value=operation.renderPart/>
    <@insight.entry name="Render Phase" value=operation.renderPhase/>
    <@insight.entry name="ETag" value=operation.ETag/>

    <@insight.entry name="Window State" value=operation.winState/>
    <@insight.entry name="Window ID" value=operation.winId/>
</@insight.group>

<#if operation.params?has_content>
    <@insight.group label="Params" collection=operation.params?keys ; k>
        <@insight.entry name=k value=operation.params[k] />
    </@insight.group>
</#if>

<#if operation.preferences?has_content>
    <@insight.group label="Preferences" collection=operation.preferences?keys ; k>
        <@insight.entry name=k value=operation.preferences[k] />
    </@insight.group>
</#if>

<#if operation.exception??>
    <@insight.group label="Exception Details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

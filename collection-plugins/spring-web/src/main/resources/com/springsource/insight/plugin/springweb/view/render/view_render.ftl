<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="View Render">
    <@insight.entry name="View Type" value=operation.viewType />
    <@insight.entry name="Content Type" value=operation.contentType />
    <@insight.entry name="Model">
        <#if operation.model?has_content>
            <@insight.group collection=operation.model?keys ; key>
                <@insight.entry name=key value=operation.model[key] />
            </@insight.group>
        <#else>
            <em>empty</em>
        </#if>
    </@insight.entry>
</@insight.group>

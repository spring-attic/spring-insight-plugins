<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="View Resolver">
    <@insight.entry name="View Name" value=operation.viewName />
    <@insight.entry name="Locale" value=operation.locale />
    <@insight.entry name="Resolved View" value=operation.resolvedView required="true" />
    <@insight.entry name="Content-Type" value=operation.contentType required="true" />
</@insight.group>

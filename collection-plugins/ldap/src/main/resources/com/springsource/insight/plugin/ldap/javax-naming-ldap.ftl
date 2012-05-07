<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="LDAP query">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Connection URL" value=operation.connectionUrl />
    <@insight.entry name="Search Controls" if=operation.searchControls??>
        <@insight.group collection=operation.searchControls?keys ; key>
            <@insight.entry name=key value=operation.searchControls[key] />
        </@insight.group>
    </@insight.entry>
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

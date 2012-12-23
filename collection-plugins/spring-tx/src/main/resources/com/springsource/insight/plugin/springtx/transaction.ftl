<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Transaction">
    <@insight.entry name="Name" value=operation.name />
    <@insight.entry name="Propagation" value=operation.propagationAsString />
    <@insight.entry name="IsolationLevel" value=operation.isolationLevelAsString />
    <@insight.entry name="Read Only" value=operation.readOnly />
    <@insight.entry name="Timeout" value=operation.timeout />
    <@insight.entry name="Status" value=operation.status />
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

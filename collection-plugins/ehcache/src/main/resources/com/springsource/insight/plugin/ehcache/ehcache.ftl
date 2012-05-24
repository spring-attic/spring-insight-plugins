<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="EHCache Operation">
    <@insight.entry name="Operation Method" value=operation.method />
    <@insight.entry name="Key" value=operation.key />
    <#if operation.evalue??>
        <@insight.entry name="Value" value=operation.value />
    </#if>
    <@insight.entry name="Cache Name" value=operation.name />
</@insight.group>

<#if operation.CacheConfiguration?has_content>
    <@insight.group label="Attributes" collection=operation.CacheConfiguration?keys ; k>
        <@insight.entry name=k value=operation.CacheConfiguration[k] />
    </@insight.group>
</#if>

<#if operation.CacheManager?has_content>
    <@insight.group label="Actions" collection=operation.CacheManager?keys ; k>
        <@insight.entry name=k value=operation.CacheManager[k] />
    </@insight.group>
</#if>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

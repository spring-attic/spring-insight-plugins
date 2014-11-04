<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="EHCache Operation">
    <@insight.entry name="Operation Method" value=operation.method />
    <@insight.entry name="Key" value=operation.key />
    <#if operation.value??>
        <@insight.entry name="Value" value=operation.value />
    </#if>
    <@insight.entry name="Cache Name" value=operation.name />
</@insight.group>

<#if operation.CacheConfiguration?has_content>
    <@insight.group label="Cache Configuration" collection=operation.CacheConfiguration?keys ; k>
        <@insight.entry name=k value=operation.CacheConfiguration[k] />
    </@insight.group>
</#if>

<#if operation.CacheManager?has_content>
    <@insight.group label="CacheManager" collection=operation.CacheManager?keys ; k>
        <@insight.entry name=k value=operation.CacheManager[k] />
    </@insight.group>
</#if>

<#if operation.exception??>
    <@insight.group label="Exception details">
        <@insight.entry name="Exception" value=operation.exception/>
    </@insight.group>
</#if>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

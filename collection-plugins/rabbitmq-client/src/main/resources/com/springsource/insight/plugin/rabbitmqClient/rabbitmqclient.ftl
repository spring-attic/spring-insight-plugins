<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />


<@insight.group label="Connection">
	<@insight.entry name="Connection URL" value=operation.connectionUrl if=operation.connectionUrl?? />
	<@insight.entry name="Server Version" value=operation.serverVersion if=operation.serverVersion?? />
	<@insight.entry name="Client Version" value=operation.clientVersion if=operation.clientVersion?? />
</@insight.group>

<#if operation.label == "Publish">
	<@insight.group label="Publish Data">
	    <@insight.entry name="Exchange" value=operation.exchange if=operation.exchange?? />
	    <@insight.entry name="Routing Key" value=operation.routingKey if=operation.routingKey?? />
	    <@insight.entry name="Mandatory" value=operation.mandatory />
	    <@insight.entry name="Immediate" value=operation.immediate />
	    <@insight.entry name="Body Length" value=operation.bytes if=operation.bytes?? />
	</@insight.group>
<#else>
	<#if operation.envelope?? && operation.envelope?has_content>
		<@insight.group label="Envelope">
		    <@insight.entry name="Delivery Tag" value=operation.envelope.deliveryTag />
		    <@insight.entry name="Routing Key" value=operation.envelope.routingKey if=operation.envelope.routingKey?? />
		    <@insight.entry name="Body Length" value=operation.envelope.bytes if=operation.bytes?? />
		</@insight.group>
	</#if>	
</#if>

<#if operation.props??>
	<#if operation.props?has_content>
		<@insight.group label="Properties" collection=operation.props?keys ; key>
			<@insight.entry name=key value=operation.props[key] />
		</@insight.group>
	</#if>
	
	<#if operation.headers?? && operation.headers?has_content>
		<@insight.group label="Headers" collection=operation.headers?keys ; key>
			<@insight.entry name=key value=operation.headers[key] />
		</@insight.group>
	</#if>
</#if>
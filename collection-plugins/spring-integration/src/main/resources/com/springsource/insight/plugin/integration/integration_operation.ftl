<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<#--    
    This FreeMarker template contains the HTML used to render an Integration
    Operation. 
   
    The instance of the Operation is bound to the variable 'operation'.
    -->

<@insight.group label="">
    <@insight.entry name=operation.siComponentType value=operation.beanName />
    <@insight.entry name="Message Payload Type" value=operation.payloadType />
    <@insight.entry name="Message ID" value=operation.idHeader />
</@insight.group>

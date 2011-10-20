<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<#--    
    This FreeMarker template contains the HTML used to render an Operation 
    (such as MyOperation).
    
    The instance of the Operation is bound to the variable 'operation'.
    
    See:  org.myorg.insight.myplugin.MyOperation.java
    -->

<@insight.group label="Balance Set">
    <@insight.entry name="New Balance" value=operation.newBalance />
</@insight.group>

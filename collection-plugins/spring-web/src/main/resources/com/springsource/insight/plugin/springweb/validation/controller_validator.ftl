<#ftl strip_whitespace=true>
<#import "/insight-1.0.ftl" as insight />

<@insight.group label="Spring Web Validation">
    <@insight.entry name="Label" value=operation.label />
    <@insight.entry name="Errors count" value=operation.validationErrorsCount />
    <@insight.group label="Errors list" if=operation.validationErrorsList??>
    <table border='0' cellspacing='0' cellpadding='3'>
        <tr>
            <td><b>Object name</b></td>
            <td><b>Error text</b></td>
        </tr>
        <#list operation.validationErrorsList as errorData>
            <tr>
                <td>${errorData['name']}</td>
                <td>${errorData['value']}</td>
            </tr>
        </#list>
    </table>
    </@insight.group>
    <@insight.entry name="Exception" value=operation.exception if=operation.exception?? />
</@insight.group>

<@insight.sourceCodeLocation location=operation.sourceCodeLocation />

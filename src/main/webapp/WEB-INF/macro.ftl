<#macro ms_file jsonString>
    <#if jsonString?? && jsonString!='' && jsonString!='[]'>
        <@compress>
            ${jsonString?eval[0].url}
        </@compress>
    </#if>
</#macro>

<#macro ms_len text len>
    <#if text?length lt (len+1)?number>
        ${text}
    <#else>
        ${text[0..len?number]}...
    </#if>
</#macro>

<#macro ms_file_desc jsonString>
    <#if jsonString?? && jsonString!='' && jsonString!='[]'>
        <@compress>
            ${jsonString?eval[0].desc}
        </@compress>
    </#if>
</#macro>

<#macro ms_fmt_url url="">
    <@compress>
        <#if !(url??) || !url?has_content>
            <#return />
        </#if>
        <#assign normalized_url = url?trim?replace("\\", "/")?replace("^\\s+|(?<!:)/{2,}", "/", "r") />
        ${normalized_url}
    </@compress>
</#macro>
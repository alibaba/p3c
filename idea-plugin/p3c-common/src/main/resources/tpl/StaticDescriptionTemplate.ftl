<#ftl output_format="HTML">
<html>
<body>
<span style="color:orange">
    <pre>${message}<#if description??><br>${description}</#if></pre>
</span>
<#list examples as example>
<pre>${example}</pre>
</#list>
<!-- tooltip end -->
</body>
</html>

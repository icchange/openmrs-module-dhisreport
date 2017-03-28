<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>
<%@ include file="./resources/js/js_css.jsp"%>
<link type="text/css" rel="stylesheet"
	href="${pageContext.request.contextPath}/moduleResources/dhisreport/styles/sh_style.css" />
<script type="text/javascript"
	src="${pageContext.request.contextPath}/moduleResources/dhisreport/js/sh_main.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/moduleResources/dhisreport/js/sh_sql.js"></script>

<script type="text/javascript">
window.onload = sh_highlightDocument;
$(document).ready(function() {
 $("#editReportDefinitionCode").click( function() {
		document.getElementById("underlinedCode").innerHTML = "<input id=\"reportCode\" type=\"text\" value=\""+document.getElementById("underlinedCode").innerHTML+"\"/>";	
		$("#editReportDefinitionCode").hide();
		$("#saveReportDefinitionCode").show();
	});
 $("#saveReportDefinitionCode").click( function() {
 $.post("${pageContext.request.contextPath}/module/dhisreport/editReportCode.form",{reportCode: document.getElementById("reportCode").value,reportId: document.getElementById("reportDefinitionId").value});
 setTimeout(function(){
          window.location.reload();
      }, 1000);
 });
 
	});
</script>

<div>
	<b class="boxHeader"><spring:message
			code="dhisreport.editReportDefinition" />: ${reportDefinition.name}
		[<u id="underlinedCode">${reportDefinition.code}</u>]<a
		id="editReportDefinitionCode" style="cursor: pointer;"><img
			src="<c:url value='/images/edit.gif'/>" border="0"
			title='<spring:message code="dhisreport.editReportDefinition"/>' /></a><a
		id="saveReportDefinitionCode" style="display: none; cursor: pointer;"><spring:message code="dhisreport.dhis2saveButton" /></a><input
		id="reportDefinitionId" type="hidden" value="${reportDefinition.id}"></b>
	<div class="box">
		<table cellspacing="0" cellpadding="2">
			<tbody>
				<tr>
					<th></th>
					<th>[<spring:message code="dhisreport.dataElement" />]
					</th>

					<th>[<spring:message code="dhisreport.disaggregationName" />]
					</th>
					<!--  <th>[<spring:message code="dhisreport.disaggregationCode" />]</th>
                    -->
					<th>[<spring:message code="dhisreport.query" />]
					</th>
					<th>[<spring:message code="dhisreport.action" />]
					</th>
				</tr>
				<c:forEach var="dataValueTemplate" varStatus="varStatus"
					items="${reportDefinition.dataValueTemplates}">
					<tr class='${varStatus.index % 2 == 0 ? "oddRow" : "evenRow" }'>
						<td><input type="checkbox"
							value="dataValueTemplate${dataValueTemplate.id }"
							name="dataValueTemplateId"></td>
						<th>${dataValueTemplate.dataelement.name}</th>

						<td>${dataValueTemplate.disaggregation.name}</td>
						<!--
                        <td><b><u>${dataValueTemplate.disaggregation.code}</u></b></td>
                        -->
						<td><i><pre class="sh_sql"
									id="reportDefinition_query${dataValueTemplate.id }">${dataValueTemplate.query}</pre></i></td>

						<td><a
							onclick="REPORTDEFINITION.edit(${dataValueTemplate.id })"
							id="reportDefinition_edit${dataValueTemplate.id }"><spring:message
									code="dhisreport.Edit" /></a> <span
							id="reportDefinition_save${dataValueTemplate.id }"></span></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>

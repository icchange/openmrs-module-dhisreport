<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View Dhisreport" otherwise="/login.htm" redirect="/module/dhisreport/listDhis2Reports.form" />

<%@ include file="template/localHeader.jsp"%>
<%@ include file="./resources/js/js_css.jsp" %>


<script type="text/javascript">
$(document).ready(function(){

	$("tr").each(function (index) { // traverse through all the rows
		
        if(index != 0) { // if the row is not the heading one
            $(this).find("td:first").html(index + "."); // set the index number in the first 'td' of the row

        }
    }); 
	
	$(".new").css({
		
		'background-color':'#FE2E2E',
		  'color':'#FAFAFA',

	})
	
	$(".old").css({
	
		'background-color':'',
		  'color':'',
	})
	
	
});
</script>


<h3><spring:message code="dhisreport.reportDefinitions" /></h3>
<a href="http://dev.kmri.co.ke:60055/dhis-web-dashboard-integration/index.action" target="_blank">go to DHIS2</a>

<c:forTokens var="type" items="Daily,Weekly,Monthly" delims=",">
	<c:set var="count" value="0"/>
	<h3>${type}</h3>
	<table>
    	<c:forEach var="reportDefinition" items="${reportDefinitions}">
    		<c:set var="flag" value ="true"/>
    		<c:set var="periodType" value="${reportDefinition.periodType}"/>
			<c:if test="${periodType eq type}">
				<c:set var="count" value="${count + 1}"/>
    			<c:forEach var="dataValueTemplate" items="${reportDefinition.dataValueTemplates}">
    				<c:if test='${flag == "true"}'>
    					<c:if test="${empty dataValueTemplate.query}">
    			  	 		<c:set var="flag" value ="false"/>
    					</c:if>
    				</c:if>
    			</c:forEach>
    
    			<c:choose>
      				<c:when test='${flag == "false"}'>
        				<tr class="new">
           					<td id ="serial"></td>
           					<td ><a  href="editReportDefinition.form?reportDefinition_id=${reportDefinition.id}" style="color:white" >${reportDefinition.name}</a> </td>
           					<td ><a href="setupReport.form?reportDefinition_id=${reportDefinition.id}" style="color:white"><spring:message code="dhisreport.Export" /></a> </td>
           					<td><a onclick="mapReports.form?reportDefinition_id=${reportDefinition.id}" style="color:white"><spring:message code="dhisreport.MapReport" /></a> </td>
           					<td><a href="scheduler.form?reportDefinition_id=${reportDefinition.id}">schedule</a></td>
           					<td><a onclick="REPORTDEFINITION.deleteReportDefinition(${reportDefinition.id})" style="color:white"><spring:message code="dhisreport.Delete" /></a> </td>
        					<td>
                            	<c:if test="${reportDefinition.scheduled}">
                            		scheduled
                               	</c:if>
                            </td>
        				</tr>
      				</c:when>

      				<c:otherwise>
         				<tr class="old">
        					<td id ="serial"></td>
            				<td><a href="editReportDefinition.form?reportDefinition_id=${reportDefinition.id}">${reportDefinition.name}</a> </td>
         					<td><a href="setupReport.form?reportDefinition_id=${reportDefinition.id}"><spring:message code="dhisreport.Export" /></a> </td>
             				<td><a href="mapReports.form?reportDefinition_id=${reportDefinition.id}"><spring:message code="dhisreport.MapReport" /></a> </td>
             				<td><a href="scheduler.form?reportDefinition_id=${reportDefinition.id}">schedule</a></td>
             				<td><a onclick="REPORTDEFINITION.deleteReportDefinition(${reportDefinition.id})"><spring:message code="dhisreport.Delete" /></a> </td>
             				<td>
             					<c:if test="${reportDefinition.scheduled}">
									scheduled
                            	</c:if>
                            </td>
        				</tr>
  	  				</c:otherwise>
    
				</c:choose>
			</c:if>
    	</c:forEach>
    </table>
    <!-- fn:length(reportDefinitions) -->
    <c:if test="${ count == 0 }">
        <spring:message code="general.none" />
    </c:if>
    <c:if test="${ count gt 1}">
    	<a href="setupBulkReport.form?type=${type}" >${type} bulk data transfer</a>
    </c:if>
    <br/>
</c:forTokens>

<h3>Tracker Capture</h3>

<c:set var="counttracker" value="0"/>
<table>
    <c:forEach var="TrackerCaptureTemplate" items="${TrackerCaptureTemplates}">
    	<c:set var="counttracker" value="${counttracker + 1}"/>
    	<tr>
    		<td id ="serial"></td>
    		<td>${TrackerCaptureTemplate.name}</td>
    		<td><a href="setupTrackerCapture.form?trackerCapture_id=${TrackerCaptureTemplate.id}"><spring:message code="dhisreport.Export" /></a></td>
    		<td><a onclick="TRACKERCAPTURE.deleteTrackerCapture(${TrackerCaptureTemplate.id})"><spring:message code="dhisreport.Delete" /></a></td>
    	</tr>
    </c:forEach>
</table>
<c:if test="${ counttracker == 0 }">
    <spring:message code="general.none" />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>

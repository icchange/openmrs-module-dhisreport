<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<h3><spring:message code="dhisreport.reportResult" /></h3>
<c:forEach var="aggregatedValues" items="${aggregatedList}">
<c:if test="${not empty aggregatedValues.dataValues}">
<h3>------------------------------------------------------------------------</h3>

     <br>
<div>
	<table style="width:600px">
	<tr>
	<th>TrackerCapture Id</th>
	<th>Value</th>
	</tr>
	<tr>
    <c:forEach var="dv" items="${aggregatedValues.dataValues}">
    <tr>
  		<td>${dv.id}</td>
  		<td>${dv.description}</td>
  	</tr>
	<tr>
  	</c:forEach>
	</table>
	</div>
</c:if>
</c:forEach>
<a href="http://dev.kmri.co.ke:60055/dhis-web-dashboard-integration/index.action" target="_blank">go to DHIS2</a>
<br/>
<%@ include file="/WEB-INF/template/footer.jsp"%>
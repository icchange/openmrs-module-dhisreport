<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<openmrs:require privilege="Manage Dhisreport" otherwise="/login.htm" redirect="/module/dhisreport/listDhis2Reports.form" />

<table>
	<tr>
    	<th>Name</th>
    	<th>Id</th>
    	<th></th>
    </tr>
    <c:forEach var="tb" items="${tableList}">
    <tr>
  		<td>${tb.name}</td>
  		<td>${tb.uid}</td>
  		<td><a href="${pageContext.request.contextPath}/module/dhisreport/tablePermissions.form?id=${tb.uid}">Permissions</a></td>
  	</tr>
	<tr>
  	</c:forEach>
</table>



<%@ include file="/WEB-INF/template/footer.jsp"%>
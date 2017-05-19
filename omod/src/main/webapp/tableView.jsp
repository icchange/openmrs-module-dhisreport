<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

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
  		<td><a href="${pageContext.request.contextPath}/module/dhisreport/tableEmbed.form?id=${tb.uid}">View</a></td>
  	</tr>
	<tr>
  	</c:forEach>
</table>



<%@ include file="/WEB-INF/template/footer.jsp"%>
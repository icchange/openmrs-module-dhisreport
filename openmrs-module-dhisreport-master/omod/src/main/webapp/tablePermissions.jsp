<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage Dhisreport" otherwise="/login.htm" redirect="/module/dhisreport/listDhis2Reports.form" />

<%@ include file="template/localHeader.jsp"%>

<script>
function moveLeft(){
	var deAvailable = document.getElementById("deAvailable");
	var deSelected = document.getElementById("deSelected");
    for (var x=deAvailable.length-1;x >=0;x--){
        if (deAvailable[x].selected){
			deSelected.appendChild(deAvailable[x]);
        }
    }
}

function moveRight(){
	var deAvailable = document.getElementById("deAvailable");
	var deSelected = document.getElementById("deSelected");
    for (var x = deSelected.length-1;x>=0;x--){
        if (deSelected[x].selected){
			deAvailable.appendChild(deSelected[x]);
        }
    }
}

function moveAllLeft(){
	var deAvailable = document.getElementById("deAvailable");
	var deSelected = document.getElementById("deSelected");
    while (deAvailable.length > 0){
		deSelected.appendChild(deAvailable[0]);
    }
}

function moveAllRight(){
		var deAvailable = document.getElementById("deAvailable");
    	var deSelected = document.getElementById("deSelected");
        while (deSelected.length > 0){
    		deAvailable.appendChild(deSelected[0]);
        }

}
</script>
<h3>Permissions</h3>

<table>
    <tr>
  		<td>${table.name}</td>
  		<td>${table.uid}</td>
	</tr>
</table>


<form action="${pageContext.request.contextPath}/module/dhisreport/tablePermissions.form?id=${table.uid}" method="post">

<table>
<tr>
<td>
<select id="deAvailable" name="deAvailable" multiple="multiple" style="height: 200px; width: 200px;">
<c:forEach var="role" items="${roleList}">
	<option value="${role.role}">${role.role}</option>
</c:forEach>
</select>
</td>
<td>
	<table>
	<tr><td>
		<button type="button" onclick="moveLeft()">&gt;</button>
	</td></tr>
	<tr><td>
		<button type="button" onclick="moveRight()">&lt;</button>
	</td></tr>
    <tr><td>
    	<button type="button" onclick="moveAllLeft()">&gt;&gt;</button>
    </td></tr>
    <tr><td>
    	<button type="button" onclick="moveAllRight()">&lt;&lt;</button>
    </td></tr>
    </table>
</td>
<td>
<select id="deSelected" name="deSelected" multiple="multiple" style="height: 200px; width: 200px;">
<c:forEach var="role" items="${roleTable}">
	<option value="${role.role}">${role.role}</option>
</c:forEach>
</select>
</td>
</tr>
</table>

<input type="submit" value="Submit"></input>
</from>


<%@ include file="/WEB-INF/template/footer.jsp"%>
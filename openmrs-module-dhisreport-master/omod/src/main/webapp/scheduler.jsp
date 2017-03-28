<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>
<%@ include file="./resources/js/js_css.jsp" %>

<h3><spring:message code="dhisreport.reportDefinitionFor" /> ${reportDefinition.name}</h3>

<form action="scheduler.form" method="post">
	<table>
        <tr>
            <td>Scheduler Pattern String:</td>
            <td>
            	Yes<input type="radio" name="schedule" value="true" <c:if test="${reportDefinition.scheduled}">checked</c:if>/>
            	No<input type="radio" name="schedule" value="false" <c:if test="${not reportDefinition.scheduled}">checked</c:if> />
            </td>
        </tr>
        <tr>
            <td />
            <td>
                <input type="submit" value="set" />
            </td>
        </tr>

    </table>
    <input type="hidden" name="reportDefinition_id" value="${reportDefinition.id}" />

</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
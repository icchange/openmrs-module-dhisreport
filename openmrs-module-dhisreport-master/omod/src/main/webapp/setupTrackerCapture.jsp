<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>
<%@ include file="./resources/js/js_css.jsp" %>

<h3><spring:message code="dhisreport.reportDefinitionFor" /> ${reportDefinition.name}</h3>

<form action="executeTrackerCapture.form" method="post">
    <table>
        <tr>
            <td /><td><select name="resultDestination">
                    <option value="preview"><spring:message code="dhisreport.Preview" /></option>
                    <c:if test="${not empty dhis2Server}">
                        <option value="post"><spring:message code="dhisreport.postToDHIS" /></option>
                    </c:if>
                </select>
            </td>
        </tr>
        <tr>
            <td />
            <td>
                <input type="submit" value="<spring:message code="dhisreport.Generate" />" />
            </td>
        </tr>

    </table>
    <input type="hidden" name="trackerCapture_id" value="${trackerCapture.id}" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
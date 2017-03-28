<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form action="loadReportDefinitions.form" method="post" enctype="multipart/form-data">
    <div>
        <b class="boxHeader"><spring:message code="dhisreport.uploadReport" /></b>
        <div class="box">
            <span style="white-space: nowrap">
                <span><spring:message code="dhisreport.selectReport" />:</span>
                <input type="file" name="datafile" size="40" accept="application/xml" style="border: 1px solid cadetblue; padding: 1px;"/>
            </span>


            <div>
                <input type="submit" name="import" value="<spring:message code="dhisreport.UploadButton" />">
            </div>
        </div>

    </div>
</form>

<form action="exportReportDefinitions.form">
    <div>
        <b class="boxHeader"><spring:message code="dhisreport.exportReport" /></b>
        <div class="box">
            <span style="white-space: nowrap">
                <span><spring:message code="dhisreport.exportAs" /> xml: </span>
                <input type="submit" name="export" value="Export"/>
            </span>
        </div>
    </div>
</form>

<form action="getReportDefinitions.form" method="post">
	    <div>
        <b class="boxHeader"><spring:message code="dhisreport.importReportDefinition" /></b>
        <div class="box">
            <span style="white-space: nowrap">
                <span><spring:message code="dhisreport.importReportDefinition" /> as XML:</span>
                <td><input value="GET REPORT DEFINITIONS" name="submit" type="submit"></td>
            </span>
        </div>
    </div>
</form>

<br/>

<form action="loadReportDefinitions.form" method="post" enctype="multipart/form-data">
    <div>
        <b class="boxHeader"><spring:message code="dhisreport.uploadTrackerCapture" /></b>
        <div class="box">
            <span style="white-space: nowrap">
                <span><spring:message code="dhisreport.selectReport" />:</span>
                <input type="file" name="trackerfile" size="40" accept="application/xml" style="border: 1px solid cadetblue; padding: 1px;"/>
            </span>


            <div>
                <input type="submit" name="import" value="<spring:message code="dhisreport.UploadButton" />">
            </div>
        </div>

    </div>
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<link rel="stylesheet" type="text/css" href="http://dhis2-cdn.org/v221/ext/resources/css/ext-plugin-gray.css" />
<script src="https://dhis2-cdn.org/v221/ext/ext-all.js"></script>
<script src="https://dhis2-cdn.org/v221/plugin/table.js"></script>
<script src="https://code.jquery.com/jquery-2.1.4.min.js";></script>

<table>
    <tr>
  		<td>${table.name}</td>
  		<td>${table.uid}</td>
	</tr>
</table>

<div id="table1"></div>

<script>
//var base = "http://dev.kmri.co.ke:60055/";
var url = window.location.href;
var substring = url.split("openmrs");
var base = substring[0]+"openmrs";
base+="/module/dhisreport/dhis2tablegetter/";
console.log(base);

/*
Ext.onReady(function() {
	Ext.data.JsonP.request({
	 url: base + "dhis-web-commons/security/login.action",
	 callbackKey: 'jsonp_callback',
	 params: { "j_username": "admin", "j_password": "district" },
	 callback: function(data) {
	 	console.log(data.results);
	 	var msg = data.results;
	 	var html = tpl.apply(msg);
	 	resultPanel.update(html);
	 	console.log('SUCCESS');
	 }
	});

      Ext.Ajax.request({
        crossDomain: true,
        url: base + "dhis-web-commons/security/login.action",
        method: "POST",
        cors: true,
        useDefaultXhrHeader : false,
        param: { "j_username": "admin", "j_password": "district" },
        success: setLinks,
		failure: function () {
			alert('failure');
		}
      });

});
*/
Ext.onReady(function() {
	DHIS.getTable({ url: base, el: "table1",  id: "${table.uid}" });
});
 /*
console.log("test");
$( document ).ready(function() {
  console.log("jquery");


  $.ajax({
    crossDomain: true,
    url: base + "dhis-web-commons-security/login.action?authOnly=true",
    method: 'POST',
	params: {"j_username": "admin", "j_password": "district" },
    success: setLinks,
    failure: function () {
           			alert('failure');
    }
  });

});
  */
function setLinks() {
	console.log("success");
	DHIS.getTable({ url: base, el: "table1",  id: "${table.uid}" });
}

</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js"  djConfig="isDebug: true,parseOnLoad: true"></script>
<!-- <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dijit/themes/claro/claro.css"/>  -->
<link rel="stylesheet" type="text/css" href="js/dojo/themes/squid/squid.css"/> 
<style type="text/css">
    @import "http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojox/grid/resources/Grid.css";
    @import "http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojox/grid/resources/claroGrid.css";
    .dojoxGrid table { margin: 0; } html, body { width: 100%; height: 100%;
    margin: 0; }
</style>
<title>Browse Object Types</title>
</head>
<script>
var		datSrcUrl;
var		datStore;


dojo.require("dojox.grid.DataGrid");
dojo.require("dojox.data.QueryReadStore");

dojo.addOnLoad(function ()
{
	datSrcUrl = "ObjTypeManager";
	
	datStore = new dojox.data.QueryReadStore({
		url: 			datSrcUrl,
		requestMethod:	"post",
		query:			{action: "listjson"}
	});
	

	//	Setup the grid
	
	objGrid.setStore(datStore);
	
});
</script>
<body  class=" claro ">
<!--<div dojoType="dojox.data.QueryReadStore" url="ObjTypeManager" jsId="srcObjTypes" method="post" ></div>-->
<table dojoType="dojox.grid.DataGrid" pageSize="30"  query="{ action: 'listjson' }" jsId="objGrid">
  <thead>
    <tr>
      <th field="objid" width="50px">ID</th>
      <th field="objname" width="200px">Name</th>
      <th field="objtilesrc" width="50px">Tileset</th>
      <th field="description" width="400px">Description</th>
    </tr>
  </thead>
</table>
</body>
</html>
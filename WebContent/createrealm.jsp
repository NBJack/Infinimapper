<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.util.*,org.rpl.infinimapper.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<?xml version="1.0" encoding="iso-8859-1"?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
  <title>Realm Management: Create Realm</title>
  <meta name="generator" content="Amaya, see http://www.w3.org/Amaya/" />
  <link href="default.css" rel="stylesheet" type="text/css" />
</head>

<body style="text-align:center;margin-left:auto;margin-right:auto;">
<h1>Create Map</h1>

<form name="tileinfo" method="post" action="RealmManager">

  <table style="width:600px;margin-left:auto;margin-right:auto;" border="0">
    <col />
    <tbody>
      <tr>
        <td style="width:200px;" class="cellhead">Name </td>
        <td style="width:500px;" class="cellbody"><input type="text" name="title"
          style="width:200px" /></td>
      </tr>
      <tr>
        <td class="cellhead">Description </td>
        <td class="cellbody"><textarea rows="5" cols="40" name="desc"></textarea></td>
      </tr>
      <tr>
        <td class="cellhead">Author </td>
        <td class="cellbody"><%= session.getAttribute("username") %> </td>
      </tr>
      <tr>
        <td class="cellhead">Tile Set </td>
        <td class="cellbody">
        	<select name="tileset">
        		<%
        		Collection<String[]>	tilesets;
        		
        		
        		
        		//	Grab the tilesets en-masse (will not always be able to do this!)
        		
        		tilesets = WorldDB.getAllTilesets();
        		
        		for ( String [] s : tilesets )
        		{
        		%> <OPTION value="<%=s[1]%>"><%=s[0]%></OPTION> <%
        		}
        		
        		%>        		
        	</select>
        </td>
      </tr>
      <tr>
        <td class="cellhead">Default Tile </td>
        <td class="cellbody"><input type="textbox" name="defaulttile" value="0" /></td>
      </tr>
      <tr>
        <td class="cellhead">Number of Layers</td>
        <td class="cellbody"><select name="layerCount">
        <OPTION value="1">1</OPTION>
        <OPTION value="2">2</OPTION>
        <OPTION value="3">3</OPTION>
        <OPTION value="4">4</OPTION></select>
        </td>
      </tr>
      <tr>
        <td class="cellhead">Public </td>
        <td class="cellbody"><input type="checkbox" name="publicflag" checked="checked" value="true" /></td>
      </tr>
      <tr>
        <td colspan="2">
	  <p></p>
          <input type="submit" value="Create Map" /> </td>
      </tr>
    </tbody>
  </table>
</form>
</body>
</html>

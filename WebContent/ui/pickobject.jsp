<%@page import="org.rpl.infinimapper.DBResourceManager,java.sql.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Object Selector</title>
<link href="corestyle.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div style=""><a href="#" onclick="parent.document.getElementById('objectselect').style.display='none';">X</a></div>
<span style="color:white;">Select an object:</span><br>
<select style="width:200px;" id="selectObject">
<%

Connection		c;
ResultSet		rs;
Statement		st;


c = null;
st = null;
rs = null;

try {
	
//	Open a connection to the database

c = DBResourceManager.getConnection();

//	Get a list of all object types.

st = c.createStatement();

rs = st.executeQuery("SELECT id, name, description FROM objlib WHERE id > 0");

//	Display them to the user.

while ( rs.next() )
{
%>
	<option value="<%= rs.getInt(1)%>"><%= rs.getString(2) %></option>
<%
}
} catch ( Exception ex )
{
	//	Report something to the user
	
	ex.printStackTrace();
	
} finally 
{
	//	Clean up resources
	
	if ( rs != null )
	{
		try { rs.close(); } catch ( Exception ex ) {};
	}
	
	if ( c != null )
	{
		try { c.close(); } catch ( Exception ex ) {};
	}


}

%>
</select>
<input type="button" value="Use" onclick="parent.changeObjectToPaint(document.getElementById('selectObject').value);"><br>
<input type="checkbox"  checked="true" value="true" id="enableSnap" onclick="parent.objSnapToGrid=document.getElementById('enableSnap').checked;"><span style="color:white;">Snap to grid</span>
</body>
</html>
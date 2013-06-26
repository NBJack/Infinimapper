<%@page import="org.rpl.infinimapper.DBResourceManager,java.sql.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Realm Selector</title>
<link href="corestyle.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div style=""><a href="#" onclick="parent.document.getElementById('realmselect').style.display='none';">X</a></div>
<span style="color:white;">Please select a realm to switch to:</span><br>
<select style="width:200px;" id="selectRealm">
<%

Connection			c;
ResultSet			rs;
PreparedStatement	st;
Integer				userid;



userid = (Integer) session.getAttribute("userid");

c = null;
st = null;
rs = null;

try {
	
//	Open a connection to the database

c = DBResourceManager.getConnection();

//	Get a list of all realms.

if ( userid == null  )
{
	st = c.prepareStatement("SELECT id, name, description FROM realms WHERE public=True AND sublayer=False");

	rs = st.executeQuery();
} else {
	
	//	Customize to the user
	
	st = c.prepareStatement("SELECT id, name, description FROM realms WHERE public=True OR ownerid=? AND sublayer=False");
	
	st.setInt(1, userid);
	
	rs = st.executeQuery();

}
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
<input type="button" value="Go" onclick="parent.changeRealm(document.getElementById('selectRealm').value, true);"><br>
<a href="#" onclick="parent.window.location='../createrealm.jsp'">Create Realm</a> | <a href="#" onclick="parent.window.location='../uploadtiles.html'">Upload Images</a>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" href="default.css" type="text/css" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Logout</title>
</head>
<body>
<%

if ( session.getAttribute("username") != null )
{
%><h2>User was logged out.</h2><br><%
	//	Just invalidate the session
	
	session.invalidate();
	
} else {
%><h2>No logout was necessary.</h2><%	
	//	Nothing to do
}
%>

</body>
</html>
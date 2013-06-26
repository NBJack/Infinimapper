<%@ page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%

Connection		c;
Statement		st;
ResultSet		rs;


c = DriverManager.getConnection("jdbc:mysql://localhost/jacobsdefense?autoReconnectForPools=true&autoReconnect=true", "jacob", "Dolphins89Pindrop");
st = null;

try {
	
	st = c.createStatement();
	st.execute("select count(*) FROM tilelib");
} catch ( Exception ex )
{
	ex.printStackTrace();
} finally {

	if ( st != null )
		st.close();
}


c.close();

%>
If you see this, the database was successfully contacted.
</body>
</html>
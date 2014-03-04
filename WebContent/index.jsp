
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.sql.*,javax.naming.*,javax.sql.DataSource,java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Infinimapper Gateway</title>
    <link rel="stylesheet" href="corestyle.css" type="text/css" />
    <script type="text/javascript" src="js/objects.js"></script>
</head>
<body>
    <h1>What would you like to do?</h1>
    <div class="mapChoice"><a href="TiledCanvas.jsp">Just have fun in the Sandbox</a></div>
    <div class="mapChoice"><a href="MakeMap">Start from a Template</a></div>
    <div class="mapChoice"><a href="#">Make my Own from Scratch</a></div>

</body>
</html>
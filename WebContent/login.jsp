<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
<link href="corestyle.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="default.css" type="text/css" />
<script type="text/javascript" src="js/md5.js"></script>
</head>

<script type="text/javascript">

//
// A simple local salt to help hide creds in transit. Just keeps things
// safe from basic snooping.
//
var localsalt = "CaptainUpboat";


//
// Our simple password checking function
//
function checkpass()
{


	// Set hash

	document.forms["creds"].phash.value = hex_md5(localsalt + document.forms["creds"].password.value);

	// Clear password

	document.forms["creds"].password.value = "";
	
	// Submit for authentication
	
	document.forms["creds"].submit();
}

</script>
<body>
<h1>Login</h1>
<form action="LoginMan" method="POST" name="creds">
<p>User: <input name="username" id="user"></input></p>
<p>Pass: <input type="password" name="password" id="password"></input></p>
<input type='hidden' name="phash"></input>
<button  onclick="checkpass();">Login</button> 
</form>
Don't have login credentials?<a href="register.jsp">Register</a> here!
</body>
</html>
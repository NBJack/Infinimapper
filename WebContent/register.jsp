<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link href="corestyle.css" rel="stylesheet" type="text/css" />
<link href="default.css" rel="stylesheet" type="text/css" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js"></script>
<script type="text/javascript" src="js/md5.js"></script>
<script>

//
// A simple local salt to help hide creds in transit. Just keeps things
// safe from basic snooping.
//
var localsalt = "CaptainUpboat";

dojo.require("dojo.io.iframe");

dojo.addOnLoad ( function () {
	
	//	Connect the submit button to our addition function
	
	//dojo.connect( dojo.byId("registerButton"), "onclick", null, requestUserAdd);

	
});


function requestUserAdd ()
{
	var		finalHash;
	var		msgOut;
	
	
	
	
	//	Note the object we post our result message to
	
	msgOut = dojo.byId("msgOut");
	
	//	First, verify we have matching passwords
	
	if ( dojo.byId("passA").value != dojo.byId("passB").value )
	{
		//	Alert
		
		msgOut.innerHTML = "Passwords don't match!";
		
		return;
	}
	
	//	Create our password hash for submission
	
	finalHash = hex_md5(localsalt + dojo.byId("passA").value);
	
	dojo.byId("phash").value = finalHash;
	
	// Clear out old passwords
	
	dojo.byId("passA").value = "";
	dojo.byId("passB").value = "";
	
	//	Submit via iframe
	
	dojo.io.iframe.send({
		
		url : "RegisterMan",
		handleAs : "json",
		form : "userdata",
		
		load : function (response, ioArgs)
		{
			if ( response.success )
			{
				//	Redirect to a new page
				
				alert("Success.");
				
			} else {
				
				//	Report the problem
				
				msgOut.innerHTML = response.message;
			}
		},
		
		error : function (response, ioArgs)
		{
			alert("There was an error while processing the user ID.");
		}
		
		
	});
}


</script>
<title>New User Registration</title>
</head>
<body>
<h1>User Registration</h1>
<form name="userdata" id="userdata">
<input type="hidden" id="phash" name="phash">
<table><tbody>
<tr>
<td>User Name:</td>
<td><input name="username" id="username"></td>
</tr>
<tr>
<td>Password:</td>
<td><input  id="passA" name="passA" type="password"></td>
</tr>
<tr>
<td>Confirm Password:</td>
<td><input id="passB" name="passB" type="password"></td>
</tr>
<tr>
<td>E-mail:</td>
<td><input id="email" name="email"></td>
</tr>
<tr><td colspan="2"><button id="registerButton" onclick="requestUserAdd(); return false;">Register</button></td></tr>
</tbody></table>

</form>
<span id="msgOut"></span>
</body>
</html>
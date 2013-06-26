
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.sql.*,javax.naming.*,javax.sql.DataSource,java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<script type="text/javascript" src="js/objects.js"></script>
<script type="text/javascript">

var 	ajaxPipe;		// Our AJAX xml object
var 	requestInProgress;
var 	requestStack;	// Our request stack for raw messages to the server



requestStack = new Array();
requestInProgress = false;


//
// Initialize the AJAX communication pipeline
//

// FROM: http://www.xul.fr/en-xml-ajax.html

if (window.XMLHttpRequest)     // Object of the current windows
{ 	
    ajaxPipe = new XMLHttpRequest();     // Firefox, Safari, ...
} 
else if (window.ActiveXObject)   // ActiveX version
{
   ajaxPipe = new ActiveXObject("Microsoft.XMLHTTP");  // Internet Explorer 
}




//
// Sends a message to the database handler. If we already have another
// request in progress, queue it up.
//
function sendMessage ( data )
{
	if ( requestInProgress )
	{
		// Queue

		requestStack.push(data);

		console.log("Queued: " + data + " Length: " + requestStack.length);

		return false;
	}

	// Begin our request

	requestInProgress = true;

	console.log("Sent: " + data);

	ajaxPipe.onreadystatechange = handleDataResponse;
	ajaxPipe.open('POST', "WorldDB", true);
	ajaxPipe.send(data + '\n');

	return true;
}


function postDataToChunk ()
{
}


function handleDataResponse ()
{
	var responseArr;
	var i;
	var metaResponseArr;



	if ( this.readyState == 4 && this.status == 200 )
	{
		// Great; things went well.

		//alert(this.responseText);

		// Do we need to do anything?
		
		console.log(this.responseText);

		metaResponseArr = this.responseText.split("\n");

		for ( i = 0; i < metaResponseArr.length; i++ )
		{

		// Grab our next response

		responseArr = metaResponseArr[i].split("!!!");

		// Figure out how to act

		if ( responseArr[0] == "OK" )
		{
			// What does the sequnce say?

			
		} else if ( responseArr[0] == "CHUNK" )
		{
			// Awesome; this is a new chunk.

			postDataToChunk(responseArr[1], responseArr[2], responseArr[3].split(","), responseArr[4].split(","), responseArr[5]);
		} else if ( responseArr[0] == "BLANK" )
		{
			postDataToChunk(responseArr[1], null, null, null, 0);
		} else if (responseArr[0] == "REFRESHAVAIL") {
			postDataToChunk(responseArr[1], responseArr[2], responseArr[3].split(","), responseArr[4].split(","), responseArr[5], responseArr[6]);
		} else if ( responseArr[0] == "OBJLIB") {
			
			//	Parse the objects, using the existing string.
			
			decodeObjLibResponse(responseArr);
			
		} else {
			console.log("Ignored response: '" + responseArr[0] + "'");
		}

		}
			

		// Clear the request flag

		requestInProgress = false;

		// Try to empty the queue

		sendNextQueuedItem();

	} else {
		
		try {
			if ( this.status == null )
				return;

			if ( this.status == 200 )
			{
				// Ignore
			}  else {

				// Note and clear the flag.

				console.log("Problem during AJAX communication: " + this.status);

				requestInProgress = false;
			}
		} catch (err) {
			//console.log("Error on ajax response: " + err + " from " + this);
		}
	}

	
}


//
// Try to send a queued item if available. If the send wasn't successful,
// put it back on the queue and hope we don't run out of space for requests.
// When possible, tries to piggyback multiple requests in one message via
// newline.
//
function sendNextQueuedItem ()
{
	var itemData;
	var finalMsg;
	var reqCount;





	// Stop if we have nothing to send

	if ( requestStack.length == 0 )
		return;

	// Get a few more items off the stack if we can

	reqCount = 0;
	finalMsg = "";
		
	itemData = requestStack.shift();

	while ( itemData != null && reqCount < 3 )
	{
		finalMsg = finalMsg + itemData + "\n";

		itemData = requestStack.shift();		

		reqCount++;
	}

	// Get the last request

	if ( itemData != null )
		finalMsg = finalMsg + itemData + "\n";

	// Try to send immediately. If it didn't work, the message automatically ends
	// back up in the queue. Note this may lead to interesting scenarios involving
	// 'stacked' messages.

	//alert(finalMsg);

	sendMessage(finalMsg);
}



</script>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Infinimapper Gateway</title>
</head>
<body>
<h1>Coming Soon</h1>
<form method="POST" action="WorldDB">

</form>

</body>
</html>
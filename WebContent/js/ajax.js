/********************************************************************************

Infinimapper - Browser-based tiled map editing tool
Copyright (C) 2011 Ryan Layfield

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

http://www.ryanlayfield.com/

**********************************************************************************/

/******************************************
 * AJAX Communications Library
 * 
 * This manages the pipe, queue, and request status of the
 * communications pipeline to the server.  In order for it
 * to function properly, a callback function named
 * 'handleDataResponse' MUST exist elsewhere. This will likely be 
 * changed for a more flexible system in the future as long
 * as there is no discernable performance hit.
 * 
 * 
 */

	var ajaxPipe;			// Our AJAX xml object
	var requestInProgress;  // Do we have a request in progress?
	var requestStack;		// Our request stack for raw messages to the server
	


	//
	// Global variable setup
	//
	

	//
	// Initialize the AJAX communication pipeline
	//
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

	// End FROM
	

	// Tracks concurrent requests and forms our queue

	requestInProgress = false;
	requestStack = new Array();
	
	
	
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

			debugLog("Queued: " + data + " Length: " + requestStack.length);

			return false;
		}

		// Begin our request

		requestInProgress = true;

		debugLog("Sent: " + data);
		
		ajaxPipe.onreadystatechange = handleDataResponse;
		ajaxPipe.open('POST', "WorldDB", true);
		ajaxPipe.send(data);

		return true;
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

		while ( itemData != null && reqCount < 7 )
		{
			//	Line feeds indicate a new message
			
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

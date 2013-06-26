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


/*******************************
 * 
 * Map Chunk Management Library
 * 
 * To allow for infinite editing, we divide the map into fixed-size
 * chunks. This library provides all of the basic operations to 
 * manipulate, encoded, and decode of a chunk object, as well as
 * construction of new ones.
 * 
 */

	var chunks;								// A map of chunk objects, keyed by address name
	var chunkRealWidth, chunkRealHeight;	// The real pixel dimensions of each chunk
	var chunkWidth, chunkHeight;			// The width and height in tiles of each chunk
	var tileScale;							// The scale of each tile
	var updateQueue;						// A named list of chunks to send
	var updatesPending;						// Updates that need to go out

	
	

	chunks = {};
	
	
	//	The scale of each tile we're working with (in pixels)
	
	//tileScale = 32;
	
	//	How big each chunk of information is, in tiles
	
	chunkWidth = 40;
	chunkHeight = 40;

	//	How big each chunk of information is, in pixels

	chunkRealWidth = tileScale * chunkWidth;
	chunkRealHeight = tileScale * chunkHeight;

	// Chunk update tracker

	updateQueue = new Array();
	updatesPending = false;

	
	
	
	//
	// Setup a new chunk object for the specified coordinates
	// and returns it. Note this requires several global vars
	// to already have been setup.
	//

	function getNewChunk (startX, startY, layerID)
	{
		var chunk;
		var tIndex;

		chunk = new Object();
		chunk.chunkID 	= "AAAA";
		chunk.width   	= chunkWidth;
		chunk.height  	= chunkHeight;	
		chunk.tileSet 	= 0;
		chunk.startX  	= startX;
		chunk.startY  	= startY;
		chunk.endX		= chunk.width + chunk.startX - 1;		// Where this chunk ends (X)
		chunk.endY		= chunk.height + chunk.startY - 1;		// Where this chunk ends (Y)
		chunk.data    	= new Array(chunk.width * chunk.height);	// Our raw data
		chunk.coordName = startX + "x" + startY + "x" + layerID;	// The master name
		chunk.toUpdate  = false;					// Are we sending this chunk as an update?
		chunk.mapAttribs= new Array();					// Map attributes; unique to each tile, can be null
		chunk.lastUpd   = -1;						// When was this chunk last updated? May need to be updated after update to DB...
		chunk.seq		= 0;						// For our personal chunk-tracking, record our 'sequence' number to ensure older updates don't overwrite newer updates
		

		
		//	Setup functions

		chunk.isTileHere = isTileHere;


		//	Perform dummy initialization. Use the most appropriate tile.
		if (layerID == realmInfo.id)
			tIndex = realmInfo.tiledefault;
		else
			tIndex = -1;
		for ( i = 0; i < chunk.data.length; i++ ) {
			chunk.data[i] = tIndex;
			chunk.mapAttribs[i] = new Object();
		}
		

		return chunk;
	}
	
	

	//
	// Basic data function for chunks to be able to see if the coordinates
	// fall within its' known boundaries. This is meant to be attached
	// to a chunk object.

	function isTileHere(x, y)
	{
		if ( x < this.startX || x > this.endX || y < this.startY || y > this.endY )
			return false;

		return true;
	}

	//
	// Decode an attribute string back into detailed data
	// to be stored in an entry of an attribute array for
	// an object. 
	//
	function decodeAttribStr(str,attribObj)
	{
		var c;
		var results;
		var i;



		// 'Reset' object

		attribObj.passable = true;
		attribObj.overlay  = null;

		// Start parsing

		results = str.split('+');

		for ( i = 0; i < results.length; i++ )
		{
			if ( results[i].length > 0 )
			{
				c = results[i].charAt(0);

				switch (c)
				{
					case 'o':

						// Overlay

						attribObj.overlay = parseInt(results[i].substring(1));

						break;

					case 's':

						// Solidity

						attribObj.passable = false;

						break;
				}
			}
		}
	}

	//
	// 'Translate' detail data into flag data. Seperator is plus.
	//  This builds a string that can be used for a part of
	//	an attribute array for a chunk.
	//
	function buildAttrib(att)
	{
		var bResult;




		if (att == null)
			return "";

		bResult = "";

		// Overlay data

		if (att.overlay != null)
			bResult += "o" + att.overlay;

		bResult += "+";

		// Passability (false means solid or 'sl')

		if (att.passable != null && !att.passable)
		{
			bResult += "s";
		}

		return bResult;
	}
	
	// Calculate a diff of the current changes and the prior
    // known data.  Returns an array reflecting that diff.
    function calcDiffBlock( currentData, priorData )
    {
        var diffData = new Array(currentData.length);
        var index;


        for (index = 0; index < currentData.length; index++)
        {
            if (currentData[index] == priorData[index])
            {
                diffData[index] = -2;
            } else {
                diffData[index] = currentData[index];
            }
        }

        return diffData;
    }

    // Applies a diff to the array.
    function applyDiff(diffData, currentData)
    {
    	var result = new Array(currentData.length);
        var index;


        for (index = 0; index < currentData.length; index++)
        {
        	// Either apply the diff data or use the existing data
            if (diffData[index] != -2)
            {
                result[index] = diffData[index];
            } else {
            	result[index] = currentData[index];
            }
        }
        
        return result;
    }



	// Converts an array to a string using the provided separator. NOTE: This is either
	// already available or may be useful elsewhere.
	function arrayToString ( arr, separator )
	{
		var i;
		var result;
		
		if ( !separator ) {
			separator = ",";
		}
		
		if ( arr.length < 1 ) {
			return "";
		}
		
		result = "" + arr[0];
		for ( i = 1; i < arr.length; i++ ) {
			result += separator + arr[i];
		}
		
		return result;
	}
	
	// Build a string representing the chunk data. This will be
	// strictly text.
	function buildTxtUpdateFromChunk ( chunk )
	{
		var resultData;
		var i;		



		// Setup the header

		resultData = "UPDATE!!!";
		resultData += chunk.coordName + "!!!";
		resultData += chunk.tileset + "!!!";

		// Encode the data itself
		
		if ( chunk.lastData != null ) {
			// Build a diff
			resultData += arrayToString(calcDiffBlock(chunk.data, chunk.lastData));
		} else {
			// Use just the content
			resultData += arrayToString(chunk.data);
		}
		
		// Encode the extra data

		resultData += "!!!";

		for ( i = 0; i < chunk.mapAttribs.length; i++ )
		{
			if ( i > 0 )
				resultData += ",";

			resultData += buildAttrib(chunk.mapAttribs[i]);
		}

		resultData += "!!!";


		// Add sequence # to avoid issues with updates conflicting
		// with refreshes

		resultData += chunk.seq;

		// Terminate to avoid issues with parsing

		resultData += "!!!";


		return resultData;
	}


	//
	// When the server responds, post this information to the chunk itself.
	//
	function postDataToChunk ( chunkName, tileset, data, detaildata, lastUpdate, origSeq )
	{
		var chunk;	
		var i;



		// Fetch from our repository; should already be there

		chunk = chunks[chunkName];

		debugLog("Chunk data post: '" + chunkName + "'");

		if ( chunk != null )
		{
			/*
			// Has anything changed since? In other words, is our current sequence equal to the request time's sequence?

			if ( origSeq != null )
			{
				if ( chunk.seq > origSeq )
					return;
			}
			*/

			// Great, post the array

			if ( data == null )
			{
				// Defaults; it was blank

				chunk.tileset = 0;
				chunk.lastUpd = 0;
				chunk.lastData = null;
			} else {

				// Use the info
				
				// Perform a diff with the last-known data against what we've
				// currently been doing, then apply the difference to the 
				// latest arriving data.

				if ( chunk.lastData != null ) {
					// Calc the difference (what the user has done between the
					// previous data and 'now').
					var dataDelta = calcDiffBlock(chunk.data, chunk.lastData);
					// Apply it to the incoming data
					chunk.data = applyDiff( dataDelta, data);
					// Note that the currently arriving data becomes the 'last' data
					chunk.lastData = data;
				} else {
					// Nothing special; apply the data directly
					chunk.data = data;
					// Last data needs to remain independent; make a copy.
					chunk.lastData = chunk.data.slice(0);
				}
				
				chunk.tileset = tileset;
				chunk.lastUpd = lastUpdate;
			}

			// Decode detail information
			if ( detaildata != null )
			{
				for ( i = 0; i < detaildata.length; i++ )
				{
					decodeAttribStr(detaildata[i], chunk.mapAttribs[i]);
				}
			}

			chunk.ready = true;

			// All done

			debugLog("Chunk update was SUCCESSFUL for " + chunkName);
		} else {

			// ??? What happened?

			debugLog("Received info for chunk not needed: " + chunkName);


		}
	}


	//
	//	Adds a chunk to the update queue.
	//
	function addChunkForUpdate ( chunk )
	{
		// No matter what, increment the sequence. We'll need this if something arrives out of order.

		chunk.seq += 1;
	
		// Skip chunks already flagged for update

		if ( chunk.toUpdate == true )
			return;

		// Add to our queue

		updateQueue.push(chunk.coordName);

		chunk.toUpdate = true;

		// Start a countdown if updates weren't previously made

		if ( updatesPending == false )
		{
			// Setup the delay for 2 seconds from now

			window.setTimeout(sendAllChunkUpdates, 2000);

			updateStatus.innerHTML="Updates detected...";			

			updatesPending = true;
		}
	}

	
	//
	// Sends every chunk noted in the queue
	//
	function sendAllChunkUpdates ()
	{
		var nameToSend;
		var chunkToSend;



		updateStatus.innerHTML = "Sent " + updateQueue.length + " update(s)";

		while ( updateQueue.length > 0 )
		{
			// Get the name

			nameToSend = updateQueue.shift();

			// Get the chunk

			chunkToSend = chunks[nameToSend];

			// Clear update flag

			chunkToSend.toUpdate = false;

			// Send it!

			if ( chunkToSend != null )
				sendChunk(chunkToSend);
			else {
				debugLog("ERROR sending update: " + nameToSend + " wasn't in my chunk pile.");
			}
		}


		// Clear the flag

		updatesPending = false;

	}	

	//
	// Upload a chunk to the server
	//
	function sendChunk ( chunk )
	{
		sendMessage(buildTxtUpdateFromChunk(chunk));
	}

	
	//
	// Request a chunk from the server
	//
	function requestChunk ( chunkName )
	{
		sendMessage("RETRIEVE!!!" + chunkName);
	}

	
	

	//
	// Real coordinate search for a tile. Falls thru to findChunkByTile
	//
	function findChunkByReal (realX, realY)
	{
		return findChunkByTile(Math.floor(realX / tileScale), Math.floor(realY / tileScale));
	}

	//
	// Find a chunk by tile coordinate. Uses static caching; WATCH OUT FOR CONCURRENCY!
	//

	var lastChunkX, lastChunkY, lastChunkLayer, lastChunk;
	var chunkCount = 0;

	
	
	function findChunkByTile (tileX, tileY, layerID)
	{
		var finalName;
		var finalX, finalY;

		finalX = Math.floor(tileX / chunkWidth) * chunkWidth;
		finalY = Math.floor(tileY / chunkHeight) * chunkHeight

		if ( layerID == null ) {
			layerID = realmID;
		}
		
		if ( finalX == lastChunkX && finalY == lastChunkY && lastChunkLayer == layerID && lastChunk != null )
			return lastChunk;

		
		// Build the name by coordinates and our current realm

		finalName = finalX + "x" + finalY + "x" + layerID;

		lastChunk = chunks[finalName];	

		if ( lastChunk == null )
		{
			//	Fetch that chunk. First, create a placeholder object and add it

			lastChunk = getNewChunk(finalX, finalY, layerID);
			chunks[finalName] = lastChunk;

			// Set the 'not quite ready' flag

			debugLog("Waiting for chunk updated on '" + finalName + "'");

			lastChunk.ready=false;

			// Send a chunk request query

			requestChunk(finalName);

			chunkCount++;
		}

		lastChunkX = finalX;
		lastChunkY = finalY;
		lastChunkLayer = layerID;

		return lastChunk;
	}


	//
	// Calculate a tile of a map. No error checking 
	// performed. Retrieves only tile indexes. Must
	// be provided the chunk object which the tile
	// belongs to.
	//
	function getTile (mdat, x, y)
	{
		var finalEntry;

		
		x = (x  - mdat.startX) % mdat.width;
		y = (y - mdat.startY) % mdat.height;

		finalEntry = x + y * mdat.width;

		return mdat.data[finalEntry];
	}

	
	
	//
	// Calculate an attribute set of a tile of a map.
	// No error checking. Retrieves only attribute
	// data.
	//
	function getTileAttribs (mdat, x, y)
	{
		x = (x  - mdat.startX) % mdat.width;
		y = (y - mdat.startY) % mdat.height;

		finalEntry = x + y * mdat.width;

		return mdat.mapAttribs[finalEntry];
	}


	//
	// Set a tile of a map. No error checking 
	// performed.
	//
	function setTile (mdat, x, y, tIdx)
	{
		var finalEntry;

		x = (x  - mdat.startX) % mdat.width;
		y = (y  - mdat.startY) % mdat.height;
		

		finalEntry = x + y * mdat.width;

		mdat.data[finalEntry] = tIdx;
	}

	function determinCoordName ( realX, realY )
	{
		return Math.floor(realX / tileScale / chunkWidth) * chunkWidth + "x" + Math.floor(realY / tileScale / chunkHeight)  * chunkHeight;
	}
	
	
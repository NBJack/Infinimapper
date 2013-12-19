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
 * Object Management Library
 * 
 * Games need objects in most cases; this library facilitates the
 * creation, manipulation, and storage of those objects.
 * 
 */
var		uniqueObjIDCount;		//	Used to assign temporary IDs to newly created objects. CONCURRENCY WARNING: unknown.
var		objTypeLibrary;			//	A library of known object types.
var		knownObjMap;			//	A map of objects by IDs. Must be flushed periodically for memory reasons.
var		objSnapToGrid;			// 	Indicates we need to snap objects to the grid

var 	OBJECT_TYPE_REGION = 0;//  Indicates the object is an arbitrary region


objTypeLibrary = {};
knownObjMap    = {};

uniqueObjIDCount = 1000;

objSnapToGrid  = false;


//
//	Creates a new empty object data structure with the assigned ID. If no ID is provided,
//  assigns a new temporary ID.
//
function createNewObject(libraryType, id)
{
	var			obj;
	var			finalID;
	
	
	
	//	Get or create the ID for the object. Note that
	//  temporary IDs are always < 0 until we obtain
	//	the number officially designated by the system.
	
	if ( id == null )
		finalID = -(uniqueObjIDCount++);
	else
		finalID = id;
	
	//	Layout the structure
	
	obj = new Object();
	obj.id 	   = finalID;
	obj.xCoord = 0;
	obj.yCoord = 0;
	obj.type   = libraryType;	//	Type must always be defined (aka the index in the object library this is from)
	obj.classType = null;		//  The 'class' type of the object (aka Type in Tiled)
	obj.data   = null;			//	This is left null until it is either retrieved or created.
	obj.width  = -1;			//  Width of the object (applies only to regions)
	obj.height = -1;		    //  Height of the object (applies only to regions)
	
	//	Add an initial reference to this object. We can update it later.
	
	knownObjMap[obj.id] = obj;
	
	return obj;
}


//
//	Creates a new object type definition.
//
function createNewObjectType (typeid, name, imgData, imgXOff, imgYOff, imgWidth, imgHeight )
{
	var		otype;
	
	
	
	otype = new Object();
	otype.id = parseInt(typeid);
	otype.name = name;
	otype.imgData = imgData;		//	A raw image source, can be a sprite sheet with many images.
	otype.imgXOff = parseInt(imgXOff);		//	X Offset of the image in the sprite sheet
	otype.imgYOff = parseInt(imgYOff);		//	Y Offset of the image in the sprite sheet
	otype.width = parseInt(imgWidth);					//  Width of the object, both for the image and physical object dimensions
	otype.height = parseInt(imgHeight);					//	Height of the object, both for the image and physical object dimensions
	
	
	//	Add to our library
	
	objTypeLibrary[typeid] = otype;
	
	return otype;
}


//
//	Send an object's data back to the server. If it exists, it will be updated.
//
function sendObject (obj)
{
	sendMessage("OBJINSERT!!!" + obj.xCoord + "x" + obj.yCoord + "x" + realmID + "!!!" + obj.id + "!!!" + obj.type + "!!!" + obj.width + "!!!" + obj.height + "!!!" + obj.classType);	
}

function sendObjectDelete (obj)
{
	sendMessage("OBJDELETE!!!" + obj.xCoord + "x" + obj.yCoord + "x" + realmID + "!!!" + obj.id + "!!!");	
}




//
//	Change an object's ID. A reference to the old ID in the map is left behind.
//
function updateObjectID (oldid, newid)
{
	knownObjMap[newid] = knownObjMap[oldid];
	knownObjMap[newid].id = newid;
}


//
//	Request all of the objects for a specific chunk
//
function requestObjsForChunk (coordname)
{
	sendMessage("RTRVOBJS!!!" + coordname + "!!!");
}



//
//	Get a list of object types for a realm
//
function requestObjTypes (realmid)
{
	sendMessage("GETOBJTYPES!!!" + realmid + "!!!");
}



//
//	Decodes an object type from a raw string. Seperator
//	will be ### for each attribute. It will automatically
//	be added to the type library, overwriting any prior
//	objects. Note the sprite sheet must correspond
//	to a known image; otherwise, it will be requested.
//
function decodeObjectTypeFromString (rawdata)
{
	var		pieces;
	var		imgDat;
	
	
	
	pieces = rawdata.split("###");	
	
	//	Ignore insufficient numbers of pieces
	
	debugLog("Decoding " + rawdata);
	
	if ( pieces.length < 7 )
	{
		debugLog("Object library entry ignored: " + rawdata);
		
		return;
	}
	
	//	How to decode pieces[2]? Check the global pool first, then create a new Image object if needed.
	
	imgDat = new Image();
    if ( pieces[2] == 0 || !pieces[2] ) {
        alert("WTF: " + rawdata );
    }
	imgDat.src = "FetchTiles?ref=objFetch&id=" + pieces[2];
    console.info("Fetching tileset ID " + pieces[2] + " for " + rawdata);
	
	
	return createNewObjectType(pieces[0], pieces[1], imgDat, pieces[3], pieces[4], pieces[5], pieces[6]);
}


function updateObjects(rawdata)
{
	decodeObjectDataFromString(rawdata);
}


//
//Decodes an object data payload from a raw string. Seperator
//will be ### for each entry. Note that the object type library
//must already have been loaded. Try to make this dynamic
//in the future.
//
function decodeObjectDataFromString (rawdata)
{
	var		pieces;
	var		imgDat;
	var		i;
	
	
	
	pieces = rawdata.split("###");	
	
	
	debugLog("Decoding " + pieces.length + " object information payload(s)");

	for ( i = 0; i < pieces.length; i++ )
	{
		decodeObjectInfo(pieces[i]);
	}
}


function decodeObjectInfo(rawdata)
{
	var		pieces;
	var		tileStore, oldTileStore;
	var		obj;
	
	
	
	
	pieces = rawdata.split(",");
	
	//	Ignore insufficient numbers of pieces
	
	if ( pieces.length < 4 )
	{
		debugLog("Not decoding object info " + rawdata);
		
		return;
	}
		
	//	Does this object exist?

	obj = knownObjMap[pieces[0]];
	
	if ( obj != null )
	{
		//	Update
		
		oldTileStore = obj.currentTile;
		
	} else {

		//	Create
		
		obj = createNewObject(pieces[1], pieces[0]);
		
		//	Store in known object map
		
		knownObjMap[obj.id] = obj;
		
		oldTileStore = null;
		
		debugLog("Created obj " + obj.id + " at " + obj.xCoord + "," + obj.yCoord);				
	}
	
	//	Change coordinates
	
	obj.xCoord = parseInt(pieces[2]);
	obj.yCoord = parseInt(pieces[3]);	
	obj.width = parseInt(pieces[5]);
	obj.height = parseInt(pieces[6]);

	
	//	Object deletion status
	
	if ( pieces[4] == 1 )
	{
		obj.deleted = true;
	}
	
	//	Locate the tile we need to use
	
	tileStore = findChunkByReal(obj.xCoord, obj.yCoord);
	
	//	If the new store is different
	
	if ( tileStore != oldTileStore )
	{
		//	Clear from the old chunk (if different)
		
		obj.currentTile = tileStore;
		
		debugLog("Adding to " + tileStore.coordName);
		
		//	Add to chunk
		
		addObjToChunk(tileStore, obj);
		
		if ( oldTileStore != null )
		{
			//	Remove from old chunk
		}
		
	}
	
	
}


//
//	Decode all entries in the response array
//
function decodeObjLibResponse ( rawdata )
{
	var		i;
	var		ob;
	
	
	for ( i = 1; i < rawdata.length; i++ )
	{		
		ob = decodeObjectTypeFromString(rawdata[i]);
		
		if ( ob != null )
			debugLog("Added " + ob.id);
	}
}

//
//	Piggy-backs an object to a chunk via an attribute
//	on the object 'objList'. If it does not already
//	exist, it will be added as an array. This should
//	help bring down memory consumption.
//
function addObjToChunk(chunk, obj)
{
	if ( chunk.objList == null )
		chunk.objList = new Array();
	
	chunk.objList.push(obj);
}


//
//	Removes an element by ID from a chunk. Note that
//	this can be risky; experiments are needed on
//	how this may act when several things are 
//	being altered at once.
//
function removeObjFromChunkByID(chunk, objid)
{
	var		i;
	var		removeMe;
	
	
	//	Empty list? Nothing to do. Fail silently with 'false'
	
	if ( chunk.objList == null )
		return false;
	
	removeMe = -1;
	
	debugLog("Removing object: " + objid);
	
	for ( i = 0; i < chunk.objList.length; i++ )
	{
		if ( chunk.objList[i].id == objid )
			removeMe = i;
	}
	
	if ( removeMe == -1 )
		return false;
	
	//	Splice and dice the element out.
	
	chunk.objList.splice(removeMe, 1);		
	
	return true;
	
}


//
// Finds an object by real coordinate point within a specified chunk.
//
function getObjAtPointInChunk (chunk, realx, realy)
{
	var		i;
	var		ob;
	var		pxLeft, pxRight, pxTop, pxBottom;
	
	
	//	Any objects to go through?
	
	if ( chunk.objList == null )
		return null;
	
	//	Go through all objects of the chunk.
	
	for ( i = 0; i < chunk.objList.length; i++ )
	{
		 ob = chunk.objList[i];
		 
		 
		 //	Skip deleted objects 
		 
		 if ( !ob.deleted )
		 {		 
			 //	Construct the boundaries 
			 
			 pxLeft = ob.xCoord;
			 pxTop = ob.yCoord;

			 if ( ob.type > 0 ) {
				 // Use type data
				 pxRight = ob.xCoord + objTypeLibrary[ob.type].width;
				 pxBottom = ob.yCoord + objTypeLibrary[ob.type].height;
			 } else {
				 // Use instance-stored width and height.
				 pxRight = ob.xCoord + ob.width;
				 pxBottom = ob.yCoord + ob.height;
			 }
			 // Are we within the coordinates?
			 
			 if ( realx >= pxLeft && realx <= pxRight
			   && realy >= pxTop  && realy <= pxBottom
			  )
				 return ob;
		 
		 }
	}
	
	return null;		
}


//
// Change the type of object we wish to place. 
//
function changeObjectToPaint (type)
{
	currentObjType = type;
}


//
// Flags an object for deletion, both locally and on the server.
//
function flagObjectForDeletion (id)
{
	var		ob;
	
	
	
	//	Get the object
	
	ob = knownObjMap[id];
	
	if ( ob == null )
	{
		//	Sometimes happens when an update arrives at a bad time
		
		debugLog("Unknown object ID: " + id + " cannot be flagged for deletion");
		
		return false;
	}
	
	//	Update the object flags
	
	ob.deleted = true;
	
	//	Send the message
	
}
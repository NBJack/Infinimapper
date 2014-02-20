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



	var toolbar, menubar, updateStatus, currentPaintPreview, paintPalette;
	var context, bufferCtxt;
	var mainscreen, offscreen;
	
	var ctWidth, ctHeight;	// The width and height of the entire browser area
	var offsetX, offsetY;	// Our current viewing offset for the X and Y coordinates	
	var texFast;			// A buffer designed to store the tile master image in a faster canvas object
	var currTime;			// The current time
	var secPassed;			// Time passed in our global timer loop
	var defaultTile;		// What's the default tile number?
	var currentPaintTile;	// WHat is our current paint tile?
	var currentObjType;		// What is the current object we want to place?
	var waitingImage;		// Our placeholder for unavailable graphics
	var unpassableImage;	// Our image used for unpassable tiles
	var paintMode;			// The mode we're currently in to paint.
	var activeRequests;		// The number of active requests made via AJAX
	var imageSet;			// The image soures holding all of the tiles we want
	var tileInfo;			// The information we store about tiles
	var realmID;			// The current chunk realm we are in
	var displayPassable;	//	Flag for displaying passability on the map. 
	var objSnapToGrid;		// Snap objects to grid mode flag.		
	var oldObjSelected;		// The prior object we selected.
	var moveActionMode;		// Indicates we need to move with action
	var cursorX, cursorY;		// The cursor coordinates, updated on each mouse move.	
	var absCursorX, absCursorY;	// The absolute cursor coordinates
	var cursorPresent;		// The cursor is present or absent
	var stampData;			// Information object storing stamp data (ex. tiles, dimensions)
	var sidebarPalette; 	// A collection of sidebar info
	var editorState;		// A collective editor state object	
	
	
	
	// Create the basic editor state object
	// TODO: Give this its own class.
	editorState = new Object();
	editorState.draw = new Object();
	editorState.render = new Object();
	// Setup some basic variables
	editorState.draw.currentLayerIndex = 0;
	editorState.draw.currentLayer = 0;
	editorState.draw.currentTile = -1;
	editorState.draw.paintMode = -1;
	
	
	//
	// 'Constants'
	// 
	
	var MODE_DRAW_TILE = 0;
	var MODE_DRAW_OVERLAY = 1;
	var MODE_ERASE_OVERLAY = 2;
	var MODE_SET_PASSABLE = 3;
	var MODE_ERASE_TILE = 4;
	var MODE_CLEAR_PASSABLE = 5;
	var MODE_MAKE_OBJECTS = 6;
	var MODE_SELECT_OBJECTS = 7;
	var MODE_DELETE_OBJECTS = 8;
	var MODE_MASS_TILE_SELECT = 20;
	var MODE_MASS_TILE_SELECT_START = 21;
	var MODE_MASS_TILE_SELECT_END = 22;
	
	var MODE_MAKE_REGION_OBJECT = 100;
	var MODE_MAKE_REGION_OBJECT_START = 101;
	var MODE_MAKE_REGION_OBJECT_END = 102;
	
	
	var MODE_TILE_STAMP = 30;
	
	
	
	moveActionMode = false;
	cursorPresent  = true;
	
	
	currentObjType    = 1;
	objSnapToGrid = true;
	displayPassable = true;

	oldObjSelected = null;
	

	editorState.draw.paintMode = -1; 

	waitingImage = new Image();
	waitingImage.src = "Images/blueprint.gif";

	//unpassableImage = new Image();
	//unpassableImage.src = "Images/cross.png";
	
	blankImage = new Image();
	blankImage.src = "Images/blank.png";
	

	stampData = new Object();

	// Grab the tile information

	constructImageSets();
	tileInfo = new Array();

	// Basics for tile and chunk data

	realmID = realmInfo.id;
	currentPaintTile = realmInfo.tiledefault; 

	defaultTile = realmInfo.tiledefault;
	
	
	sidebarPalette = {};
	


	//	Mouse data

	var mouseOldX, mouseOldY;
	var moveMapMode;


	function blockEvent ()
	{
		event.blockDefaultEvent();
	}


	function debugLog(txt)
	{
		if ( console.log )
			console.log(txt);
	}
	
    function updateMapStatus()
    {
        // Show the coordinates of the current tile (in the upper-left corner)
        mapStatusBar.innerHTML = "T:" + Math.floor(mouseX / tileScale) + "," + Math.floor(mouseY / tileScale);

    }

	function buildAndShowBookmark ()
	{
		var finalResult;
		var mapX, mapY;



		// Since we may be at a different scale, adjust for it
		// by setting up bookmarks based on map tiles. 

		mapX = Math.floor(mouseX / tileScale);
		mapY = Math.floor(mouseY / tileScale);
		
		// Start with our root URL

		finalResult = location.protocol + "//" + location.host +  location.pathname + "?x=" + mapX + "&y=" + mapY + "&realm=" + realmInfo.id;

		alert("Build bookmark:\n" + finalResult);
	}



	document.onswipemove=blockEvent;

	

	
	// Our refresh tracking variables: the counter and our official
	// periodic refresh rate.
	var refreshInterval, refreshIntervalMax;


	refreshIntervalMax = 1.5;
	refreshInterval = refreshIntervalMax;

	//
	// Based on what we know about the screen, perform an appropriate 
	// update based on the visible chunks. The messages will ask 
	//	'Has chunk X changed since timestamp Y?'; the server should
	//	respond with either no or updated information. This check
	// however will not be done if the request stack is too long;
	// otherwise we may swamp our own queue due to a slow connection.
	//
	function performCheckForRefresh ()
	{
		var minX, minY, maxX, maxY;
		var offsetX, offsetY;
		var dx, dy;
		var x, y, i;
		var chkName, chk;
		var layerId;



		// If we have a lot of updates, skip the refresh this time...

		if ( requestStack.length > 20 )
			return;

		// Calculate our offsets

		
		offsetX = mouseX;
		offsetY = mouseY;


		xMin = Math.floor(offsetX / tileScale / chunkWidth);
		xMax = Math.floor(xMin + ctWidth / tileScale / chunkWidth) + 2; 
		yMin = Math.floor(offsetY / tileScale / chunkHeight);
		yMax = Math.floor(yMin + ctHeight / tileScale / chunkHeight) + 2;


		// Refresh all known chunks in the grid

		console.log("Checking refresh... (Layer count: " + realmInfo.layers.length + ")");

		for ( i = 0; i < realmInfo.layers.length; i++ )
		{			
			layerId = realmInfo.layers[i].realmid;
			for ( x = xMin; x <= xMax; x++ )
			{
				for ( y = yMin; y <= yMax; y++ )
				{
					chkName = x * chunkWidth + "x" + y * chunkHeight + "x" + layerId;
	
					chk = chunks[chkName];
	
					if ( chk != null && chk.lastUpd != "-1" && chk.lastUpd != "None" && !chk.toUpdate )
					{
						sendMessage("REFRESH!!!" + chkName + "!!!" + chk.lastUpd + "!!!" + chk.seq + "!!!" );
						sendMessage("OBJRETRIEVE!!!" + chkName + "!!!" + chk.lastUpd + "!!!" );
					}
				}
			}
		}
	}

	//
	// Update the response
	//
	function handleDataResponse ()
	{
		var responseArr;
		var i;



		if ( this.readyState == 4 && this.status == 200 )
		{
			// Great; things went well.

			//alert(this.responseText);

			// Do we need to do anything?
			
			debugLog(this.responseText);

			metaResponseArr = this.responseText.split("\n");

			for ( i = 0; i < metaResponseArr.length; i++ )
			{

				// Grab our next response
	
				responseArr = metaResponseArr[i].split("!!!");
	
				// Figure out how to act
	
				if ( responseArr[0] == "OK" )
				{
					// What does the sequence say?
	
					
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
					decodeObjLibResponse(responseArr);
				} else if ( responseArr[0] == "OK_OBJ") {
					updateObjectID(responseArr[2], responseArr[3]);
				} else if ( responseArr[0] == "OBJDATA") {
					updateObjects(responseArr[1]);					
				} else {
					debugLog("Ignored response: '" + responseArr[0] + "'");
				}

			}
				

			// Clear the request flag

			requestInProgress = false;

			// Try to empty the queue

			sendNextQueuedItem();

		} else {
			
			try {
				if ( this.status == null || this.status == 0 )
					return;

				if ( this.status == 200 )
				{
					// Ignore
				}  else {
	
					// Note and clear the flag.

					debugLog("Problem during AJAX communication: " + this.status);

					requestInProgress = false;
				}
			} catch (err) {
				//console.log("Error on ajax response: " + err + " from " + this);
			}
		}

		
	}

	
	function touchStart(e)
	{
		var touches;
		var finalX, finalY;


		e.preventDefault();

		touches = e.touches;
		finalX = touches[0].pageX - mainscreen.totalOffsetLeft;
		finalY = touches[0].pageY - mainscreen.totalOffsetTop;

		if ( editorState.draw.paintMode >= 0 )
		{
			//	Draw

			doClickEvent(0, finalX, finalY, false);

		} else {

			// 	Move

			doClickEvent(2, finalX, finalY, false);

		}

		//console.log("Touch Start. #: " + touches.length);

	}

	function touchEnd(e)
	{
		e.preventDefault();
		
		doUnClickEvent(e);
	}
	

	// Capture touch events from a touch device.
	//
	function touchCap(e)
	{
		var touches;
		var finalX, finalY;
		
		

		e.preventDefault();


		touches = e.touches;
		finalX = touches[0].pageX - mainscreen.totalOffsetLeft;
		finalY = touches[0].pageY - mainscreen.totalOffsetTop;

		// We'll just permit moving for now; handling multiple touches is 
		// tricky. 

		if ( editorState.draw.paintMode < 0 )
			moveMapMode = true;
		else
			moveMapMode = false;

		doMoveAction(finalX, finalY);
	}	

	
	
	mainscreen = null;
	context = null;
	toolbar = null;
	menubar = null;
	paintPalette = null;

	offsetX = 0;
	offsetY = 0;


	//
	// Setup our images. This should be called after our master image is loaded.
	//

	function setupImageInfo()
	{
		var		i, suppIndex, j;
		var		tilesPerRow;
		var		tilecount;
		
		
		
		console.log("Tile count: " + imageInfo.tilecount);
		
		// Setup an empty tile
		
		tileInfo[-1] = new Object();
		tileInfo[-1].img = blankImage;
		tileInfo[-1].imgX = 0;
		tileInfo[-1].imgY = 0;
		
		var tileIndexCounter = 0;
		
		for ( j = 0; j < imageSet.length; j++ ) {
			// Calculate the real tiles per row
			tilesPerRow = (imageSet[j].width - 2 * imageInfo.border) / (imageInfo.tilewidth + imageInfo.gap);
			tileCount   = Math.ceil(imageSet[j].height / imageInfo.tileheight) * tilesPerRow;
			
			// Note where this index starts		
			imageSet[j].tileIndexOffset = tileIndexCounter;

			console.log("Tile index " + tileIndexCounter + " at set " + imageSet[j].src);
			
			for ( i = 0; i < tileCount; i++ ) {
				
				
				var tOffX, tOffY;

				// Calculate the offsets we need to find the image in the tileset

				tOffX = (i % tilesPerRow) * (imageInfo.tilewidth + imageInfo.gap) + imageInfo.border;
				tOffY = Math.floor(i / tilesPerRow) * (imageInfo.tilewidth + imageInfo.gap)  + imageInfo.border;

				// Store them in the tile object

				tileInfo[tileIndexCounter] = new Object();
				tileInfo[tileIndexCounter].img = imageSet[j];
				tileInfo[tileIndexCounter].imgX = tOffX;
				tileInfo[tileIndexCounter].imgY = tOffY;

				tileIndexCounter++;
			}
		}
		

	}

	// Add the basic resize detector for our canvas
	// TODO: Try to use dojo here instead
	function resizeEvent ()
	{	
		var 	tOff;
		
		
		
		if ( mainscreen != null && context != null )
		{
			var toolRatio;



			ctWidth = window.innerWidth;
			ctHeight = window.innerHeight;

			// Calculate toolbar as 20% of the rest of the table, but no less than 100 pixels.

			toolRatio = Math.max(ctWidth * .2, 150);

			console.log("RESIZE: width: " + ctWidth + " height: " + ctHeight );

			// Setup the toolbar 

			toolbar.width = toolRatio + "px";
			toolbar.height = ctHeight - 36 - 20 - 100;


			paintPalette.style.width = toolRatio + "px"; 
			paintPalette.style.height = (ctHeight - 50 - 36 - 100) + "px";

			menubar.width = ctWidth;
			menubar.height = 36;

			// Remaining area is canvas

			mainscreen.width = ctWidth - toolRatio - 10;
			mainscreen.height = ctHeight - 36 - 20;


			offscreen.width = mainscreen.width;
			offscreen.height = mainscreen.height;

			document.getElementById("lowerhalf").height = ctHeight - 36 - 20;
			document.getElementById("lowerhalf").width = ctWidth / 2;
			
			
			//	Calculate mainscreen's offset in order to properly determine a mouse click location 
			//	relative to itself. Add it as attributes.
			
			tOff = calculateTotalOffset(mainscreen);
			
			mainscreen.totalOffsetLeft = tOff.offsetLeft;
			mainscreen.totalOffsetTop  = tOff.offsetTop;
		}


	}

	
	//
	// Paint a stamp 
	//
	function paintStampAt(realX, realY, stamp)
	{
		var x, y;
		var index;
		
		
		
		// Write all tiles out from the linear array
		
		index = 0;
		
		for ( x = 0; x < stamp.tilesWidth; x++ ) 
		{
			for ( y = 0; y < stamp.tilesHeight; y++ )
			{
				// Paint the entry
				
				if ( stamp.dataArray[index] != null )
					setTileAtRealPoint(
						x * imageInfo.tilewidth + realX, 
						y * imageInfo.tileheight + realY, 
						stamp.dataArray[index]);
				
				index++;
			}
		}
		
		// Objects (TODO: Add the ability to clone existing objects)
		
		if ( stamp.objectData )
		{
			// Copy all objects
		}
		
	}
	
	
	//
	// Gets a single tile at a real-world coordinate. If that tile isn't
	// available, a null will be returned.
	//
	function getTileAtRealPoint(realX, realY, layerID)
	{
		var drawingChunk;
		var dx, dy;
		var t;
		

		// Calculate tile

		dx = Math.floor(realX / tileScale);
		dy = Math.floor(realY / tileScale);
		
		// Retrieve chunk
		
		drawingChunk = findChunkByTile(dx, dy, layerID);
		
		// Abort drawing if we're trying to draw on an unloaded chunk.
		
		if ( !drawingChunk.ready )
			return null;		

		// Grab that tile
		
		t = getTile(drawingChunk, dx, dy);

		console.info(dx + "," + dy + " = " + t + " (" + drawingChunk.coordName + ")");
		
		return t;
	}
	
	
	//
	// Set a single tile at a real-world coordinate.
	//
	function setTileAtRealPoint(realX, realY, tileindex)
	{
		var drawingChunk;
		var dx, dy;
		
		

		// Calculate tile

		dx = Math.floor(realX / tileScale);
		dy = Math.floor(realY / tileScale);

		// Retrieve chunk
		
		drawingChunk = findChunkByTile(dx, dy, editorState.draw.currentLayer.realmid);
		
		//	Abort drawing if we're trying to draw on an unloaded chunk.
		
		if ( !drawingChunk.ready )
			return;		
		
		
		// Set to index
		
		setTile(drawingChunk, dx, dy, tileindex);

		// Add the chunk for update
		
		addChunkForUpdate(drawingChunk);
		
	}
	

	// 
	// Whatever we currently are painting, however we are painting it, is placed to the
	// map at the specified real coordinates.  If triggered by a click, initialClick will
	// be true.
	//
	function paintOnMapAt (realX, realY, initialClick)
	{
		var obj;
		var t;
		var dx, dy;
		var attribs;
		var drawingChunk;


		// Offset mouse

		//realY -= menubar.height;

		// Calculate tile

		dx = Math.floor(realX / tileScale);
		dy = Math.floor(realY / tileScale);

		//updateStatus.innerHTML = dx + "," + dy + "(" + realY + ") " + mouseY;
        updateStatus.innerHTML = "Tile: " + dx + "," + dy


        // Find the chunk

		drawingChunk = findChunkByTile(dx, dy, editorState.draw.currentLayer.realmid);
		
		//	Abort drawing if we're trying to draw on an unloaded chunk.
		
		if ( !drawingChunk.ready )
			return;

		// If it isn't null, draw.

		if ( drawingChunk != null )
		{
			switch ( editorState.draw.paintMode )
			{
				case MODE_ERASE_TILE:
				case MODE_DRAW_TILE:

					// Draw

					setTile(drawingChunk, dx, dy, currentPaintTile);

					// Note for update

					break;

				case MODE_MAKE_OBJECTS:
					
					//	Object creation tool
					
					obj = createNewObject(currentObjType);
					
					//	Do we snap this object to the grid or not?
					
					if ( objSnapToGrid )
					{
						obj.xCoord = dx * tileScale;	
						obj.yCoord = dy * tileScale;	
					} else {
						obj.xCoord = realX;
						obj.yCoord = realY;												
					}
					
					//	Add the object to the scratch list of the chunk.
					
					t = findChunkByTile( dx, dy );					
					
					addObjToChunk( t, obj );
					
					//	Send object in as update
					
					sendObject(obj);
					
					break;
					
				case MODE_SELECT_OBJECTS:
					
					//	Object selection tool
					
					t = findChunkByReal(realX, realY);
					
					obj = getObjAtPointInChunk(t, realX, realY);
					
					if ( obj != null )
					{
						debugLog("Object found: " + obj.id);
						
						//	Note that object is selected
						
						if ( oldObjSelected != null )
							oldObjSelected.selected = false;
						
						obj.selected = true;
						
						oldObjSelected = obj;
						
						// Load information on it
						
						document.getElementById("objpropeditor").contentWindow.loadObjectInstance(obj.id);
					}
					
					break;
					
				case MODE_DELETE_OBJECTS: 
					
					//	Object deletion tool
					
					t = findChunkByReal(realX, realY);
					
					ob = getObjAtPointInChunk(t, realX, realY);
					
					if ( ob != null )
					{
						//	Clear out selection if it was previously chosen
						
						if ( oldObjSelected == ob )
						{
							oldObjSelected.selected = false;
							oldObjSelected = null;
						}
						
						//	Remove the object
						
						removeObjFromChunkByID(t, ob.id);
						
						//	Notify the server
						
						sendObjectDelete(ob);
					}
					
					break;
					
				case MODE_MAKE_REGION_OBJECT:
					
					// Beginning of a region object
					
					massSelectStartX = realX;
					massSelectStartY = realY;
					massSelectEndX = realX;
					massSelectEndY = realY;
					
					// Switch to end mode
					
					editorState.draw.paintMode = MODE_MAKE_REGION_OBJECT_END;
					
					break;
					
				case MODE_MASS_TILE_SELECT:
					
					// Beginning of mass tile selection; note the starting coordinates.
					
					massSelectStartX = realX;
					massSelectStartY = realY;
					massSelectEndX = realX;
					massSelectEndY = realY;
					
					// Switch to end mode
					
					editorState.draw.paintMode = MODE_MASS_TILE_SELECT_END;
					
					break;
					
					
				case MODE_MAKE_REGION_OBJECT_END:
				case MODE_MASS_TILE_SELECT_END:
					
					// Update the ending selection box
					
					massSelectEndX = realX;
					massSelectEndY = realY;
					
					break;
					
				case MODE_TILE_STAMP:
					
					// Draw a stamp iff this was triggered by a click
					
					if ( initialClick )
					{
						paintStampAt(realX, realY, stampData);
					}
					
					break;
			}

			// Always schedule for update

			addChunkForUpdate(drawingChunk);
		}
	}
	
	//
	//  Mouse movement
	//

	moveMapMode = false;

	//
	// Calculates an absolute offset by working our way back to the parent offset.
	//
	function calculateTotalOffset (n)
	{
		var result;
		var p;
		
		
		
		result = new Object();
		
		
		// Initialize the beginning offset
		
		result.offsetLeft = 0;
		result.offsetTop  = 0;
		
		//  Now, work our way through each offset parent until we reach the top.
		
		p = n;
		
		while ( p != null )
		{
			result.offsetLeft += p.offsetLeft;
			result.offsetTop += p.offsetTop;
				
			p = p.offsetParent;
		}
	
		
		return result;
	}
	
	
	function mouseIn (e)
	{
		cursorPresent = true;
	}
	
	
	function mouseOut (e)
	{
		cursorPresent = false;
	}
	
	
	function mouseCapture (e)
	{	
		cursorX = e.pageX - mainscreen.totalOffsetLeft;
		cursorY = e.pageY - mainscreen.totalOffsetTop;

		if ( moveActionMode )	
			doMoveAction(cursorX, cursorY);

		absCursorX = cursorX + mouseX;
		absCursorY = cursorY + mouseY;	
		
		
	}



	function doMoveAction (pX, pY)
	{
		var	dx, dy;
		var 	drawingChunk;

        updateMapStatus();

		//	Adjust offsets

		if ( moveMapMode )
		{
			mouseX += (mouseOldX - pX);
			mouseY += (mouseOldY - pY);

			mouseOldX = pX;
			mouseOldY = pY;

			//console.log("Mouse: " + mouseX + "," + mouseY);

		} else {

			paintOnMapAt((pX + mouseX), (pY + mouseY), false);
		}
	}

	
	
	//
	//	Mouse click event
	//
	function mouseClickEvent (e)
	{
		doClickEvent(e.button, e.pageX - mainscreen.totalOffsetLeft, e.pageY  - mainscreen.totalOffsetTop, true);	
	}

	

	function doClickEvent (type, pX, pY, wasMouse)
	{
		var drawingChunk;
	
		
		
		mouseClick = true;	

		mouseOldX   = pX;
		mouseOldY   = pY;


		//console.log("Click event type: " + type);

		if ( type == 2 || editorState.draw.paintMode == -1 )
		{
			moveMapMode = true;

			//console.log("Movin mode");
		} else if ( type == 1 )
		{

			moveMapMode = true;
			
		} else if (type == 0) {
			//	Draw!

			moveMapMode = false;

			//	Initial drawing
			
			paintOnMapAt((pX + mouseX), (pY + mouseY), true);
		}

		// For mice, make sure we invoke the motion capture event

		if ( wasMouse && (editorState.draw.paintMode != 6 || moveMapMode)  )
			moveActionMode = true;
	}

	function mouseUnClickEvent (e)
	{
		mouseClick = false;
		moveActionMode = false;
		
		
		doUnClickEvent(e, e.pageX - mainscreen.totalOffsetLeft, e.pageY  - mainscreen.totalOffsetTop);
		
	}
	
	function doUnClickEvent (e, px, py)
	{
		//  Do any post-state cleanup
		
		switch ( editorState.draw.paintMode )
		{
		case MODE_MAKE_REGION_OBJECT_END:
			
			// Note the dimensions of the region
			
			var regionObj = createNewObject(OBJECT_TYPE_REGION);
			regionObj.xCoord = Math.floor(massSelectStartX);
			regionObj.yCoord = Math.floor(massSelectStartY);
			regionObj.width = Math.floor(massSelectEndX - massSelectStartX);
			regionObj.height = Math.floor(massSelectEndY - massSelectStartY);
			
			//			Add the object to the scratch list of the chunk.
			
			var t = findChunkByReal(regionObj.xCoord, regionObj.yCoord);					
			
			addObjToChunk( t, regionObj );
			
			//	Send object in as update
			
			sendObject(regionObj);
			
			
			// Rest the state
			
			editorState.draw.paintMode = MODE_MAKE_REGION_OBJECT;
			
			break;
			
		case MODE_MASS_TILE_SELECT_END:
			
			
			// Note dimensions
			
			console.log(massSelectStartX + "," + massSelectStartY + " - " + massSelectEndX + "," + massSelectEndY);
			
			stampData.sourceStartX = massSelectStartX;
			stampData.sourceStartY = massSelectStartY;
			stampData.sourceEndX = massSelectEndX;
			stampData.sourceEndY = massSelectEndY;
			
			stampData.width = stampData.sourceEndX - stampData.sourceStartX;
			stampData.height = stampData.sourceEndY - stampData.sourceStartY;
			
			// Note tiles affected			
			
			stampData.tilesWidth = Math.ceil(stampData.width / imageInfo.tilewidth);
			stampData.tilesHeight = Math.ceil(stampData.height / imageInfo.tileheight);
			
			stampData.dataArray = new Array();
			stampData.overlayArray = new Array();
			
			var x, y, index;
			
			// Grab each tile and store it linearly
			
			index = 0;
			
			for ( x = 0; x < stampData.tilesWidth; x++ )
			{
				for ( y = 0; y < stampData.tilesHeight; y++ )
				{
					stampData.dataArray[index] =						
					  getTileAtRealPoint(stampData.sourceStartX + x * imageInfo.tilewidth, 
						  			     stampData.sourceStartY + y * imageInfo.tileheight,
                                         editorState.draw.currentLayer.realmid
                      );

					index++;
				}
			}
			
			
			// Make the data available
			
			stampData.available = true;
			
			console.log(stampData.width + "x" + stampData.height );
			
			// Change state to stamps
			
			editorState.draw.paintMode = MODE_TILE_STAMP;
			
			break;
		}
		
	}

	//
	// Changes our current 'paint' tile. Meant to be called from appropriate 
	// paint image with its' paintIndex set.
	//
	function changePaintIndex (e)
	{
		var	tiX, tiY;
		var	ti;
		var targetObj;
		var targetImageSet;
		var realX, realY;

	
		targetObj = e.target;
		
		// Calculate what we hit. Note that browsers are a bit...dishelved on what the 
		// standard is. :/

		if ( e.offsetX )
		{
			realX = e.offsetX;
			realY = e.offsetY;
		} else {
			// Layer hits need an offset adjustment
			realX = e.layerX - targetObj.offsetLeft;
			realY = e.layerY - targetObj.offsetTop;
		}
		

		tiX = Math.floor((realX - imageInfo.border) / (imageInfo.tilewidth + imageInfo.gap));
		tiY = Math.floor((realY - imageInfo.border) / (imageInfo.tileheight + imageInfo.gap));

		//	Make sure we had a valid selection!
				
		targetImageSet = imageSet[targetObj.tileSetIndex];
		
		if ( (tiX < 0) || (tiY < 0) || tiX >= targetImageSet.width || tiY >= targetImageSet.height )
			return;
		
		ti = tiX + tiY * Math.floor(targetImageSet.width / imageInfo.tilewidth);

		console.log("Paint index: " + ti + " (" + tiX + "," + tiY + ") "  + targetImageSet.tileIndexOffset);
		
		// Assign as our current paint

		currentPaintTile = ti + targetImageSet.tileIndexOffset;

		// Set the paint mode?

		if ( editorState.draw.paintMode != 0 && editorState.draw.paintMode != 1 )
		{
			//editorState.draw.paintMode = 0;

			// Switch to the draw tool

			selectTool("toolDraw");
		}
		
		// Set the cursor on the tile selected		
		var cursor = dojo.byId("tileSelectionCursor");
		cursor.style.left = (tiX * imageInfo.tilewidth + targetObj.offsetLeft) + "px";
		cursor.style.top = (tiY * imageInfo.tileheight + targetObj.offsetTop) + "px";
	}

	
	// Change the sidebar to a known item
	function changeSidebar (newItem)
	{
		if ( !paintPalette ) {
			console.log("Paint palette not yet available.");
			return;
		}
		console.log("Sidebar: " + newItem);
		
		// Get the item that we want to put in the sidebar
		var selectedPalette = sidebarPalette[newItem];
		
		// Does it already exist there?
		
		var currentPalette = paintPalette.lastChild;
				
		if ( currentPalette == selectedPalette ) {
			console.log("No need to change sidebar. Currently " + selectedPalette);
			return;
		}
				
		// Remove the old entry if it exists
		
		if ( currentPalette != null )
			paintPalette.removeChild(currentPalette);
		
		// Add the new entry
		
		paintPalette.appendChild(selectedPalette);
		paintPalette.currentPalette = newItem;
	}
	
	
	function setupObjectEditor ()
	{
		var objFrame;
		
		
		// Create iframe for object editing
		
		objFrame = document.createElement("iframe");
		objFrame.id = "objpropeditor";
		objFrame.src = "ui/editobjinstance.jsp";
		objFrame.width = "95%";
		objFrame.height = "95%";
		objFrame.style.border = "0px";
		
		
		// Add to palette
		
		sidebarPalette["objectEditor"] = objFrame;		
	}

	
	// Setup the tile palette on the right side by dynamically creating 
	// the necessary elements. Includes titles, cursors, etc.
	function setupPalette ()
	{
		var i;
		var paletteHolder;
		var titleEntry;
		var paintEntry;
		var cursorEntry;

		paletteHolder = document.createElement("span");
		cursorEntry = document.createElement("div");
		cursorEntry.className = "tileSelectCursor";
		cursorEntry.id = "tileSelectionCursor";
		cursorEntry.style.position = "relative";
		cursorEntry.style.left = 50 + "px";
		cursorEntry.style.top = 100 + "px";
		cursorEntry.textContent="";
		paletteHolder.appendChild(cursorEntry);
		
		
		for ( i = 0; i < imageSet.length; i++ )
		{
			titleEntry = document.createElement("div");
			titleEntry.textContent = "Tileset " + i;
			titleEntry.className = "tileName";
			paletteHolder.appendChild(titleEntry);
			
			paintEntry = document.createElement("img");
			paintEntry.src = imageSet[i].src;
			paintEntry.id  = "tilepaletteimg";
			paintEntry.paintIndex = 1;
			paintEntry.tileSetIndex = i;
			paintEntry.onclick = changePaintIndex;
			// We used to have a touch event here, but onclick handles what we need.
		
			// Add to the end
			paletteHolder.appendChild(paintEntry);
		}

		// Remove any prior palette. 

		sidebarPalette["tileSelector"] = paletteHolder;
		
		return paintEntry;
	}
	
	function setupLayers ()
	{
		var i;
		var layerList, layerEntry;
		var layerContainer;
		
		layerContainer = dojo.byId("layerList");
		layerContainer.style.height = "100px";
		dojo.empty(layerContainer);
		
		layerList = document.createElement("div");
		layerEntry = document.createElement("span");
		layerEntry.className = "layerTitle";
		layerEntry.textContent = "Layers";
		layerList.appendChild(layerEntry);

		for ( i = 0; i < realmInfo.layers.length; i++ ) {
			layerEntry = document.createElement("div");
			layerEntry.textContent = realmInfo.layers[i].name + " (" + realmInfo.layers[i].realmid + ")";
			layerEntry.className = "layerUnselected";
			layerEntry.id = "layerEntry" + i;
			layerEntry.layerNum = i;
			layerEntry.onclick = selectLayerEntry;
			layerList.appendChild(layerEntry);
		}
		
		console.log("Setting layers..." + layerContainer);
		layerContainer.appendChild(layerList);
		
		// Reset layer state in the editor		
		editorState.draw.currentLayer = realmInfo.layers[0];
		selectLayer(0);
	}
	


	var keys = new Array();

	function keyMapDown (e)
	{
		keys[e.keyCode] = true;
	}

	function keyMapUp (e)
	{
		keys[e.keyCode] = false;
	}

	// Key constants

	KEY_LEFT = 37;
	KEY_UP   = 38;
	KEY_RIGHT= 39;
	KEY_DOWN = 40;
	KEY_SHIFT= 16;


	function isKeyDown (code)
	{
		if ( keys[code] != null )
			return keys[code];

		return false;
	}

    var liveSocket;

	function setup ()
	{
		// Grab our basic elements of interest

		toolbar    = document.getElementById('toolbar');
		paintPalette = document.getElementById('paintPalette');
		menubar    = document.getElementById('menubar');
		updateStatus = document.getElementById('updateStatus');
		mainscreen = document.getElementById('mainscreen');
		currentPaintPreview = document.getElementById('currentPaintPreview');
        mapStatusBar = document.getElementById('mapStatus');

		// Setup graphics on our canvas

		context = mainscreen.getContext('2d');

		context.mozImageSmoothingEnabled = false;

		setInterval("renderWin()",16.7);

		window.onresize=resizeEvent;

		//	Create an offscreen buffer

		offscreen = document.createElement("canvas");
		bufferCtxt= offscreen.getContext('2d');
		bufferCtxt.mozImageSmoothingEnabled = false;


		//	Resize

		resizeEvent();

		//	Setup the palettes

		setupPalette();
		setupObjectEditor();
		setupLayers();
		changeSidebar("tileSelector");

		//	Setup events

		mainscreen.onmousedown = mouseClickEvent;
		mainscreen.onmouseup   = mouseUnClickEvent;
		mainscreen.onmousemove = mouseCapture;
		mainscreen.onmouseover = mouseIn;
		mainscreen.onmouseout  = mouseOut;
		
		document.onkeydown     = keyMapDown;
		document.onkeyup       = keyMapUp;


		mainscreen.ontouchstart=touchStart;	
		mainscreen.ontouchmove= touchCap;
		mainscreen.ontouchend  =touchEnd;
		mainscreen.ontouchabort=touchEnd;

        // Attempt to establish a websocket link
        liveSocket = new WebSocket('ws://localhost:8080/UpdateSocket')

        if ( liveSocket != null ) {

            liveSocket.onopen = function () {
                liveSocket.send("I'm ALLIIIVVEE!");
            }

            liveSocket.onmessage = function(msg) {
                updateStatus.innerHTML = msg.data;
            }

        }

		//	Start timer

		currTime = new Date();

		//	Select a default tool

		selectTool("toolMove");

		//	Grab a nice object library 
		
		sendMessage("OBJLIBRETRIEVE!!!0x0x0!!!");
		
		//	Ready

		updateStatus.innerHTML = "Ready.";
	}



	// Our primary rendering loop. 
	function renderWin ()
	{
		var x, y, i;
		var xMin, xMax, yMin, yMax;
		var finalX, finalY;
		var pxOffsetx, pxOffsety;
		var mTile, ovTile;
		var mChunk;
		var tileIndex;
		var attribs;
		var layer;
		
		

		//	Calculate time offset

		secPassed = (new Date() - currTime) / 1000.0;

		currTime = new Date();

		//
		//	Hodge-podge event loop
		//
		var baseSpeed = 1;
		if ( isKeyDown(KEY_SHIFT))
		{
			baseSpeed = 4;
		}

		if ( isKeyDown(KEY_LEFT) )
		{
			mouseX -= 500 * secPassed * baseSpeed;
            updateMapStatus();
		} else if ( isKeyDown(KEY_RIGHT) )
		{
			mouseX += 500 * secPassed * baseSpeed;
            updateMapStatus();
		}
		
		if ( isKeyDown(KEY_UP) )
		{
			mouseY -= 500 * secPassed * baseSpeed;
            updateMapStatus();
		} else if ( isKeyDown(KEY_DOWN) )
		{
			mouseY += 500 * secPassed * baseSpeed;
            updateMapStatus();
		}

		//
		// Refresh our chunks periodically
		
		refreshInterval -= secPassed;
		
		if ( refreshInterval < 0 )
		{
			refreshInterval = refreshIntervalMax;

			performCheckForRefresh();
		}

        // Save where we are
        if ( sessionStorage ) {
            sessionStorage.setItem("mouseX", mouseX);
            sessionStorage.setItem("mouseY", mouseY);
            if ( isNaN(mouseX) ) {
                mouseX = 0;
            }
            if ( isNaN(mouseY) ) {
                mouseY = 0;
            }

        }

		offsetX = mouseX;
		offsetY = mouseY;


		xMin = Math.floor(offsetX / tileScale);
		xMax = Math.floor(xMin + ctWidth / tileScale) + 2; 
		yMin = Math.floor(offsetY / tileScale);
		yMax = Math.floor(yMin + ctHeight / tileScale) + 2;

		pxOffsetx = Math.floor(offsetX);
		pxOffsety = Math.floor(offsetY);

		if ( imageInfo.useBackground == 1 )
		{
			// Clear the background

			bufferCtxt.fillStyle = '#101010';
			bufferCtxt.fillRect( 0, 0, mainscreen.width, mainscreen.height );	

		}

		//	
		//	Render all tiles in each layer. 	
		//
		
		for ( layer = 0; layer < realmInfo.layers.length; layer++ ) {
			
			// Either show the realm based on visibility or show it if it is the currently selected layer.
			if ( realmInfo.layers[layer].visible || editorState.draw.currentLayerIndex == layer ) {
				renderTiles(xMin, xMax, yMin, yMax, pxOffsetx, pxOffsety, tileScale, realmInfo.layers[layer].realmid);
			}
			
		}
		
		// Tweak the max's for extra space
		
		xMax += chunkWidth;
		yMax += chunkHeight;

        // Now, render all objects in the primary layer.
        // TODO: Render objects in either all layers or just object layers
		
		for ( x = xMin; x < xMax; x+= chunkWidth )
			for ( y = yMin; y < yMax; y += chunkHeight )			
			{
				var		obj;
				var		objTypeData;
				
				
				//	Retrieve the chunk of intrest
				
				mChunk = findChunkByTile(x, y);

				
				if ( mChunk.objList != null )
				{
					//	Render each object, in order.
					
					for ( i = 0; i < mChunk.objList.length; i++ )
					{
						obj = mChunk.objList[i];

						//	Ignore deleted objects
						
						if ( !obj.deleted )
						{
						
							//	Lookup the type
							
							objTypeData = objTypeLibrary[obj.type];
							
							//console.log("Rendering " + obj.id);
							
							finalX = obj.xCoord - pxOffsetx;
							finalY = obj.yCoord - pxOffsety;
							
							if ( objTypeData == null )
							{
								
								bufferCtxt.fillStyle = '#FF0000';						
								bufferCtxt.fillRect( obj.xCoord - pxOffsetx, obj.yCoord - pxOffsety, tileScale, tileScale );
							} else if ( obj.type == 0 ) {
								// Render the region
								if ( obj.selected ) {
									// Use the 'selected' color
									bufferCtxt.strokeStyle = '#FFFF00';
								} else {
									// Use the default color
									bufferCtxt.strokeStyle = '#BB00FF';
								}
								bufferCtxt.strokeRect( finalX, finalY, obj.width, obj.height );								
							} else {
	
								//	Use the image
								
								bufferCtxt.drawImage(objTypeData.imgData, objTypeData.imgXOff, objTypeData.imgYOff, objTypeData.width, objTypeData.height, finalX, finalY, objTypeData.width, objTypeData.height);
								
								if ( obj.selected)
								{								
									bufferCtxt.lineWidth = 4;
									bufferCtxt.strokeStyle = '#000000';						
									bufferCtxt.strokeRect( obj.xCoord - pxOffsetx, obj.yCoord - pxOffsety, objTypeData.width, objTypeData.height );
									bufferCtxt.lineWidth = 2;
									bufferCtxt.strokeStyle = '#FFFF00';						
									bufferCtxt.strokeRect( obj.xCoord - pxOffsetx, obj.yCoord - pxOffsety, objTypeData.width, objTypeData.height );								
								}
							}
						}
					}
				
				}
			
			}

		//	Render cursor 


		if ( pxOffsetx && cursorPresent )
		{
			var cX, cY;
			var gridSnap;
			var cWidth, cHeight;
			var oType;
			var hideCursor;


			hideCursor = false;
			bufferCtxt.lineWidth = 3;
			bufferCtxt.strokeStyle='#20FF30';
			
			switch ( editorState.draw.paintMode )
			{
				case MODE_MAKE_OBJECTS : 
					// Object placement
					
					oType = objTypeLibrary[currentObjType];
					
					if ( oType != null )
					{
						cWidth = oType.width;
						cHeight = oType.height;
					} else {
						cWidth = imageInfo.tilewidth;
						cHeight = imageInfo.tileheight;
					}

					// Use current snap procedure for object
					
					gridSnap = objSnapToGrid;			

					break;
					
				case MODE_DRAW_TILE:
				case MODE_DRAW_OVERLAY:
				case MODE_ERASE_TILE:
				case MODE_ERASE_OVERLAY:
				case MODE_SET_PASSABLE:
				case MODE_CLEAR_PASSABLE:

					// Tiles

					cWidth = imageInfo.tilewidth;
					cHeight = imageInfo.tileheight;

					gridSnap = true;

					break;
					
				case MODE_TILE_STAMP:
					
					// Stamping
					
					if (stampData.available)
					{
						// Make the cursor as big as the stamp
						
						cWidth = stampData.tilesWidth * imageInfo.tilewidth;
						cHeight = stampData.tilesHeight * imageInfo.tileheight;

						
						gridSnap = true;
						
						bufferCtxt.strokeStyle='#80AAFF';
						
						
					} else {
						
						hideCursor = true;
					}
					
					break;
					
				default:
					
					// Dont' show anything if we aren't in a supported mode.
					
					hideCursor = true;
			}
			
			if ( gridSnap )
			{		
				cX = Math.floor(absCursorX / imageInfo.tilewidth) * imageInfo.tilewidth - pxOffsetx;
				cY = Math.floor(absCursorY / imageInfo.tileheight) * imageInfo.tileheight - pxOffsety;
			} else {
				cX = Math.floor(absCursorX) - pxOffsetx;
				cY = Math.floor(absCursorY) - pxOffsety;
			}

			if ( !hideCursor )
				bufferCtxt.strokeRect( cX, cY, cWidth, cHeight );
		}
		
		//  Render any dragging rectangles
		
		if ( editorState.draw.paintMode == MODE_MASS_TILE_SELECT_END || editorState.draw.paintMode== MODE_MAKE_REGION_OBJECT_END )
		{
			bufferCtxt.lineWidth = 3;
			bufferCtxt.strokeStyle='#FF3020';
			
			
			bufferCtxt.strokeRect( 
					massSelectStartX - pxOffsetx, massSelectStartY - pxOffsety, 
					massSelectEndX - massSelectStartX, massSelectEndY - massSelectStartY);
		}
		
		// TODO: Render the current object region mode
		
		
		//	Render buffer

		context.drawImage(offscreen,0,0);

		
		
	}

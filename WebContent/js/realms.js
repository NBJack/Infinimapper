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


var oldTilesetID;			// Stores our prior tileset; if it's a match in the new realm, don't bother reloading graphics.
var pauseEditing;			// When true, pause editing while the realm loads
var resourceCountdown;		// Keeps track of the number of resources we want present before we continue


pauseEditing = false;

/**
 * Change the realm via dynamic script loading.
 */
function changeRealm ( rid, clearPosition )
{
	// Load the new realm information, overwriting the old.
	// Note: Some error checking should be performed on ID's
	// existance!
	
	oldTilesetID = realmInfo.tileset;

	pauseEditing = true;
	
	loadJS("js/realmInfo.jsp?id=" + rid, realmDataLoaded );

	// Change current global realm
	
	realmID = rid;

	// Flush chunks
	
	chunks = {};

    // Clear the map position if needed

    if ( clearPosition ) {
        mouseX = 0;
        mouseY = 0;
    }

}

/**
 * Phase 2 of the loading; script is done, we have our information
 * on the realm. If the tileset has changed, change the graphics.
 */
function realmDataLoaded ()
{
	// Check; do we need to do anything else?
	
	if ( realmInfo.tileset == oldTilesetID )
	{
		// Skip to the last step.
		
		finishSetup();

	} else {		
	
		constructImageSets();
	}
	
}

function constructImageSets ()
{
	console.log("Setting up tiles");
	
	// Setup the counter
	if ( imageInfo.supplement ) {
		resourceCountdown = imageInfo.supplement.length;
	} else {
        resourceCountdown = 1;
    }
			
	
	// Load new images; defer additional setup until done.	
	imageSet = new Array();

	if ( imageInfo.supplement )
	{
		var i;
		
		for ( i = 0; i < imageInfo.supplement.length; i++ )
		{
			imageSet[i] = new Image();
			imageSet[i].src = "FetchTiles?ref=realmFetch&id=" + imageInfo.supplement[i];
			imageSet[i].onload = deferredStart;
			imageSet[i].tileSetIndex = i;
		}
		
	} else {
        imageSet[0] = new Image();
        imageSet[0].src = "FetchTiles?ref=realmFetch&id=" + realmInfo.tileset;
        imageSet[0].onload = deferredStart;
        imageSet[0].tileSetIndex = 0;
    }
}


/**
 * Wait until all resources are finished before loading.
 */
function deferredStart ()
{
	resourceCountdown--;	
	
	if ( resourceCountdown <= 0 ) {
		startImageSetup();
	}
}


/**
 * Phase 3 of the loading; image is done. Now, setup the tool palette
 * so that we can use the new tiles.
 */
function startImageSetup ()
{	
	// TODO: Move all of this into a realmInit procedure of some kind
	
	setupImageInfo();
	setupPalette();
	setupLayers();
	
	// Default to the tile sidbar
	
	changeSidebar("tileSelector");


	finishSetup();
}

/**
 * Final Phase; allow editing to resume. Go to a default edit state.
 */
function finishSetup()
{
	// Reset tools
	
	currentPaintTile = realmInfo.tiledefault; 
	selectTool("toolMove");

	// Reset the chunks again.
	
	chunks = {};	

	// Clear the pause flag.
	
	pauseEditing = false;

    // Save the realm
    sessionStorage.setItem("currentRealm", realmID);
}


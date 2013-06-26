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


	var oldToolSelected;

	//
	// Simulate selection of a tool button
	//
	function selectTool (name)
	{	
		var toolCtrl;



		toolCtrl = document.getElementById(name);

		if ( toolCtrl == null )
		{
			console.log("No tool found by name '" + name + "'");
			return;
		}

		// Active tool control as if it was pressed.

		doToolClick(toolCtrl);
	}
        

	function doToolClick (src)
	{
		console.log(src.id);	

		// Undo old class, and set current selection class while noting it for next time.

		if ( oldToolSelected != null )
		{
			oldToolSelected.className = "ToolUnselected";						
		}

		src.className = "ToolSelected";

		oldToolSelected = src;

		
		// Default to tiles
		
		changeSidebar("tileSelector");
		
		
		// Determine what to do

		if ( src.id == "toolMove" )
		{
			editorState.draw.paintMode = -1;

		} else if ( src.id == "toolDraw" )
		{
			editorState.draw.paintMode = 0;

		} else if ( src.id == "toolDefault" )
		{
			// Draw, but use default mode.

			editorState.draw.paintMode = 4;
			
			if ( editorState.draw.currentLayerIndex == 0 )
				currentPaintTile = realmInfo.tiledefault;
			else
				currentPaintTile = -1;

		} else if ( src.id == "toolErase" )
		{
			editorState.draw.paintMode = 2;

		} else if ( src.id == "toolOverlay" )
		{
			editorState.draw.paintMode = 1;
			

		} else if ( src.id == "toolBookmark" )
		{
			// Grab our current coordinates and attach to this URL

			buildAndShowBookmark();

		} else if ( src.id == "toolRealm" )
		{
			var realmBox;
			
			
			realmBox = document.getElementById("realmselect");			
			realmBox.style.display='block';
			
		} else if ( src.id == "toolObjAdd")
		{
			var	objBox;
			
			
			
			//	Add an object
			
			editorState.draw.paintMode = 6;
			
			//	Show the object palette if not already visible
			
			objBox = document.getElementById("objectselect");
			
			objBox.style.display='block';
			
			
		} else if ( src.id == "toolObjSelect")
		{
			//	Select object
			
			editorState.draw.paintMode = 7;
			
			changeSidebar("objectEditor");
			
		} else if ( src.id == "toolObjDelete")
		{
			//	Delete object
			
			editorState.draw.paintMode = 8;
		} else if ( src.id == "toolExport")
		{
			//	Export current realm (TODO: Offer an advanced dialog)
			
			window.open("ExportRealm?realmid=" + realmInfo.id);
			window.open("FetchTiles?id=" + realmInfo.tileset + "&download=true");
			
		} else if ( src.id == "toolAbout")
		{
			//	Display a quick pop-up about the current realm. TODO: Make this an iframe!
			
			alert("Realm Info:\n" + realmInfo.description + "\n\nImage Info:\n" + imageInfo.description );
		} else if ( src.id == "toolTileSelect" )
		{
			//	Select tiles mode
			
			editorState.draw.paintMode = MODE_MASS_TILE_SELECT;
		} else if ( src.id == "toolLayer" )
		{
			// Shortcut to advance the current layer
			nextLayer();
			console.log("Current layer: " + editorState.draw.currentLayer.realmid);
		} else if ( src.id == "toolRegion" )
		{
			// Create object 'regions'
			editorState.draw.paintMode = MODE_MAKE_REGION_OBJECT;
		}
			
		
		
		console.log("Paint mode: " + editorState.draw.paintMode);
		
		

		// Passability is either on or off.
		if ( src.id == "toolPassable" || src.id == "toolBlocking" )
		{
			displayPassable = true;
			

			if ( src.id == "toolPassable" )			
				editorState.draw.paintMode = 3;
			else
				editorState.draw.paintMode = 5;
			
		
		} else {
			displayPassable = false;
		}

		// Update the text
		
		document.getElementById("statusToolText").innerHTML = src.alt;
		
	}

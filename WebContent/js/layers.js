/**
 * Manages layer information
 */


// Switch editing to the next layer
function nextLayer ()
{
	editorState.draw.currentLayerIndex++;
	
	// Wrap around layer switches
	if ( editorState.draw.currentLayerIndex >= realmInfo.layers.length ) {
		editorState.draw.currentLayerIndex = 0;
	}
	
	selectLayer(editorState.draw.currentLayerIndex);
}


/**
 * Sets the active layer and updates the UI elements associated with it.
 * @param index
 */
function selectLayer(index)
{
	var i;
	var layerEntry;
	
	// Set the current layer to draw upon
	editorState.draw.currentLayerIndex = index;
	editorState.draw.currentLayer = realmInfo.layers[editorState.draw.currentLayerIndex];
	
	// Update the UI state
	for ( i = 0; i < realmInfo.layers.length; i++ )
	{
		layerEntry = dojo.byId("layerEntry" + i);
		if ( i == index ) {
			layerEntry.className = "layerSelected";
		} else {
			layerEntry.className = "layerUnselected";			
		}
	}
}

function selectLayerEntry(e)
{
	selectLayer(e.target.layerNum);
}
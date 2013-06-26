
function renderTiles (xMin, xMax, yMin, yMax, pxOffsetx, pxOffsety, tileScale, layerNum)
{
	var x, y;
	var mchunk;
	var finalX;
	var tileScale;
	var tileInfoIndex;
	
	
	for ( x = xMin; x < xMax; x++ )
			for ( y = yMin; y < yMax; y++ )			
			{
				//	Retrieve the chunk of interest
				mChunk = findChunkByTile(x, y, layerNum);
				

				//	Is that chunk ready to be displayed?
				if ( !mChunk.ready )
				{
					mTile = null;
				}
				else {
					tileInfoIndex = getTile(mChunk,x,y);
					mTile = tileInfo[ tileInfoIndex ];
				}
				
				try
				{
					// NOTE: This calculation is optimizable as long as it does not fail.

					finalX = x * tileScale - pxOffsetx;
					finalY = y * tileScale - pxOffsety;
					
					//if ( x == xMin && y == yMin )
						//console.log("x" + x + " y" + y + " = " + tileInfoIndex + " of " + mChunk.coordName + " in " + tileScale);

					if ( mTile == null )
					{
						bufferCtxt.drawImage(waitingImage, finalX, finalY, tileScale, tileScale);
					} else {
						
						if ( tileInfoIndex >= 0 ) {
							bufferCtxt.drawImage(mTile.img, mTile.imgX, mTile.imgY, tileScale, tileScale, finalX, finalY, tileScale, tileScale);
						}
						
					}
				} catch ( err )
				{
					console.log(err);
					//	Ignore
				}


			}
		
}
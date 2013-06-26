package org.rpl.infinimapper;

/**
 * Provides a means of encoding chunk data internally.
 * 
 * @author rplayfield
 *
 */
public class ChunkData implements Comparable<ChunkData> {

	int			worldX, worldY;
	int			rightX, bottomY;
	
	String []	linearTileData;
	String [] 	linearAttribData;
	
	
	public static final int TILES_WIDTH_IN_CHUNK = 40;
	public static final int TILES_HEIGHT_IN_CHUNK = 40;
	
	
	public ChunkData ( int worldX, int worldY, String rawTileData )
	{
		this.worldX = worldX;
		this.worldY = worldY;
		this.rightX = worldX + TILES_WIDTH_IN_CHUNK - 1;
		this.bottomY = worldY + TILES_HEIGHT_IN_CHUNK - 1;
		
		this.linearTileData   = rawTileData.split(",");
	}
				
	
	/**
	 * Gets a tile from the specified coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getTileDataAt ( int x, int y )
	{
        int	finalCoord = x + y * TILES_WIDTH_IN_CHUNK;
		
		return linearTileData[finalCoord];		
	}

    public void setTileDataAt ( int x, int y, String value ) {
        int	finalCoord = x + y * TILES_WIDTH_IN_CHUNK;

        linearTileData[finalCoord] = value;
    }
	
	/**
	 * Gets attribute data from the specified coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getAttribDataAt ( int x, int y )
	{
		int		finalCoord;
		
		
		
		
		finalCoord = x + y * TILES_WIDTH_IN_CHUNK;
		
		return linearAttribData[finalCoord];
	}
	
	
	/**
	 * Gets the overlay tile at the coordinates specified. If none
	 * exists, returns null.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getOverlayTile ( int x, int y )
	{
		String		r;
		int			startX, endX;
		
		
		r = getAttribDataAt(x, y);
		
		if ( r.length() > 0 && r.contains("o") )
		{
			startX = r.indexOf('o') + 1;
			endX   = r.indexOf('+', startX);
		
			//	If a plus wasn't found, use the end
			
			if ( endX < 0 )
				endX = r.length();
			
			return r.substring(startX, endX);
		}
		
		return null;
	}
	
	
	/**
	 * Check to see if the tile is passable or not.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean getPassabilityAt ( int x, int y )
	{
		String		r;
		int			startX, endX;
		
		
		r = getAttribDataAt(x, y);
		
		if ( r.contains("s") )
		{
			return true;
		}
		
		return false;
	}


	@Override
	public int compareTo(ChunkData arg0) {
		
		if ( arg0.worldX != this.worldX )
			return arg0.worldX - this.worldX;
		
		if ( arg0.worldY != this.worldY )
			return arg0.worldY - this.worldY;
		
		return 0;
	}
	
	public int getTop ()
	{
		return worldY;
	}
	
	public int getBottom ()
	{
		return bottomY;
	}
	
	public int getLeft ()
	{
		return worldX;
	}
	
	public int getRight ()
	{
		return rightX;
	}
	
	
	/**
	 * Calculates the 'x' coordinate of a chunk, which is its' x coordinate divided by the width of a single chunk.
	 * @return
	 */
	public int getChunkCoordinateX ()
	{
		return worldX / TILES_WIDTH_IN_CHUNK;
	}
	
	/**
	 * Calculates the 'y' coordinate of a chunk, which is its' y coordinate divided by the height of a single chunk.
	 * @return
	 */
	public int getChunkCoordinateY ()
	{
		return worldY / TILES_HEIGHT_IN_CHUNK;
	}
}

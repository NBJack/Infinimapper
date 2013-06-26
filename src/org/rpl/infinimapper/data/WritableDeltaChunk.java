package org.rpl.infinimapper.data;

import com.sun.deploy.util.ArrayUtil;
import org.rpl.infinimapper.ChunkData;

import java.util.Arrays;

/**
 * User: Ryan
 * Date: 1/22/13 - 12:32 PM
 */
public class WritableDeltaChunk extends ChunkDelta {


    /**
     * Create a 'blank' delta.
     * @param userid
     */
    public WritableDeltaChunk(int userid) {
        super(userid, new String[ChunkData.TILES_WIDTH_IN_CHUNK * ChunkData.TILES_HEIGHT_IN_CHUNK]);
        clearData();
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
        validateCoordinates(x, y);
        int	finalCoord = x + y * ChunkData.TILES_WIDTH_IN_CHUNK;

        return tileData[finalCoord];
    }


    public void setTileDataAt ( int x, int y, String value ) {
        validateCoordinates(x, y);
        int	finalCoord = x + y * ChunkData.TILES_WIDTH_IN_CHUNK;

        this.tileData[finalCoord] = value;
    }


    /**
     * Clears all data in the delta to the transparent color.
     */
    public void clearData () {
        Arrays.fill(this.tileData, ChunkDelta.TILE_TRANSPARENT);
    }

    /**
     * Make sure that this coordinate pair is within the chunk.
     * @param x
     * @param y
     */
    private void validateCoordinates( int x, int y ) {
        if ( x < 0 || y < 0 || x >= ChunkData.TILES_WIDTH_IN_CHUNK || y >= ChunkData.TILES_HEIGHT_IN_CHUNK ) {
            throw new IllegalArgumentException("Invalid coordinate range");
        }
    }
}

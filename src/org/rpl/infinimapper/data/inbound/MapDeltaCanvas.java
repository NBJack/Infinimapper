package org.rpl.infinimapper.data.inbound;

import org.rpl.infinimapper.ChunkData;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.WritableDeltaChunk;
import org.rpl.infinimapper.data.management.ChunkCache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a canvas on which chunk data can be written and later flushed.
 * User: Ryan
 * Date: 1/24/13 - 10:22 PM
 */
public class MapDeltaCanvas {

    /**
     * Represents 'active' chunks that have yet to be written.
     */
    private HashMap<ChunkKey, WritableDeltaChunk> workingSet;
    /**
     * Holds information about the realm being written to.
     */
    private Realm realm;
    private ChunkCache chunkCache;
    private int userId = -1;

    public MapDeltaCanvas(Realm targetRealm, ChunkCache cache) {
        this.workingSet = new HashMap<ChunkKey, WritableDeltaChunk>();
        this.chunkCache = cache;
        this.realm = targetRealm;
    }

    /**
     * Write a tile to the appropriate chunk.
     * @param tileX
     * @param tileY
     * @param tileIndex
     */
    public void writeTile ( int tileX, int tileY, int tileIndex) {

        ChunkKey key = new ChunkKey(realm, tileX, tileY);

        // Does it exist, or do we need to make a blank?
        WritableDeltaChunk chunk = workingSet.get(key);
        if ( chunk == null ) {
            // Create
            chunk = new WritableDeltaChunk(userId);
            workingSet.put(key, chunk);
        }

        // Calculate where to put it in the tile.
        int finalX = (ChunkData.TILES_WIDTH_IN_CHUNK + tileX - key.getXcoord()) % ChunkData.TILES_WIDTH_IN_CHUNK;
        int finalY = (ChunkData.TILES_HEIGHT_IN_CHUNK + tileY - key.getYcoord()) % ChunkData.TILES_HEIGHT_IN_CHUNK;

        chunk.setTileDataAt(finalX, finalY, Integer.toString(tileIndex));
    }


    /**
     * Write all tiles held to the cache.
     */
    public void flush() {

        Iterator<Map.Entry<ChunkKey, WritableDeltaChunk>> chunkIterator = workingSet.entrySet().iterator();

        // Write every chunk in the working set to the cache for storage, then remove it from the local set.
        while ( chunkIterator.hasNext() ) {
            Map.Entry<ChunkKey, WritableDeltaChunk> entry = chunkIterator.next();
            chunkCache.updateValue(entry.getKey(), entry.getValue());
            chunkIterator.remove();
        }
    }

    /**
     * Get the number of chunks pending.
     * @return
     */
    public int pendingSize() {
        return workingSet.size();
    }
}

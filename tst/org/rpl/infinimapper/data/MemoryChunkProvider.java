package org.rpl.infinimapper.data;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.management.DataProvider;

import java.util.HashMap;

/**
 * Provides an in-memory chunk storage system.
 * User: Ryan
 * Date: 1/25/13 - 8:47 PM
 */
public class MemoryChunkProvider extends DataProvider<ChunkKey, Chunk> {


    HashMap<ChunkKey, Chunk> chunkMap;

    public MemoryChunkProvider() {
        chunkMap = new HashMap<ChunkKey, Chunk>();
    }

    @Override
    public Chunk getValue(ChunkKey chunkKey) {
        return chunkMap.get(chunkKey);
    }

    @Override
    public void putValue(ChunkKey chunkKey, Chunk chunk) {
        Validate.notNull(chunkKey);
        Validate.notNull(chunk);
        chunkMap.put(chunkKey, chunk);
    }

    public HashMap<ChunkKey, Chunk> getMap() {
        return chunkMap;
    }
}

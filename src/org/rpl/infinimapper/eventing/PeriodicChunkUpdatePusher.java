package org.rpl.infinimapper.eventing;

import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.management.ChunkCache;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Periodically sends out collected updates while looking up the respective
 * {@link Chunk}s involved.
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 3/2/14
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeriodicChunkUpdatePusher extends PeriodicUpdatePusher<String, ChunkKey, Chunk> {

    @Autowired ChunkCache chunkCache;

    Logger log = Logger.getLogger(PeriodicChunkUpdatePusher.class.getName());

    /**
     * {@inheritDoc}
     */
    public PeriodicChunkUpdatePusher(UpdateCollector<ChunkKey> updateCollector, long updateInterval) {
        super(updateCollector, updateInterval);
    }

    /**
     * {@inheritDoc}
     */
    public PeriodicChunkUpdatePusher(UpdateCollector<ChunkKey> updateCollector) {
        super(updateCollector, MS_BETWEEN_UPDATES);
    }

    /**
     * Generate a list of chunks using the provided list of keys.
     * @param in the key list.
     * @return A list of chunks.
     */
    @Override
    protected List<Chunk> generateOutput(List<ChunkKey> in) {
        List<Chunk> chunkList = new ArrayList(in.size());
        for ( ChunkKey key : in) {
            // Get the chunk from the cache. Note that nulls can be returned
            // here, as it is possible in the eventual consistency nature of
            // this design to return values that don't yet exist.
            Chunk chunk = chunkCache.getValue(key);
            if (chunk != null) {
                chunkList.add(chunk);
            } else {
                // Looks like something was waiting to be notified before
                // we got a chance to store it consistently.
                log.warning("ANOMALY: The key " + key.toString() + " returned a null chunk upon fetch.");
            }
        }
        return chunkList;
    }
}

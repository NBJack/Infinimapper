package org.rpl.infinimapper.eventing;

import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * This collector is responsible for accepting changed chunks and transforming
 * them into chunks we can send back.
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 2/28/14
 * Time: 11:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkUpdateCollector extends UpdateCollector<ChunkKey> {

    @Autowired
    private org.rpl.infinimapper.data.management.ChunkCache chunkCache;

    public List<Chunk> getUpdates() {
        // Get the list of updates
        List<ChunkKey> changedKeys = grabChanges();
        // Convert it into a list of actual chunks
        List<Chunk> changedChunks = new ArrayList<Chunk>(changedKeys.size());
        for (ChunkKey key : changedKeys) {
            changedChunks.add(chunkCache.getValue(key));
        }

        return changedChunks;
    }


}

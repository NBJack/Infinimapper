package org.rpl.infinimapper.data.management;

import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.ChunkData;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkDelta;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.Realm;

public class ChunkCache extends BackgroundFlushCache<ChunkKey, Chunk, ChunkDelta> {

    /**
     * We need realm information about each of these chunks.
     */
    private RealmCache realmProvider;

	public ChunkCache(DataProvider<ChunkKey, Chunk> provider, boolean startBackgroundFlush, RealmCache cache) {
		super(provider, startBackgroundFlush);

        Validate.notNull(cache, "A realm cache provider is necessary for Chunk creation.");
        realmProvider = cache;
	}

	/**
	 * Provides an empty chunk. There's a LOT to do here for clarity and
	 * robustness. Dimensions of a chunk are hard-coded for now.
	 * 
	 * @param key
	 * @return
	 */
	protected Chunk getEmptyChunk(ChunkKey key) {
		Chunk chunk = new Chunk(key);
        // Grab the realm
        Realm realm = realmProvider.getValue(key.getRealmid());

		// TODO: Query about the realm
		String [] data = new String[ChunkData.TILES_WIDTH_IN_CHUNK * ChunkData.TILES_HEIGHT_IN_CHUNK];
		Arrays.fill(data, Integer.toString(realm.getDefaulttile()));
		chunk.setTiles(data);
		return chunk;
	}

	/**
	 * {@inheritDoc} This cache style actually provides an empty chunk.
	 */
	@Override
	protected Chunk handleEmptyWrite(ChunkKey key) {
		return getEmptyChunk(key);
	}

}

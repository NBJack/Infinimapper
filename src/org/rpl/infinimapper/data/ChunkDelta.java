package org.rpl.infinimapper.data;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.ChunkData;

/**
 * A simple representation of a chunk's changes. Provides the primary mechanisms
 * along with a few statistics.
 * 
 * @author Ryan
 * 
 */
public class ChunkDelta {

	/**
	 * When found, indicates a 'transparent' tile.
	 */
	public static final String TILE_TRANSPARENT = "-2";

	protected String[] tileData;
    protected int userid;


	public ChunkDelta(int userid, String rawData) {
		Validate.notEmpty(rawData);
		this.userid = userid;
		this.tileData = rawData.split(",");
	}

	public ChunkDelta(int userid, String[] tileData) {
		Validate.notEmpty(tileData);
		this.userid = userid;
		this.tileData = tileData;
	}

	public int getUserId() {
		return userid;
	}

	public String[] getTileData() {
		return tileData;
	}

	/**
	 * Get the number of tiles changed (those that aren't transparent).
	 * 
	 * @return The number of tiles altered.
	 */
	public int getChangedCount() {
		int count = 0;
		for (String tile : tileData) {
			if (!TILE_TRANSPARENT.equals(tile)) {
				count++;
			}
		}
		return count;
	}
}

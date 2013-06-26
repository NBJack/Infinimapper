package org.rpl.infinimapper;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Assimilates a list of realms into a virtual 'giant map' that can be used to
 * output a map one tile at a time. It enables us to 'stream' from the chunks
 * and treat it as if it were a single map
 * 
 * @author rplayfield
 * 
 */
public class RealmAssimilator {

	/**
	 * A collection of all chunks, randomly accessible.
	 */
	ArrayList<ChunkData> chunks;
	int defaultTile;
	long boundLeft, boundTop, boundRight, boundBottom; // A complete set of
														// boundaries made from
														// all chunks

	int mapWidthInChunks;
	int mapHeightInChunks;

	int gidOffset; // The tile offset to use for references

	/**
	 * The total number of all tiles across the map, including tiles from
	 * non-existant chunks within the boundaries.
	 */
	long totalTileCount;
	int tileCursorX; // The current X tile coordinate of a chunk (relative)
	int tileCursorY; // The current Y tile coordinate of a chunk (relative)
	int mapRowCursor; // The current map row we are on, in chunk coordinates
	int rowIndexCursor; // The current column we are on of current row
	int chunkArrayCursor; // Where we are in the COMPLETE chunks array list

	long tilesPerRow; // How many tiles we have per row in the map
	long tilesPerColumn; // How many tiles we have per column in the map
	int chunksPerRow; // How many chunks we need per row
	int chunksPerColumn; // How many chunks we need per column
	int lastChunkCoordinateRow; // The very last row number in
								// chunk-coordinates, based on boundBottom /
								// number of tiles in a chunk's height

	// TODO: Devise a better way to do rows

	ChunkData[] currentRow;

	public RealmAssimilator(Collection<ChunkData> realmChunks, int defaultTile,
			Rectangle bounds) {
		/**
		 * Assume, for now, chunks are already in a proper order. TODO: Replace
		 * this with an on-demand list
		 */
		this.chunks = new ArrayList<ChunkData>(realmChunks);
		this.defaultTile = defaultTile;
		this.gidOffset = 1;

		System.out.println("Chunks provided to assimilator: "
				+ this.chunks.size());

		if (bounds == null) {
			findBoundariesInChunks();
		} else {
			// Use provided boundary rectangle
			boundLeft = bounds.x;
			boundTop = bounds.y;
			boundRight = bounds.x + bounds.width - 1;
			boundBottom = bounds.y + bounds.height - 1;
		}

		calculateBoundaries();

		resetCursor();
	}

	/**
	 * Automatically calculate the boundaries given the avaialble chunks.
	 */
	private void findBoundariesInChunks() {
		// Initialize

		boundLeft = Integer.MAX_VALUE;
		boundRight = Integer.MIN_VALUE;
		boundTop = Integer.MAX_VALUE;
		boundBottom = Integer.MIN_VALUE;

		// Run through every chunk and determine how the boundaries are
		// affected.

		for (ChunkData dat : chunks) {
			// Calculate the boundaries of each chunk. Note that we only have
			// the top-left
			// coordinates of each chunk; calculate the bottom-right coordinate
			// by adding
			// the total number of tiles in a chunk in either dimension - 1.

			boundLeft = Math.min(boundLeft, dat.getLeft());
			boundRight = Math.max(boundRight, dat.getRight());
			boundBottom = Math.max(boundBottom, dat.getBottom());
			boundTop = Math.min(boundTop, dat.getTop());
		}

	}

	private void calculateBoundaries() {
		// TODO: Debug data.

		// Calculate the total number of tiles present in the virtual map.

		tilesPerRow = Math.abs(boundRight - boundLeft) + 1;
		tilesPerColumn = Math.abs(boundBottom - boundTop) + 1;

		totalTileCount = tilesPerRow - tilesPerColumn;

		// Calculate the maximum number of chunks in a row

		chunksPerRow = (int) (tilesPerRow / ChunkData.TILES_WIDTH_IN_CHUNK);
		chunksPerColumn = (int) (tilesPerColumn / ChunkData.TILES_HEIGHT_IN_CHUNK);

		lastChunkCoordinateRow = (int) (boundBottom / ChunkData.TILES_HEIGHT_IN_CHUNK);

		// Pre-allocate the row we use to manage the cursor

		currentRow = new ChunkData[chunksPerRow];
	}

	public void resetCursor() {
		this.tileCursorX = 0;
		this.tileCursorY = 0;
		this.rowIndexCursor = 0;
		this.chunkArrayCursor = 0;

		// The current map row is the upper coordinate in the Y-axis divided by
		// the number of tiles in a chunk

		this.mapRowCursor = (int) (boundTop / ChunkData.TILES_HEIGHT_IN_CHUNK);

		fillCurrentCursorRow();
	}

	/**
	 * The easiest way right now to create a map is to 'expand' the chunks
	 * row-by-row into a fixed-size array and fill it out with each member,
	 * based on its' coordinates in the world. Sweep from left to right; if we
	 * find a chunk at the cursor, assign it to its' respective point in the
	 * array. Otherwise, store a null there.
	 * 
	 */
	private void fillCurrentCursorRow() {
		ChunkData c;
		int chunkRowCursor;

		// Fill our current row

		chunkRowCursor = (int) (boundLeft / ChunkData.TILES_WIDTH_IN_CHUNK);
		if (chunkArrayCursor >= chunks.size()) {
			c = null;
		} else {
			c = chunks.get(chunkArrayCursor);
		}

		for (int i = 0; i < chunksPerRow; i++) {
			// See if our current chunk is at the same tile Y-coordinate and it
			// matches our
			// current chunk-coordinate row. TODO: Do this faster by just
			// skipping across the row array since we know how many are between
			// each
			// real chunk.

			if (c != null && c.getChunkCoordinateY() == mapRowCursor
					&& c.getChunkCoordinateX() == chunkRowCursor) {
				// There is a chunk at this coordinate; store that chunk here.

				currentRow[i] = c;

				// Move to the next chunk in our array

				chunkArrayCursor++;

				if (chunkArrayCursor < chunks.size())
					c = chunks.get(chunkArrayCursor);
				else
					c = null;

			} else {

				// No chunk at this point; assign null to this position.

				currentRow[i] = null;
			}

			// Advance the chunk row cursor separately.

			chunkRowCursor++;
		}
	}

	/**
	 * Are we at the very beginning of the map?
	 * 
	 * @return
	 */
	public boolean isAtBeginningOfMap() {
		return rowIndexCursor == 0 && tileCursorX == 0 && tileCursorY == 0;
	}

	/**
	 * Have our cursors advanced off the end of the map?
	 * 
	 * @return
	 */
	public boolean isAtEndOfMap() {
		return (mapRowCursor > lastChunkCoordinateRow);
	}

	/**
	 * Returns true when we've either first reset or have started a brand new
	 * row.
	 * 
	 * @return
	 */
	public boolean startedNewRow() {
		return rowIndexCursor == 0 && tileCursorX == 0;
	}

	/**
	 * Retrieves the tile at the current cursor
	 * 
	 * @return
	 */
	public int getCurrentTile() {
		ChunkData c;

		c = currentRow[rowIndexCursor];

		if (c == null)
			return defaultTile + gidOffset;

		return Integer.parseInt(c.getTileDataAt(tileCursorX, tileCursorY))
				+ gidOffset;
	}

	/**
	 * Advances to the next tile of the map, left to right, top to bottom.
	 * Returns true if more data is available, false if we've reached the end of
	 * the map.
	 * 
	 */
	public boolean next() {

		// Advance the cursors

		tileCursorX++;

		if (tileCursorX >= ChunkData.TILES_WIDTH_IN_CHUNK) {
			// Reset X coordinate

			tileCursorX = 0;

			// Move to the next chunk

			rowIndexCursor++;

			if (rowIndexCursor >= chunksPerRow) {
				// Reset our row index cursor to the beginning

				rowIndexCursor = 0;

				// Advance Y coordinate

				tileCursorY++;

				if (tileCursorY >= ChunkData.TILES_HEIGHT_IN_CHUNK) {
					// Reset Y cursor

					tileCursorY = 0;

					// Advance the row cursor

					mapRowCursor++;

					if (mapRowCursor > lastChunkCoordinateRow) {
						// We're done. Return false.

						return false;

					} else {

						// Fill out the next row

						fillCurrentCursorRow();
					}
				}
			}
		}

		return true;
	}

	/**
	 * Retrieves how big the map is in tiles, width-wise.
	 * 
	 * @return
	 */
	public long getWidthInTiles() {
		return tilesPerRow;
	}

	/**
	 * Retrieves how big the map in is tiles, height-wise.
	 * 
	 * @return
	 */
	public long getHeightInTiles() {
		return tilesPerColumn;
	}
}

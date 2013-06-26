package org.rpl.infinimapper.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * provides relevant data about a map and gives contex to data on it.
 * 
 * @author rplayfield
 * 
 */
public class MapContext {

	private int rootId;
	private Rectangle bounds;
	private List<TilesetData> tilesets;

	private List<Layer> layers;

	/**
	 * Creates a new map context.
	 * 
	 * @param rootId
	 *            A valid map ID. Must be > 0.
	 * @param bounds
	 *            The boundaries of the map. Cannot be null.
	 * @param tilesets
	 *            The tilesets. Must be at least one entry. The first tileset
	 *            will establish the properties used by the entire context.
	 * @param layers
	 *            The layers of the map. Must be at least one entry.
	 */
	public MapContext(int rootId, Rectangle bounds, List<TilesetData> tilesets, List<Layer> layers) {
		Validate.notNull(bounds);
		Validate.notEmpty(tilesets);
		Validate.notEmpty(layers);
		if (rootId <= 0) {
			throw new IllegalArgumentException("A valid root ID must be greather than zero");
		}
		// Assignment
		this.rootId = rootId;
		this.bounds = bounds;
		this.tilesets = tilesets;
		this.layers = layers;
	}

	/**
	 * Used to create coordinates that are oriented to topLeft as Zero despite
	 * actual position.
	 */
	private Point topLeftCoordinate;

	private int getTileWidth() {
		return tilesets.get(0).getTileWidth();
	}

	private int getTileHeight() {
		return tilesets.get(0).getTileHeight();
	}

	/**
	 * 'Normalizes' the position of an object from "absolute space" coordinates
	 * (the global map) to the relative map coordinates (the rendered map). The
	 * tile position must be provided in chunk-coordinates, not pixel
	 * coordinates.
	 * 
	 * @param tilePositionX
	 * @param tilePositionY
	 * @param localObjectX
	 * @param localObjectY
	 * @return
	 */
	public Point calculatedObjectPosition(int tilePositionX, int tilePositionY, int localObjectX, int localObjectY) {
		// For each coordinate: Multiply the tile position by the number of
		// pixels per tile, add it to the object's coordinate relative to the
		// upper-left corner of the tile, then subtract the coordinate of the
		// upper-left map corner.
		return new Point(tilePositionX * getTileWidth() + localObjectX - topLeftCoordinate.x, tilePositionY
				* getTileHeight() + localObjectY - topLeftCoordinate.y);
	}
}

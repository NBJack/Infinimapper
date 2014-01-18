package org.rpl.infinimapper.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.j256.ormlite.field.DatabaseField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.management.Incrementable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "chunks")
public class Chunk implements Incrementable<Chunk, ChunkDelta>, Identable<ChunkKey> {
    private String[] data;
    @DatabaseField private int userid;
    @DatabaseField private long lastUpdate;

    @DatabaseField private ChunkKey id;

	/**
	 * Default constructor. Creates a new identity-less Chunk.
	 */
	Chunk() {

	}

	/**
	 * Creates a new chunk using the specified key (which will be copied).
	 * 
	 * @param key The key to use. Should not be null.
	 */
	public Chunk(ChunkKey key) {
		this.id = new ChunkKey(key.getXcoord(), key.getYcoord(), key.getRealmid());
		this.data = new String[0];
		this.userid = -1;
		this.lastUpdate = 0;
		setData("0");
	}

	public Chunk(int realmid, int xcoord, int ycoord) {
		this(new ChunkKey(xcoord, ycoord, realmid));
	}

	/**
	 * Bean setter for id. Should not be used outside of bean setting.
	 * 
	 * @param key
	 */
	public void setId(ChunkKey key) {
		this.id = key;
	}

	@Id
	public ChunkKey getId() {
		return this.id;
	}

	/**
	 * Sets the tile information. The tiles must be represented properly as a
	 * comma-delineated list of either positive numbers or -1.
	 * 
	 * @param data The data to parse. Cannot be null or not a comma-delineated
	 *            list.
	 * @throws IllegalArgumentException if unexpected data was found.
	 */
	public void setData(String data) {
		String[] parsedData = data.split(",");
		for (int index = 0; index < parsedData.length; index++) {
			String s = parsedData[index];
			// Must be non-empty, -1, or a number without decimals.
			if (!(s.equals("-1") || StringUtils.isNumeric(s)) || s.isEmpty())
				throw new IllegalArgumentException("Unexpected Tile Data: '" + s + "' at array index " + index);
		}
		// Everything clears, do the assignment
		this.data = parsedData;
	}

    @Column(name = "data")
	public String getData() {
		return StringUtils.join(data, ',');
	}

	public int getUserId() {
		return userid;
	}

	public void setUserId(int userid) {
		this.userid = userid;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Transient
	@JsonIgnore
	public String[] getTiles() {
		return this.data;
	}

	/**
	 * Explicitly sets the tile data.
	 * 
	 * @param tiles
	 */
	@Transient
	@JsonIgnore
	public void setTiles(String[] tiles) {
		this.data = tiles;
	}

	@Transient
	@JsonIgnore
	public boolean isValid() {
		if (userid < 0)
			return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param delta
	 * @return
	 */
	@Override
	public Chunk applyDelta(ChunkDelta delta) {
		Validate.notNull(delta);
		String[] tileDiff = delta.getTileData();
		Validate.isTrue(tileDiff.length == this.data.length);

		// Construct a clone, then update the data with the delta
		Chunk revisedChunk = new Chunk(id.getXcoord(), id.getYcoord(), id.getRealmid());
		revisedChunk.userid = delta.getUserId();
		revisedChunk.data = new String[this.data.length];
		revisedChunk.lastUpdate = System.currentTimeMillis();
		for (int i = 0; i < revisedChunk.data.length; i++) {
			String tile = tileDiff[i];
			if (ChunkDelta.TILE_TRANSPARENT.equals(tile)) {
				revisedChunk.data[i] = this.data[i];
			} else {
				revisedChunk.data[i] = tile;
			}
		}

		// Return the revised copy
		return revisedChunk;
	}

    @Override
    public ChunkKey getID() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != null;
    }

    /**
	 * Erase this chunk according to its parent data.
	 * 
	 * @param parent
	 */
	/*
	 * public void erase(Realm parent) { Validate.notNull(parent);
	 * Validate.isTrue(parent.getId() == this.getId().getRealmid());
	 * 
	 * // Generate the data setData(parent.getBlankData()); }
	 */
}

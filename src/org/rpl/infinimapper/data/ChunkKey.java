package org.rpl.infinimapper.data;

import java.awt.*;
import java.io.Serializable;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.Validate;

/**
 * Chunks are keyed by their coordinates and the realm they are associated with.
 * NOTE: While it would be fantastic to use the Realm directly as part of the
 * key (since Hibernate supports it), this would cause trouble for serialization
 * to and from JSON. To that end, the realm ID must remain the same.
 * 
 * @author Ryan
 * 
 */
@Embeddable
public class ChunkKey implements Serializable {
	/**
	 * Generated serial ID.
	 */
	private static final long serialVersionUID = -4059846185964739828L;
	private int xcoord;
	private int ycoord;
	private int realmid;

	@JsonCreator
	public ChunkKey(@JsonProperty("xcoord") int xcoord, @JsonProperty("ycoord") int ycoord,
			@JsonProperty("realmid") int realmid) {
		this.xcoord = xcoord;
		this.ycoord = ycoord;
		this.realmid = realmid;
	}

    /**
     * Constructs a new key based on tile-based coordinates in a particular realm.
     * @param realm The realm in which this is taking place. Cannot be null.
     * @param tileX The tile-based X coordinate of the chunk to determine.
     * @param tileY The tile-based Y coordinate of the chunk to determine.
     */
    public ChunkKey( Realm realm, int tileX, int tileY) {
        Validate.notNull(realm);
        this.realmid = realm.getId();

        // Calculate
        this.xcoord = (int)Math.floor((double) tileX / realm.getTilesWidthInChunk())
                * realm.getTilesWidthInChunk();
        this.ycoord = (int)Math.floor((double) tileY / realm.getTilesHeightInChunk())
                * realm.getTilesHeightInChunk();


     }

	@Override
	public int hashCode() {
		return xcoord ^ ycoord ^ realmid;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkKey)) {
			return false;
		}

		ChunkKey other = (ChunkKey) obj;

		return (other.xcoord == this.xcoord) && (other.ycoord == this.ycoord) && (other.realmid == this.realmid);
	}

	public int getXcoord() {
		return xcoord;
	}

	public void setXcoord(int xcoord) {
		this.xcoord = xcoord;
	}

	public int getYcoord() {
		return ycoord;
	}

	public void setYcoord(int ycoord) {
		this.ycoord = ycoord;
	}

	public int getRealmid() {
		return realmid;
	}

	public void setRealmid(int realmid) {
		this.realmid = realmid;
	}


}
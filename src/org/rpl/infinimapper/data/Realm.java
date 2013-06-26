package org.rpl.infinimapper.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.rpl.infinimapper.ChunkData;
import org.rpl.infinimapper.data.management.Incrementable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.awt.*;

/**
 * Represents information about a realm.
 * User: Ryan
 * Date: 1/12/13
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName="realms")
public class Realm implements Incrementable<Realm,Realm>, Identable<Integer> {


    @DatabaseField( generatedId=true ) private int id;
    @DatabaseField private String name;
    @DatabaseField private String description;
    @DatabaseField private int tileset;
    @DatabaseField private int defaulttile;
    @DatabaseField private int ownerid;
    @DatabaseField private boolean sublayer = false;
    @DatabaseField private boolean publicFlag = true;
    @DatabaseField private int tileWidth = 32;
    @DatabaseField private int tileHeight = 32;

    public static int UNSPECIFIED_ID = -1;

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }


    public Realm() {
        this.id = UNSPECIFIED_ID;
    }

    public Realm( int id ) {
        this.id = id;
    }

    /**
     * The unique identifier of the realm.
     * @return
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * The name of the realm.
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Describes the realm. Things like content license and intent should be specified here.
     * @return
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The root tileset.
     * @return
     */
    public int getTileset() {
        return tileset;
    }

    public void setTileset(int tileset) {
        this.tileset = tileset;
    }

    /**
     * The default tile for 'empty' space.
     */
    public int getDefaulttile() {
        return defaulttile;
    }

    public void setDefaulttile(int defaulttile) {
        this.defaulttile = defaulttile;
    }

    /**
     * The owner, if any.
     * @return
     */
    public int getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(int ownerid) {
        this.ownerid = ownerid;
    }

    public boolean isSublayer() {
        return sublayer;
    }

    public void setSublayer(boolean sublayer) {
        this.sublayer = sublayer;
    }

    public boolean getPublic() {
        return publicFlag;
    }

    public void setPublic(boolean aPublic) {
        publicFlag = aPublic;
    }

    /**
     * This just creates a copy of the entire realm.
     * @param realm
     * @return
     */
    @Override
    public Realm applyDelta(Realm realm) {
        Realm newRealm = new Realm();

        newRealm.defaulttile = realm.defaulttile;
        newRealm.name = realm.name;
        newRealm.description = realm.description;
        newRealm.id = realm.id;
        newRealm.ownerid = realm.ownerid;
        newRealm.publicFlag = realm.publicFlag;
        newRealm.sublayer = realm.sublayer;
        newRealm.tileset = realm.tileset;

        return newRealm;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int getTilesWidthInChunk() {
        return ChunkData.TILES_WIDTH_IN_CHUNK;
    }

    public int getTilesHeightInChunk() {
        return ChunkData.TILES_HEIGHT_IN_CHUNK;
    }

    public Point calculateTileFromWorldCoord( int realX, int realY ) {
        int dx = (int) Math.floor(realX / tileWidth);
        int dy = (int) Math.floor(realY / tileHeight);

        return new Point(dx, dy);
    }

    public int getChunkWidthInPixels() {
        return tileWidth * getTilesWidthInChunk();
    }

    public int getChunkHeightInPixels() {
        return tileHeight * getTilesHeightInChunk();
    }

    /**
     * Calculates a point on a chunk based on real-world coordinates.  This
     * will be based on the upper-left coordinate of the chunk.
     * @param realX
     * @param realY
     * @return
     */
    public Point calculateOffsetOnChunk( int realX, int realY ) {
        int dx = (int) Math.floor(realX % (tileWidth * getTilesWidthInChunk()));
        int dy = (int) Math.floor(realY % (tileHeight * getTilesHeightInChunk()));

        return new Point(dx, dy);
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != UNSPECIFIED_ID;
    }
}

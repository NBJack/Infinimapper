package org.rpl.infinimapper.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents an association of tilesets with realms.
 * User: Ryan
 * Date: 5/19/13 - 10:12 AM
 */
@DatabaseTable(tableName="realmtilesets")
public class TilesetAssignment implements Identable<Integer> {


    public static final int EMPTY_ID = -1;

    @DatabaseField(generatedId = true) private int id;
    @DatabaseField private int realmId;
    @DatabaseField private int tilesetId;
    @DatabaseField private int order;


    public TilesetAssignment() {
        this.id = EMPTY_ID;
    }

    public TilesetAssignment( int realm, int tileset, int order ) {
        this.realmId = realm;
        this.tilesetId = tileset;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * The tileset ID the entry refers to
     */
    public int getTilesetId() {
        return tilesetId;
    }

    public void setTilesetId(int tilesetId) {
        this.tilesetId = tilesetId;
    }

    /**
     * The realm this entry associates with the tileset
     * @return
     */
    public int getRealmId() {
        return realmId;
    }

    public void setRealmId(int realmId) {
        this.realmId = realmId;
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != EMPTY_ID;
    }
}

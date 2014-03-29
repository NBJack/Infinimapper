package org.rpl.infinimapper.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;

/**
 * Establishes information about the tileset assigned to a realm.
 * User: Ryan
 * Date: 2/12/13 - 9:13 PM
 */
@DatabaseTable(tableName="realmtilesets")
public class RealmTileset implements Identable<Integer> {

    public static final int UNASSIGNED_ID = -1;


    @DatabaseField( generatedId=true )
    private int id;
    @DatabaseField private int realmid;
    @DatabaseField private int tilesetid;
    @DatabaseField private int order;

    public RealmTileset () {
        this(-1, -1);
    }

    public RealmTileset (int realmid, int tilesetid) {
        this.id = UNASSIGNED_ID;
        this.realmid = realmid;
        this.tilesetid = tilesetid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRealmid() {
        return realmid;
    }

    public void setRealmid(int realmid) {
        this.realmid = realmid;
    }

    public int getTilesetid() {
        return tilesetid;
    }

    public void setTilesetid(int tilesetid) {
        this.tilesetid = tilesetid;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != UNASSIGNED_ID;
    }


}

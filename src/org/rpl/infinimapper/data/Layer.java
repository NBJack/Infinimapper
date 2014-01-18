package org.rpl.infinimapper.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a layer within a map.
 */
@DatabaseTable(tableName="layerdata")
public class Layer implements Identable<Integer> {

    public static final int UNASSIGNED_ID = -1;


    @DatabaseField( generatedId=true )
	private int id;
    @DatabaseField
	private int realmid;
    @DatabaseField
	private int ordernum;
    @DatabaseField
	private int masterrealmid;
    @DatabaseField
	private boolean defaultvisibility;
    @DatabaseField
	private String name;


    public Layer() {
        this.id = UNASSIGNED_ID;
    };


    public int getRealmid() {
        return realmid;
    }

    public void setRealmid(int realmid) {
        this.realmid = realmid;
    }

    public int getOrdernum() {
        return ordernum;
    }

    public void setOrdernum(int ordernum) {
        this.ordernum = ordernum;
    }

    public int getMasterrealmid() {
        return masterrealmid;
    }

    public void setMasterrealmid(int masterrealmid) {
        this.masterrealmid = masterrealmid;
    }

    public boolean isDefaultvisibility() {
        return defaultvisibility;
    }

    public void setDefaultvisibility(boolean defaultvisibility) {
        this.defaultvisibility = defaultvisibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

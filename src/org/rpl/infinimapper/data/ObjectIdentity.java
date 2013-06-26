package org.rpl.infinimapper.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The identity information of a given object.
 * 
 * @author rplayfield
 * 
 */
@DatabaseTable(tableName="objlib")
public class ObjectIdentity implements Identable<Integer> {

    public static Integer EMPTY_ID = new Integer(-1);

    @DatabaseField( generatedId=true )
	private Integer id;
    @DatabaseField
	private String name;
    @DatabaseField
	private int tilesrc;
    @DatabaseField
	private int imgXOff;
    @DatabaseField
	private int imgYOff;
    @DatabaseField
	private int imgWidth;
    @DatabaseField
	private int imgHeight;
    @DatabaseField
	private String description;
    @DatabaseField
	private String defaultidentity;


	public ObjectIdentity() {
        id = EMPTY_ID;
	}

    /**
     * The generated unique ID.
     * @return
     */
	public int getId() {
		return id;
	}


    public Integer getID() {
        return id;
    }

    public boolean hasId() {
        return id != EMPTY_ID;
    }

	public void setName(String name) {
		this.name = name;
	}

    /**
     * The human readable name of the object.
     * @return
     */
	public String getName() {
		return name;
	}

	public void setTilesrc(int tilesrc) {
		this.tilesrc = tilesrc;
	}

    /**
     * The ID of the sprite for this object in the tileset library.
     * @return
     */
	public int getTilesrc() {
		return tilesrc;
	}

	public void setImgXOff(int imgXOff) {
		this.imgXOff = imgXOff;
	}

	public int getImgXOff() {
		return imgXOff;
	}

	public void setImgYOff(int imgYOff) {
		this.imgYOff = imgYOff;
	}

	public int getImgYOff() {
		return imgYOff;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	public int getImgWidth() {
		return imgWidth;
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public int getImgHeight() {
		return imgHeight;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDefaultidentity(String defaultidentity) {
		this.defaultidentity = defaultidentity;
	}

	public String getDefaultidentity() {
		return defaultidentity;
	}
}

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
    private JsonObject propertySet;
    @DatabaseField(useGetSet = true) private String properties;
    @DatabaseField private int gidStart;
    @DatabaseField private int gidEnd;


    public TilesetAssignment() {
        this.id = EMPTY_ID;
        propertySet = new JsonObject();
    }

    public TilesetAssignment( int realm, int tileset, int order ) {
        this.realmId = realm;
        this.tilesetId = tileset;
        this.order = order;
        properties = "{}";
        propertySet = new JsonObject();
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

    /**
     * Append a new property. If it exists, overwrite it.
     * @param name the name of the property. Cannot be null.
     * @param value the value of the property.
     */
    public void addProperty ( String name, String value ) {
        Validate.notNull(name);
        propertySet.addProperty(name, value);
    }

    /**
     * Add in a specific JsonElement as a property.
     * @param name
     * @param value
     */
    public void addProperty (String name, JsonElement value) {
        Validate.notNull(name);
        propertySet.add(name, value);
    }

    /**
     * Get a property.
     * @param name The name. Cannot be null.
     * @return the value as a string if it exists, null otherwise.
     */
    public String getProperty (String name) {
        Validate.notNull(name);
        JsonElement element = propertySet.get(name);
        if (element != null) {
            element.getAsString();
        }
        return null;
    }

    /**
     * Get the properties of the specified tile.
     * @param index the global ID (gid) of the tile.
     * @return the property set as a {@link JsonObject} if present, nothing otherwise.
     */
    public JsonObject getTileProperties(int index) {
        JsonElement element = propertySet.get("_" + index);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return null;
    }

    /**
     * Retrieves the properties for serialization purposes.
     * @return the properties as a JSON blob.
     */
    public String getProperties() {
        Gson gson = new Gson();
        return gson.toJson(propertySet);
    }

    /**
     * Set the properties via a raw JSON object.
     * @param rawJson the raw JSON. Null resets.
     */
    public void setProperties(String rawJson) {
        if (rawJson != null) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(rawJson);
            if (element instanceof JsonObject) {
                propertySet = (JsonObject) element;
            } else {
                throw new IllegalArgumentException("Only JSON objects (JsonObject) are accepted");
            }
        } else {
            propertySet.entrySet().clear();
        }

    }

    /**
     * Returns the underlying property object responsible for properties.
     * @return the underlying JsonObject. This object should be used with care; misue can corrupt the object.
     * Not thread-safe.
     */
    public JsonObject getPropertiesObject() {
        return propertySet;
    }

    /**
     * Get the global tileset ID start. This is the first GID of the tile held by this tileset.
     * @return
     */
    public int getGidStart() {
        return gidStart;
    }

    /**
     * Sets the first global tileset ID.
     * @param gidStart
     */
    public void setGidStart(int gidStart) {
        this.gidStart = gidStart;
    }

    /**
     * Gets the ending tileset global ID. This it the last ID of the global tileset.
     * @return
     */
    public int getGidEnd() {
        return gidEnd;
    }

    public void setGidEnd(int gidEnd) {
        this.gidEnd = gidEnd;
    }

    public void setTileProperties(int gid, JsonObject propMap) {
        propertySet.add("_" + gid, propMap);
    }
}


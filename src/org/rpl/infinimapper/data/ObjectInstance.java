package org.rpl.infinimapper.data;

import com.google.gson.*;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.Validate;

import java.awt.*;

/**
 * Stores information about an instance of an object.
 * 
 * @author rplayfield
 */
@DatabaseTable(tableName="objects")
public class ObjectInstance implements Identable<Integer> {

    public static final int MISSING_ID = -1;

    public static final String PROPERTY_NAME_KEY = "name";
    public static final String PROPERTY_VALUE_KEY = "value";

    @DatabaseField( generatedId=true )
    private int id;
    @DatabaseField private int tileRealm;
    @DatabaseField private int tileXCoord;
    @DatabaseField private int tileYCoord;
    @DatabaseField private int offsetX;
    @DatabaseField private int offsetY;
    @DatabaseField private int definition;
    @DatabaseField private int width;
    @DatabaseField private int height;
    private JsonArray realProperties;
    @DatabaseField(columnName = "custom", useGetSet=true) private String properties;


    public ObjectInstance () {
        this.realProperties = new JsonArray();
        this.id = MISSING_ID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTileRealm() {
        return tileRealm;
    }

    public void setTileRealm(int tileRealm) {
        this.tileRealm = tileRealm;
    }

    public int getTileXCoord() {
        return tileXCoord;
    }

    public void setTileXCoord(int tileXCoord) {
        this.tileXCoord = tileXCoord;
    }

    public int getTileYCoord() {
        return tileYCoord;
    }

    public void setTileYCoord(int tileYCoord) {
        this.tileYCoord = tileYCoord;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getDefinition() {
        return definition;
    }

    public void setDefinition(int definition) {
        this.definition = definition;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getProperties() {
        // Build the object
        if ( realProperties != null ) {
            Gson gson = new Gson();
            return gson.toJson(realProperties);
        } else {
            return null;
        }
    }

    public JsonArray getPropertyJson() {
        return realProperties;
    }

    public void setProperties(String properties) {
        // TODO: Add a simple check on size here.
        JsonParser reader = new JsonParser();

        JsonArray result = null;
        try {
            JsonElement element = reader.parse(properties);
            if ( !element.isJsonArray() ) {
                result = new JsonArray();
                JsonObject obj = new JsonObject();
                obj.addProperty("name", "value");
                obj.addProperty("value", properties);
                result.add(obj);
            } else {
                result = element.getAsJsonArray();
            }
        } catch (JsonParseException ioex ) {
            // Fallback to just creating a new object and storing the value as a single property.
            result = new JsonArray();
            JsonObject obj = new JsonObject();
            obj.addProperty("name", "value");
            obj.addProperty("value", properties);
            result.add(obj);
        }
        this.realProperties = result;
    }

    /**
     * Append a new property.
     * @param name
     * @param value
     */
    public void addProperty ( String name, String value ) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("value", value);
        realProperties.add(obj);
    }

    public void addProperty ( String name, boolean value ) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("value", value);
        realProperties.add(obj);
    }

    public void addProperty ( String name, int value ) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("value", value);
        realProperties.add(obj);
    }


    @Override
    public Integer getID() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasId() {
        return id != MISSING_ID;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * Assign this chunk to a specific chunk (and realm).
     * @param key The chunk key to use. Cannot be null.
     */
    public void setAssignedChunk ( ChunkKey key ) {
        Validate.notNull(key);
        this.setTileXCoord(key.getXcoord());
        this.setTileYCoord(key.getYcoord());
        this.setTileRealm(key.getRealmid());
    }

    /**
     * Sets up this object instance based on world coorindates and the realm. This
     * involves calculation of which chunk it belongs to, the offset in pixels from
     * the chunk's upper-left coordinate, etc.
     * @param worldX The world x coordinate.
     * @param worldY The world y coordinate.
     * @param realm The realm to use for calculation. Cannot be null.
     */
    public void setPositionBasedOnRealm ( int worldX, int worldY, Realm realm ) {
        Validate.notNull(realm);
        // Calculate which chunk this object belongs to
        Point tileCoord = realm.calculateTileFromWorldCoord(worldX, worldY);
        ChunkKey key = new ChunkKey(realm, tileCoord.x, tileCoord.y);
        this.setAssignedChunk(key);
        // Setup the offset
        //Point worldPoint = realm.calculateTileFromWorldCoord(worldX, worldY);
        this.setOffsetX(worldX % realm.getChunkWidthInPixels());
        this.setOffsetY(worldY % realm.getChunkHeightInPixels());
    }
}

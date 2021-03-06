package org.rpl.infinimapper.data.inbound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.ImageUpload;
import org.rpl.infinimapper.data.*;
import org.rpl.infinimapper.data.management.ChunkCache;
import org.rpl.infinimapper.data.management.meta.MapDataProviders;
import tiled.core.*;
import tiled.core.Map;
import tiled.io.xml.XMLMapTransformer;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * User: Ryan
 * Date: 1/26/13 - 3:57 PM
 */
public class TMXMapImporter {

    private XMLMapTransformer mapTransformer;
    private Map map;
    private Point mapWriteOffset;
    private String name;

    private MapDataProviders mapDataProviders;

    private ArrayList<Realm> realms;
    private ArrayList<Layer> layers;
    private ArrayList<TilesetAssignment> tilesetAssignments;
    private File originPath;
    private HashMap<Integer,java.util.Map<String,String>> tileInfoMap;

    private boolean forceImageImport = false;

    public TMXMapImporter( String filename, Point offset, MapDataProviders mapDataProviders) throws Exception {
        this.mapTransformer = new XMLMapTransformer();
        this.originPath = new File(filename);
        this.map = mapTransformer.readMap(filename);

        this.mapDataProviders = mapDataProviders;
        this.mapWriteOffset = offset;
        this.name = filename;
        this.tileInfoMap = new HashMap<>();
    }

    /**
     * Read the passed map into a 'native' map format. This will proces all layers and objects. Object layers are currently not respected, but all objects will be added to the root layer. It's up to the caller to flush the results from the providers.
     * @param makePublic
     * @param owner
     * @throws MapProcessingException
     */
    public void processMap(boolean makePublic, int owner) throws MapProcessingException {
        // Grab the map properties
        for (java.util.Map.Entry<Object, Object> entry : map.getProperties().entrySet()) {
            // Where does this go???
        }
        // Take care of the child data
        processLayers(makePublic, owner);
        writeObjectsToLayer(getRealms().get(0), getObjects());
        processTilesets(true);
    }


    protected void processLayers(boolean makePublic, int owner) {
        // Construct the basic storage structures
        List<MapLayer> originalLayers = getLayers();
        realms  = new ArrayList<Realm>(originalLayers.size());
        layers = new ArrayList<Layer>(originalLayers.size());
        // Read in each map layer to each realm
        boolean isFirstLayer = true;
        for ( int i = 0; i < originalLayers.size(); i++ ) {
            MapLayer layer = originalLayers.get(i);
            // Skip non map layers
            if ( !(layer instanceof TileLayer) ) {
                continue;
            }
            // Construct the realm
            Realm realm = new Realm();
            if (isFirstLayer) {
                realm.setName(name);
            } else {
                realm.setName(layer.getName());
            }
            realm.setPublic(makePublic);
            realm.setOwnerid(owner);
            realm.setDescription(name);
            // NOTE: At this time, tilesets here do not matter.
            realm.setTileset(7);
            realm.setDefaulttile(Chunk.EMPTY_TILE);
            realm.setSublayer(!isFirstLayer);
            // Add it to the existing data
            mapDataProviders.realms().addEntry(realm);
            if ( !realm.hasId() ) {
                throw new IllegalStateException("Unable to add Realm to database");
            }
            realms.add(realm);
            mapDataProviders.realms().flushCacheChanges();
            // Construct an equivalent layer in the database
            Layer realmLayer;
            realmLayer = new Layer();
            realmLayer.setName(layer.getName());
            realmLayer.setRealmid(realm.getId());
            realmLayer.setMasterrealmid(realms.get(0).getId());
            realmLayer.setOrdernum(i + 1);
            // A layer will default to the visibility specified in the file
            realmLayer.setDefaultvisibility(layer.isVisible());
            layers.add(realmLayer);
            System.out.println("Putting layer " + realmLayer.getID() + ", Realm " + realmLayer.getRealmid() + ", " + realmLayer.getName());
            mapDataProviders.layers().putValue(null, realmLayer);
            // Now, write to that layer
            writeLayerToRealm((TileLayer) layer, realm);
            // Trip the flag for first layer indication
            isFirstLayer = false;
        }

    }

    public List<Realm> getRealms() {
        return realms;
    }

    protected void processTilesets(boolean addIfNotPresent) throws TilesetNotFoundException {
        Validate.notNull(this.realms, "The realms have not been processed yet.");
        Validate.isTrue(this.realms.size() > 0, "There must be at least one realm available.");
        processTilesets(addIfNotPresent, this.realms.get(0).getId());
    }

    /**
     * Process the tileset information available from the TMX file.
     * @param addIfNotPresent
     * @param assignToRealm
     * @throws TilesetNotFoundException
     */
    protected void processTilesets(boolean addIfNotPresent, int assignToRealm) throws TilesetNotFoundException {
        // Try to cross-reference every tileset against an equivalent
        ArrayList<TilesetData> chosenTilesets = new ArrayList<TilesetData>();
        tilesetAssignments = new ArrayList<TilesetAssignment>();
        int tilesetOrder = 0;
        for ( TileSet tileset : map.getTilesets() ) {
            String name = tileset.getName();
            String fileName = tileset.getTilebmpFile();
            TilesetData usedTileset = null;

            // We look for things in the resources that may already have been loaded.
            if ( name.startsWith("tileset") && !forceImageImport ) {
                // Good chance this is a tileset we've worked with already.
                String tilesetId = name.substring("tileset".length());
                int id = Integer.parseInt(tilesetId);
                // Query for the tileset data and use it (if it exists)
                usedTileset = mapDataProviders.tilesets().getValue(id);
            };

            // If there still isn't a tileset we can use, and we were told to do so, it's time to import.
            // TODO: Detect an existing version of the same file (if possible) to prevent duplicates
            if (usedTileset == null && addIfNotPresent) {
                // Load-up the tileset
                // Only relative names are supported.
                File tilesetSource = new File(fileName);
                // Assign that to our map
                int tileID = -1;
                try {
                    tileID = ImageUpload.storeImage(name, "Automatically imported tileset", tileset.getTileWidth(), 1, tilesetSource);
                } catch (Exception ex) {
                    throw new TilesetNotFoundException(name, ex);
                }
                // Now, get the results
                usedTileset = mapDataProviders.tilesets().getValue(tileID);
            }

            // Did we find everything successfully?
            if (usedTileset == null) {
                throw new TilesetNotFoundException(tileset.getName());
            }

            // Setup the association of the tileset with this map
            TilesetAssignment assignment = new TilesetAssignment();
            assignment.setOrder(tilesetOrder++);
            assignment.setRealmId(assignToRealm);
            assignment.setTilesetId(usedTileset.getId());
            assignment.setGidStart(tileset.getFirstGid());
            assignment.setGidEnd(tileset.getMaxTileId() + tileset.getFirstGid());

            // Now, process the tiles.
            for (int i = 0; i <= tileset.getMaxTileId(); i++) {
                Tile t = tileset.getTile(i);
                if (t != null) {
                    JsonObject  propMap = new JsonObject();
                    if (t.getProperties().size() > 0) {
                        // Build a new map
                        for (java.util.Map.Entry<Object, Object> entry : t.getProperties().entrySet()) {
                            // Put every non-null value into our own property listing.
                            if (entry.getValue() != null) {
                                propMap.addProperty(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }
                        // Associate this with the tile ID
                        assignment.setTileProperties(t.getGid(), propMap);
                    }
                }
            }
            chosenTilesets.add(usedTileset);

            // Tilesets can have properties; record them now
            mapDataProviders.tilesetAssignments().addValue(assignment);
            tilesetAssignments.add(assignment);

        }

    }


    /**
     * Retrieve all of the layers assigned to this map.
     * @return
     */
    public List<MapLayer> getLayers() {

        List<MapLayer> layers = new ArrayList<MapLayer>(map.getTotalLayers());
        ListIterator<MapLayer> layerIt = map.getLayers();
        while ( layerIt.hasNext() ) {
            MapLayer layer = layerIt.next();
            layers.add(layer);
        }
        return layers;
    }


    /**
     * Get a list of all objects from all layers.
     * @return
     */
    public List<MapObject> getObjects() {
        List<MapObject> objects = new ArrayList<MapObject>();
        for ( MapLayer layer : getLayers() ) {
            if (layer instanceof ObjectGroup) {
                ObjectGroup objLayer = (ObjectGroup) layer;

                Iterator<MapObject> itObj = objLayer.getObjects();
                while ( itObj.hasNext() ) {
                    objects.add(itObj.next());
                }
            }
        }

        return objects;
    }



    protected void writeLayerToRealm ( TileLayer layer, Realm realm) {
        // Create a new canvas for this layer
        MapDeltaCanvas deltaCanvas = new MapDeltaCanvas(realm, mapDataProviders.chunks());


        for ( int x = 0; x < layer.getWidth(); x++ ) {
            for ( int y = 0; y < layer.getHeight(); y++) {
                tiled.core.Tile tile = layer.getTileAt(x, y);
                // No tiles means write nothing
                if ( tile != null ) {
                    deltaCanvas.writeTile(mapWriteOffset.x + x, mapWriteOffset.y + y,  tile.getGid() - 1);
                }
            }
        }
        deltaCanvas.flush();
    }

    protected void writeObjectsToLayer ( Realm realm, List<MapObject> objects) {
        for ( MapObject obj : objects ) {
            ObjectInstance objInstance = new ObjectInstance();
            // Grab the boundary information
            objInstance.setDefinition(0);
            objInstance.setWidth(obj.getWidth());
            objInstance.setHeight(obj.getHeight());
            objInstance.setName(obj.getType());
            // Setup coordinates adn the chunk
            objInstance.setPositionBasedOnRealm(obj.getX(), obj.getY(), realm);
            // Extract the core properties
            if ( obj.getType() != null ) {
                objInstance.addProperty("__name", obj.getType());

                //objInstance.getPropertyJsonObj().addProperty("__name", obj.getName());
            }
            // Assign the original ID if present
            if ( obj.getProperties().containsKey("__typeId")) {
                int typeId = Integer.parseInt(obj.getProperties().get("__typeId").toString());
                // This is dangerous. If that object type disappears, we're sunk here.
                objInstance.setDefinition(typeId);

            }
            // Extract the child properties
            for (java.util.Map.Entry<Object,Object> entry : obj.getProperties().entrySet()) {
                if ( entry.getValue() != null ) {
                    //objInstance.getPropertyJsonObj().addProperty(entry.getKey().toString(), entry.getValue().toString());
                    objInstance.addProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            // TODO: Check for any special properties used in import
            // Add the object
            mapDataProviders.instances().putValue(null, objInstance);
        }
    }

    /**
     * Set the name used once the map is imported.
     * @param name
     */
    public void setName( String name ) {
        Validate.notNull(name);
        this.name = name;
    }

    /**
     * Reports whether the images found in a map will always be imported.
     * @return True if they will be, false otherwise.
     */
    public boolean isForceImageImport() {
        return forceImageImport;
    }

    /**
     * Sets whether or not images will always be imported.
     * @param forceImageImport if true, all images found will be imported.
     */
    public void setForceImageImport(boolean forceImageImport) {
        this.forceImageImport = forceImageImport;
    }
}

package org.rpl.infinimapper.data.export;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.rpl.infinimapper.ChunkData;
import org.rpl.infinimapper.DBResourceManager;
import org.rpl.infinimapper.DataTools;
import org.rpl.infinimapper.MapDataType;
import org.rpl.infinimapper.RealmAssimilator;
import org.rpl.infinimapper.WorldDB.QuickCon;
import org.rpl.infinimapper.data.*;
import org.rpl.infinimapper.data.management.*;
import org.rpl.util.Base64IntWriter;

import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;

public class MapExport {


    @Autowired
    private ChunkCache chunkCache;
    @Autowired
    private RealmCache realmCache;
    @Autowired
    private LayerDataProvider layerProvider;
    @Autowired
    private ObjectProvider objectDefProvider;
    @Autowired
    private ObjectInstanceProvider objectProvider;
    @Autowired
    private TilesetProvider tilesetProvider;
    @Autowired
    private TilesetAssignmentProvider tilesetAssignmentProvider;



    private static final int FIXED_TILE_WIDTH = 32;
//	private static final String EXPORT_REALM_INFO_REQUEST = "SELECT rlm.name,rlm.description,rlm.tileset,rlm.defaulttile,tile.name,tile.tilecount,tile.tilewidth,tile.defaulttile,tile.usebackground,tile.description,tile.fullwidth,tile.fullheight FROM realms as rlm, tilelib as tile WHERE rlm.id=? AND tile.id=rlm.tileset";
	private static final String EXPORT_GET_TILE_INFO = "SELECT fullwidth, fullheight, border, spacing FROM tilelib WHERE id=?";
	public static final String EXPORT_CHUNK_REQUEST = "SELECT data, xcoord, ycoord FROM chunks WHERE realmid=? ORDER BY ycoord, xcoord";
	public static final String EXPORT_GET_REALM_DIMS = "SELECT MIN(xcoord), MIN(ycoord), MAX(xcoord), MAX(ycoord) FROM chunks WHERE realmid=?";
	public static final String EXPORT_GET_ALL_LAYERS = "SELECT realmid FROM layerdata WHERE masterrealmid=? ORDER BY ordernum";
	public static final String EXPORT_GET_ALL_LAYERS_DETAIL = "SELECT * FROM layerdata WHERE masterrealmid=? ORDER BY ordernum";
	public static final String EXPORT_GET_LAYER_DATA = "SELECT * FROM layerdata WHERE realmid=?";
	public static final String EXPORT_GET_REALM_DIMS_ALL_LAYERS = "SELECT MIN(xcoord), MIN(ycoord), MAX(xcoord), MAX(ycoord) FROM chunks WHERE realmid IN (SELECT realmid FROM layerdata WHERE masterrealmid=?)";
	//public static final String EXPORT_GET_ALL_TILESETS = "select tiles.id, tiles.description, tiles.fullwidth, tiles.fullheight, tiles.border, tiles.spacing FROM tilelib tiles, realms r WHERE r.id=? AND (tiles.id IN (select tilesetid FROM realmtilesets WHERE realmid=r.id));";
	//public static final String EXPORT_GET_TILE_METADATA = "select tileindex, name, value FROM tileproperties WHERE realmid=? AND tilesetindex=? ORDER BY tilesetindex, tileindex";
	public static final String EXPORT_GET_RELEVANT_OBJECT_IDENTITIES = "SELECT * FROM objlib WHERE id IN (SELECT definition FROM objects WHERE tilerealm=? AND deleted=FALSE GROUP BY definition)";
	public static final String EXPORT_GET_OBJECTS_FOR_REALM = "SELECT tilexcoord, tileycoord, offsetx, offsety, custom, definition, width, height FROM objects WHERE tilerealm=? AND deleted=FALSE";

    private Realm masterRealm;

    /**
	 * Create a TMX file with basic encodings that captures the map data.
	 * 
	 * @param realmid
	 * @throws IOException
	 */
	public void processAndExportMapTMX(int realmid, OutputStream rawOut, String fileName, String imagePrefix,
			MapDataType mapDataFormat) throws IOException {
		List<ChunkData> chunks; // All of the chunks in the realm
		PrintWriter out;
		Connection c;
		ResultSet rs;
		PreparedStatement st;
		ChunkData chk;
		int defaultTile;
		RealmAssimilator realmMap;
		Collection<TilesetAssignment> layers;
		Rectangle mapBoundaries;

		c = null;
		rs = null;
		st = null;

        masterRealm = realmCache.getValue(realmid);

		try {

			// Setup the connection

			c = DBResourceManager.getConnection();

			// Retrieve all data about the realm
            Realm realm = realmCache.getValue(realmid);

            if (realm != null) {
                defaultTile = realm.getDefaulttile();
			} else {
				// Realm not found!

				throw new Exception("The realm information for id " + realmid + " was not found.");
			}

			// Grab layer data
            layers = tilesetAssignmentProvider.getAllTilesetsForRealm(realm);

			// Leverage the database to calculate dimensions
			mapBoundaries = findMapBoundariesAllLayers(realmid);
			// Fall-back to single realm queries
			if (mapBoundaries == null) {
				mapBoundaries = MapExport.findMapBoundaries(realmid);
			}
			// Abort if nothing was found at all
			if (mapBoundaries == null) {
				throw new IllegalArgumentException("You need at least one chunk in the realm to export.");
			}

			// Setup our file name

			if (fileName == null)
				fileName = "realm_" + realmid + ".tmx";
			else
				fileName = fileName + ".tmx";

			//
			// Write basic header info
			//
			out = new PrintWriter(rawOut);
			// TODO: Calculate boundaries by just looking at the tiles
			// themselves (in ALL layers) then calculating tile width.
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<map version=\"1.0\" orientation=\"orthogonal\" width=\"" + mapBoundaries.width
					+ "\" height=\"" + mapBoundaries.height + "\" tilewidth=\"32\" tileheight=\"32\">");

			// TODO: Allow tile width and height to be adjustable.
			writeTilesetData(realmid, imagePrefix, out);

			// For each layer, grab data and write it out.
            List<Layer> layerData = layerProvider.getLayersForRealm(realmid);

			for (Layer layer : layerData) {
                int layerID = layer.getRealmid();
				System.out.println("Layer ID: " + layerID);

				//
				// Retrieve a list of chunks, from top-left to bottom-right
				//

				st = c.prepareStatement(MapExport.EXPORT_CHUNK_REQUEST);
				st.setInt(1, layerID);

				rs = st.executeQuery();

				// Read all chunks from the database into memory.

				chunks = new LinkedList<ChunkData>();

				while (rs.next()) {
					// Create chunk object

					chk = new ChunkData(rs.getInt(2), // X coordinate of chunk
							rs.getInt(3), // Y coordinate of chunk
							rs.getString(1) // Map data
					);

					chunks.add(chk);

				}

				// Create a realm assimilator to treat it as a single,
				// contiguous map

				realmMap = new RealmAssimilator(chunks, defaultTile, mapBoundaries);

				//
				// Base layer
				//


				out.println("<layer name=\"" + layer.getName() + "\" width=\"" + realmMap.getWidthInTiles() + "\" height=\""
						+ realmMap.getHeightInTiles() + "\">");

				if (mapDataFormat == MapDataType.TMX_BASE64) {

					out.println("<data encoding=\"base64\">");
					out.flush();
					// Write out the base64 stream
					MapExport.writeMapAsBase64Data(realmMap, rawOut);
					out.println();

				} else {

					out.println("<data encoding=\"csv\">");

					// Write out the first element
					out.print(realmMap.getCurrentTile());
					realmMap.next();

					do {

						if (realmMap.startedNewRow()) {
							if (!realmMap.isAtEndOfMap())
								out.println(",");

							out.print(realmMap.getCurrentTile());

						} else {

							out.print(',');
							out.print(realmMap.getCurrentTile());
						}

					} while (realmMap.next());

				}

				out.println("</data>");
				out.println("</layer>");
				out.flush();

				// Revert default tile selection for subsequent layers to -1
				defaultTile = -1;
			}

			// EXPERIMENTAL: Add a dedicated layer for just object output.
			writeObjectsFromLayer(realmid, out, mapBoundaries);

			// Close up the file

			out.println("</map>");
			out.flush();

		} catch (Exception ex) {
			// All exceptions basically mean we have to stop. Wish I didn't
			// have to do a catch-all, but there's a lot.

			ex.printStackTrace();

		} finally {

			// Safely clean-up database interactions
			DataTools.safeCleanUp(c, st, rs);
		}

	}

	/**
	 * Determine the boundaries of the given realm across all layers.
	 * 
	 * @param realmid The ID of the realm to check.
	 * @return The boundaries if any chunks were found, null otherwise.
	 * @throws SQLException
	 */
	public static Rectangle findMapBoundaries(int realmid) throws SQLException {
		QuickCon con = new QuickCon(MapExport.EXPORT_GET_REALM_DIMS);

		try {
			con.getStmt().setInt(1, realmid);
			ResultSet results = con.query();
			if (results.next()) {
				// Populate the rectangle
				// TODO: Use the realm information directly instead of constants
				Rectangle result = new Rectangle(results.getInt(1), results.getInt(2), results.getInt(3)
						- results.getInt(1) + ChunkData.TILES_WIDTH_IN_CHUNK, results.getInt(4) - results.getInt(2)
						+ ChunkData.TILES_HEIGHT_IN_CHUNK);
				return result;
			} else {
				// Nothing was available; no chunks were there.
				return null;
			}
		} finally {
			con.release();
		}

	}

	/**
	 * Calculate the final boundaries across multiple realms.
	 * 
	 * @param masterrealmid
	 * @return
	 * @throws SQLException
	 */
	public Rectangle findMapBoundariesAllLayers(int masterrealmid) throws SQLException {
		QuickCon con = new QuickCon(MapExport.EXPORT_GET_REALM_DIMS_ALL_LAYERS);

		try {
			con.getStmt().setInt(1, masterrealmid);
			ResultSet results = con.query();
			if (results.next()) {
				// Make sure we have valid results. If we were to grab on a
				// NULL, we'd get back zeros.
				if (null == results.getObject(1)) {
					return null;
				}
				// Populate the rectangle
				// TODO: Use the realm information directly instead of constants
				Rectangle result = new Rectangle(results.getInt(1), results.getInt(2), results.getInt(3)
						- results.getInt(1) + ChunkData.TILES_WIDTH_IN_CHUNK, results.getInt(4) - results.getInt(2)
						+ ChunkData.TILES_HEIGHT_IN_CHUNK);
				return result;
			} else {
				// Nothing was available; no chunks were there.
				return null;
			}
		} finally {
			con.release();
		}
	}

	public static List<Layer> getAllLayerData_old(int realmid) throws SQLException, InstantiationException,
			IllegalAccessException {
		QuickCon con = new QuickCon(MapExport.EXPORT_GET_ALL_LAYERS_DETAIL);

        con.getStmt().setInt(1, realmid);
        ResultSet results = con.query();

        List<Layer> layers = SimpleDeserializer.deserializeFromSet(results, Layer.class);

        return layers;

	}


	/**
	 * Prints all of the XML responsible for expressing a complete tileset for a
	 * map. Includes queries for all images, tile specific attributes, etc.
	 * 
	 * @param rootRealmId
	 * @throws SQLException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 */
	public void writeTilesetData(int rootRealmId, String prefix, Writer out) throws SQLException,
			XMLStreamException, FactoryConfigurationError {

		XMLStreamWriter xmlOut = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
		// Grab a list of all tiles needed by a realm.


        // Setup the query and allow the native serialization process to
        // work.
        List<TilesetAssignment> assignments = tilesetAssignmentProvider.getAllTilesetsForRealm(masterRealm);

        // Write out each tileset
        int runningTileCount = 1;

        // Run through every assignment of a tileset.
        for (TilesetAssignment assignment: assignments) {
            // Grab that tileset's data
            TilesetData tileset = tilesetProvider.getValue(assignment.getTilesetId());
            xmlOut.writeStartElement("tileset");
            xmlOut.writeAttribute("name", "tileset" + tileset.getId());
            xmlOut.writeAttribute("firstgid", "" + runningTileCount);
            xmlOut.writeAttribute("tilewidth", "" + tileset.getTileWidth());
            xmlOut.writeAttribute("tileheight", "" + tileset.getTileHeight());
            xmlOut.writeAttribute("space", "" + tileset.getSpacing());
            xmlOut.writeAttribute("margin", "" + tileset.getBorder());

            xmlOut.writeStartElement("image");
            xmlOut.writeAttribute("source", "./" + prefix + tileset.getId() + ".png");
            xmlOut.writeEndElement();

            // Write every individual tile properties if available
            System.out.println("Writing tile properties for " + assignment.getGidStart() + " to " + assignment.getGidEnd());

            for (int tileIndex = assignment.getGidStart(); tileIndex <= assignment.getGidEnd(); tileIndex++) {
                JsonObject tileProps = assignment.getTileProperties(tileIndex);
                System.out.println("  Properties for " + tileIndex + " = " + tileProps);
                if (tileProps != null) {
                    xmlOut.writeStartElement("tile");
                    // The id should be the LOCAL ID, starting at one instead of zero, for properties.
                    xmlOut.writeAttribute("id", Integer.toString(tileIndex - assignment.getGidStart() + 1));
                    xmlOut.writeStartElement("properties");
                    // Now, for each property, write out the property
                    for (Entry<String, JsonElement> entry: tileProps.entrySet()) {
                        xmlOut.writeStartElement("property");
                        xmlOut.writeAttribute("name", entry.getKey());
                        xmlOut.writeAttribute("value", entry.getValue().getAsString());
                        xmlOut.writeEndElement();
                    }
                    xmlOut.writeEndElement();
                    xmlOut.writeEndElement();
                }
            }


            // End the tileset entry
            xmlOut.writeEndElement();

            // Calculate the number of tiles and advance the counter. In the
            // future, this may be pre-calculated.
            runningTileCount += tileset.calculateTileCount();
        }
        xmlOut.writeComment("No tiles should be greater than " + runningTileCount);


		xmlOut.flush();
		xmlOut.close();
	}

	/**
	 * Write-out objects from a specific realm. Includes all tags necessary to
	 * identify them as from a layer.
	 * 
	 * @param realmid the realm ID we're looking to write the objects from.
     * @param out the writer we're outputing the XML to.
     * @param boundaries the boundaries we're keeping the map to. Used in calculating offsets of objects to the map's
     *                   local coordinates.
     * @throws IOException if something goes wrong with communication/writing output.
	 * @throws XMLStreamException if something goes wrong writing out the XML.
	 * @throws SQLException if something goes wrong retrieving the data.
	 */
	public void writeObjectsFromLayer(int realmid, Writer out, Rectangle boundaries) throws IOException,
			XMLStreamException, SQLException  {

        // Start XML output
		XMLStreamWriter xmlOut = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
		// Start the layer
		xmlOut.writeStartElement("objectgroup");
		xmlOut.writeAttribute("name", "entities");
		xmlOut.writeAttribute("width", Integer.toString(boundaries.width));
		xmlOut.writeAttribute("height", Integer.toString(boundaries.height));
		xmlOut.writeAttribute("color", "#FF2222");

		// Grab all of the objects in the realm and write them out
        List<ObjectInstance> objects = objectProvider.getObjectsInRealm(realmid);

        for (ObjectInstance obj : objects) {

            // Grab the identity it may have.
            ObjectIdentity identity = objectDefProvider.getValue(obj.getDefinition());

            xmlOut.writeStartElement("object");
            if (identity != null) {
                if (identity.getDefaultidentity() != null) {
                    xmlOut.writeAttribute("type", identity.getDefaultidentity());
                } else if (identity.getName() != null) {
                    xmlOut.writeAttribute("type", identity.getName());
                }
            } else {
                System.out.println("WARNING! Can't find the identity " + obj.getDefinition() + " for object " + obj.getId() + " (" + obj.getName() + ")");
                xmlOut.writeAttribute("type", obj.getName());
            }

            // TODO: Create a map context that can translate object
            // coordinates into an absolute coordinate relative to the
            // upper-left corner of the map.
            int tileCoordX = obj.getTileXCoord() - boundaries.x;
            int tileCoordY = obj.getTileYCoord() - boundaries.y;
            int offsetX = obj.getOffsetX() + tileCoordX * FIXED_TILE_WIDTH;
            int offsetY = obj.getOffsetY() + tileCoordY * FIXED_TILE_WIDTH;
            xmlOut.writeAttribute("x", Integer.toString(offsetX));
            xmlOut.writeAttribute("y", Integer.toString(offsetY));

            // Use the identity's width and height if available (not sure if we should keep this...)
            // TODO: Find out if hanging on to the width and height of the identity is a good idea
            if (identity == null) {
                xmlOut.writeAttribute("width", Integer.toString(obj.getWidth()));
                xmlOut.writeAttribute("height", Integer.toString(obj.getHeight()));
            } else {
                xmlOut.writeAttribute("width", Integer.toString(identity.getImgWidth()));
                xmlOut.writeAttribute("height", Integer.toString(identity.getImgHeight()));
            }
            // Write out the attributes of each object if it isn't
            // empty.
            System.out.println(obj.getProperties());
            Map<String, String> properties = extractPropertyMapFromJSON(obj.getProperties());
            if (!properties.isEmpty()) {
                xmlOut.writeStartElement("properties");
                for (Entry<String, String> property : properties.entrySet()) {
                    xmlOut.writeStartElement("property");
                    xmlOut.writeAttribute("name", property.getKey());
                    xmlOut.writeAttribute("value", property.getValue());
                    xmlOut.writeEndElement();
                }
                xmlOut.writeEndElement();
            }

            // Flush and wrap up the output
            xmlOut.writeEndElement();
            xmlOut.flush();
        }

		xmlOut.writeEndElement();
	}

	/**
	 * Simple static method to output data from the map (whether actual tiles or
	 * overlay) to the output stream specified encoded in Base64.
	 * 
	 * @param map The realm assimilator to use to gather map information.
	 * @param out Where to write the data about the map.
	 * @throws IOException
	 */
	public static void writeMapAsBase64Data(RealmAssimilator map, OutputStream out) throws IOException {
		OutputStream b64Out = new Base64IntWriter(out);

		do {
			Base64IntWriter.writeInt(map.getCurrentTile(), b64Out);
		} while (map.next());

		b64Out.close();
		out.flush();
	}

	/**
	 * Grabs a list of all identities (at this present time) of known objects.
	 * Note that, unless writes to the realm's data are frozen, it's always
	 * possible for a future write to come through after this list is generated
	 * and use an unknown object.
	 * 
	 * @param realmid The realm of interest
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static List<ObjectIdentity> getAllIdentitiesForRealm(int realmid) throws SQLException,
			InstantiationException, IllegalAccessException {
		QuickCon connection = new QuickCon(MapExport.EXPORT_GET_RELEVANT_OBJECT_IDENTITIES);

		try {
			connection.getStmt().setInt(1, realmid);
			ResultSet results = connection.query();
			List<ObjectIdentity> identities = SimpleDeserializer.deserializeFromSet(results, ObjectIdentity.class);
			return identities;
		} finally {
			connection.release();
		}
	}

	/**
	 * Given a JSON blob presumed to be a map, extract the contents into a
	 * seperate
	 * 
	 * @param jsonBlob
	 * @return
	 */
	public static Map<String, String> extractPropertyMapFromJSON(String jsonBlob) {
		// Empty/non-existant blobs means we just return an empty map.
		if (StringUtils.isBlank(jsonBlob)) {
			return Collections.emptyMap();
		}

		// Extract and parse.
		JsonReader json = new JsonReader(new StringReader(jsonBlob));
		Map<String, String> map = new LinkedHashMap<String, String>();
		try {
			// Expect an array with single objects.
			json.beginArray();
			while (json.hasNext()) {
				json.beginObject();
				if (!json.nextName().equals("name")) {
					throw new Exception("The first map entry must be name.");
				}
				String name = json.nextString();
				if (!json.nextName().equals("value")) {
					throw new Exception("The second map entry must be value.");
				}
				String value = json.nextString();
				json.endObject();

				map.put(name, value);
			}
			json.endArray();
		} catch (Exception ex) {
			// Nothing we can do here. Just return what we managed to piece
			// together.
			return map;
		}

		// Present the results
		return map;
	}

	/**
	 * Simple map export for now; create a much more advanced class with
	 * inheritance that takes care of this later.
	 * 
	 * @param realmid
	 */
	static void processAndExportMapTxtChunk(int realmid, HttpServletResponse response) {
		PrintWriter out;
		Connection c;
		ResultSet rs;
		PreparedStatement st;

		c = null;
		rs = null;
		st = null;

		try {

			// Setup the connection

			c = DBResourceManager.getConnection();

			// Retrieve a list of chunks, from top-left to bottom-right

			st = c.prepareStatement(EXPORT_CHUNK_REQUEST);
			st.setInt(1, realmid);

			rs = st.executeQuery();

			// Setup our response

			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\"realm_" + realmid + ".txt\"");

			out = response.getWriter();

			out.println("//  Export of " + realmid + " on " + new java.util.Date());
			out.println("//  Format: Text, Chunked");

			// Process each chunk

			while (rs.next()) {
				out.print(rs.getInt(2));
				out.print(",");
				out.print(rs.getInt(3));
				out.print(":::");
				out.println(rs.getString(1));
			}

			response.flushBuffer();

		} catch (Exception ex) {
			// All exceptions basically mean we have to stop.

			ex.printStackTrace();

			try {
				response.sendError(500);
			} catch (IOException ioex) {
				// Ignore
			}

		} finally {

			// Safely clean-up database interactions

			DataTools.safeCleanUp(c, st, rs);
		}

	}

}

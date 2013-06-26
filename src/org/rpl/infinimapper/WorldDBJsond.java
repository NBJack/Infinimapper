package org.rpl.infinimapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkDelta;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.management.ChunkCache;
import org.rpl.infinimapper.data.management.ChunkDataProvider;
import org.rpl.infinimapper.security.AuthMan;

import com.google.gson.stream.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Servlet implementation class WorldDB with JSON responses.
 */
public class WorldDBJsond extends HttpServlet {

	/**
	 * Special value to use when the user isn't known.
	 */
	public static final int UNKNOWN_USER_ID = -1;

	/**
	 * Maximum number of requests allowed at once.
	 */
	static final int MAX_REQUESTS_AT_ONCE = 8;

	//
	// The static queries we'll be doing to the database.
	//

	static final String WDB_RETRIEVE_QUERY = "select xcoord, ycoord, realmid, tileset, data, detaildata, lastupdate FROM chunks WHERE xcoord = ? AND ycoord = ? AND realmid = ?";
	static final String WDB_CHECK_QUERY = "SELECT COUNT(*) FROM chunks WHERE xcoord=? AND ycoord=? AND realmid=?";
	static final String WDB_UPDATE_QUERY = "UPDATE chunks set data=?,lastupdate=CURRENT_TIMESTAMP,detaildata=? WHERE xcoord = ? AND ycoord = ? AND realmid = ?";
	static final String WDB_INSERT_QUERY = "INSERT INTO chunks VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, 0)";
	static final String WDB_REFRESH_QUERY = "select tileset, data, detaildata, lastupdate FROM chunks WHERE xcoord = ? AND ycoord = ? AND realmid = ? AND lastupdate>?";
	static final String WDB_GET_OBJLIB = "SELECT id, name, tilesrc, imgXOff, imgYOff, imgWidth, imgHeight FROM objlib";
	static final String WDB_OBJ_INSERT_QUERY = "INSERT INTO objects VALUE (null, ?, ?, ?, ?, ?, ?, null, CURRENT_TIMESTAMP, FALSE, ?, ?)";
	static final String WDB_OBJ_UPDATEPOS_QUERY = "UPDATE objects SET tilerealm=?,tilexcoord=?,tileycoord=?,offsetx=?,offsety=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_OBJ_UPDATEDATA_QUERY = "UPDATE objects SET custom=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_OBJ_RETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety, tilexcoord, tileycoord, deleted, width, height FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=?";
	static final String WDB_OBJ_SINGLE_RETRIEVE_QUERY = "SELECT definition, offsetx, offsety, tilexcoord, tileycoord, deleted, custom, width, height FROM objects WHERE id=?";
	static final String WDB_OBJ_INCRETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=? AND lastupdate<?";
	static final String WDB_OBJ_DELETE = "UPDATE objects SET deleted=true,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_TILESET_LIST = "SELECT name, id FROM tilelib";
	static final String WDB_LAYERS_LIST = "SELECT realmid FROM layerdata WHERE masterrealmid=? ORDER BY ordernum";
	static final String WDB_REALM_TILESETS = "SELECT tilesetid FROM realmtilesets WHERE realmid=? ORDER BY `order`";
	static final String WDB_REALM_PRIMARY_TILESET = "SELECT tileset FROM realms WHERE id=?";

    @Autowired
	private ChunkCache chunkCache;

	public enum QCommand {

		UPDATE, REFRESH, RETRIEVE, OBJLIBRETRIEVE, OBJRETRIEVE, OBJINSERT, OBJDELETE
	}

	/**
	 * Types of objects used in User Authentication rights management.
	 * 
	 * @author rplayfield
	 * 
	 */
	public enum ObjType {

		realm, image, right
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WorldDBJsond() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		BufferedReader queryReader;
		int lineCount;
		String currentLine;
		String[] entries;
		String[] idSet;
		QCommand cmd;
		Connection c;
		ResultSet set;
		PreparedStatement st;
		int tileScale;
		String userIDToken;
		int userID;

		// NOTE: In the future, all references to realm data should be cached
		// globally and retrievable when desired.

		tileScale = 32;

		// Setup links to the input and output

		queryReader = request.getReader();
		JsonWriter jsonOut = new JsonWriter(response.getWriter());

		response.setContentType("text/plain");

		// Grab the user's ID (if available)

		if (request.getSession().getAttribute("userid") != null) {
			userID = (Integer) request.getSession().getAttribute("userid");
		} else {
			userID = -1;
		}

		// Setup our data connection.

		try {

			c = DBResourceManager.getConnection();

			// Nullify our working elements. This will serve
			// as a way of determining if they were used
			// correctly. If they are non-null, assume that
			// the context was not correctly destroyed.

			set = null;
			st = null;

		} catch (Exception sqex) {
			// No database connection, no dice.

			jsonOut.beginObject();
			jsonOut.name("type").value("OFFLINE");
			jsonOut.endObject();
			jsonOut.flush();
			return;
		}

		// Read each line in, up to a max of MAX_REQUESTS_AT_ONCE.
		lineCount = 0;

		// Use the session information stored in the request cookie
		userIDToken = request.getSession().getId();

		jsonOut.beginArray();
		while ((currentLine = queryReader.readLine()) != null && lineCount < MAX_REQUESTS_AT_ONCE) {

			// Trim the whitespace
			currentLine = currentLine.trim();

			System.out.println(currentLine);

			// Are we at the first line?

			// How should we proceed?

			entries = currentLine.split("!!!");

			if (entries.length > 1 && entries[1].contains("x")) {
				idSet = entries[1].split("x");

				System.out.println("CHUNKID: " + entries[1]);

			} else {

				idSet = null;

				jsonOut.beginObject();
				jsonOut.name("type").value("BAD_CHUNK_ID");
				jsonOut.name("request").value(currentLine);
				jsonOut.endObject();

				continue;
			}

			// Create a chunk key just in case we need it
			ChunkKey key = new ChunkKey(Integer.parseInt(idSet[0]), Integer.parseInt(idSet[1]),
					Integer.parseInt(idSet[2]));

			// Try to parse the command

			try {

				cmd = QCommand.valueOf(entries[0]);

				System.out.println(cmd);

				switch (cmd) {
				case RETRIEVE:

					//
					// Retrieve a chunk
					//

					if (RealmManager.isRealmPublic(key.getRealmid())
							|| AuthMan.getRealmPermissionsForUser(userID, key.getRealmid()).canRead()) {

						// Grab the chunk information
						Chunk chunk = chunkCache.getValue(key);

						if (chunk != null) {

							jsonOut.beginObject();
							jsonOut.name("type").value("CHUNK");
							jsonOut.name("id").value(entries[1]);
							jsonOut.name("data").value(chunk.getData());
							jsonOut.name("lastUpdate").value(chunk.getLastUpdate());
							jsonOut.endObject();

						} else {

							// No chunk was found.
							jsonOut.beginObject();
							jsonOut.name("type").value("BLANK");
							jsonOut.name("id").value(entries[1]);
							jsonOut.endObject();
						}

					} else {

						// Note authorization failure
						jsonOut.beginObject();
						jsonOut.name("type").value("BADAUTH");
						jsonOut.name("request").value("RETRIEVE");
						jsonOut.name("id").value(entries[1]);
						jsonOut.endObject();

						System.out.println("Not auth: " + userID);
					}

					break;

				case REFRESH:

					//
					// See if a chunk has been changed
					//

					System.out.println("Timestamp: " + entries[2]);
					long timestamp = Long.parseLong(entries[2]);

					// Get the chunk at the specified key. If null, that means
					// nothing was there for the refresh.
					Chunk chunk = chunkCache.getValue(key);
					if (chunk != null && chunk.getLastUpdate() > timestamp) {

						jsonOut.beginObject();
						jsonOut.name("type").value("REFRESHAVAIL");
						jsonOut.name("id").value(entries[1]);
						jsonOut.name("data").value(chunk.getData());
						jsonOut.name("lastUpdate").value(chunk.getLastUpdate());
						jsonOut.name("sequence").value(entries[3]);
						jsonOut.endObject();
					} else {
						// Nothing changed.
						jsonOut.beginObject();
						jsonOut.name("type").value("NOUPDATE");
						jsonOut.name("id").value(entries[1]);
						jsonOut.endObject();
					}
					break;

				case UPDATE:

					//
					// Update an existing chunk
					//

					// Do we have appropriate permissions?
					if (RealmManager.isRealmPublic(Integer.parseInt(idSet[2]))
							|| AuthMan.getRealmPermissionsForUser(userID, Integer.parseInt(idSet[2])).canWrite()) {

						// Construct a delta and apply it to the store
						ChunkDelta delta = new ChunkDelta(userID, entries[3]);
						chunkCache.updateValue(key, delta);
					} else {

						// The user wasn't authorized
						jsonOut.beginObject();
						jsonOut.name("type").value("BADAUTH");
						jsonOut.name("action").value("INSERT");
						jsonOut.name("id").value(entries[1]);
						jsonOut.name("sequence").value(entries[5]);
						jsonOut.endObject();
					}

					break;

				case OBJINSERT:

					//
					// Insert or update a new object
					//

					int rawX,
					rawY,
					tRealm;
					int tX,
					tY;
					int oldID,
					newID;
					int chunkRealWidth,
					chunkRealHeight,
					chunkWidth,
					chunkHeight;
					int offX,
					offY;
					int instWidth,
					instHeight;

					// Replace these with realm queries

					chunkRealWidth = ChunkData.TILES_WIDTH_IN_CHUNK * 32;
					chunkRealHeight = ChunkData.TILES_HEIGHT_IN_CHUNK * 32;

					// Format of command:
					// OBJINSERT!!!XxYxRealmID!!!originalID!!!ObjTypeDefinition!!!instWidth!!!instHeight

					// Calculate the tile and coordinates

					oldID = Integer.parseInt(entries[2]);

					rawX = key.getXcoord();
					rawY = key.getYcoord();
					tRealm = key.getRealmid();

					tX = (int) Math.floor(rawX / (double) chunkRealWidth) * ChunkData.TILES_WIDTH_IN_CHUNK;
					tY = (int) Math.floor(rawY / (double) chunkRealHeight) * ChunkData.TILES_HEIGHT_IN_CHUNK;
					offX = ((rawX % chunkRealWidth) + chunkRealWidth) % chunkRealWidth;
					offY = ((rawY % chunkRealHeight) + chunkRealHeight) % chunkRealHeight;
					instWidth = Integer.parseInt(entries[4]);
					instHeight = Integer.parseInt(entries[5]);

					System.out.println("Raw coord: " + rawX + "," + rawY + " vs. Tile: " + tX + "," + tY);

					// TODO: Verify coordinates fall within the appropriate
					// area, and that we have authorization to access it.

					// Insert a new object or retrieve an existing one.

					if (oldID < 0) {
						// Insert a new object

						st = c.prepareStatement(WDB_OBJ_INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);

						// TODO: Ugh. I'm looking forward to using Hibernate. :/
						st.setInt(1, tRealm);
						st.setInt(2, tX);
						st.setInt(3, tY);
						st.setInt(4, offX);
						st.setInt(5, offY);
						st.setInt(6, Integer.parseInt(entries[3]));
						st.setInt(7, instWidth);
						st.setInt(8, instHeight);

						st.execute();

						set = st.getGeneratedKeys();

						System.out.println("DEBUG: raw: (" + rawX + "," + rawY + ")  official:"
								+ (tX * tileScale + offX) + "," + (tY * tileScale + offY));

						// Respond with an ID update if successful; otherwise,
						// respond negatively.

						if (set.next()) {
							System.out.println("Autogenerated object ID: " + set.getInt(1));

							jsonOut.beginObject();
							jsonOut.name("type").value("OK_OBJ");
							jsonOut.name("subType").value("IDUP");
							jsonOut.name("id").value(entries[2]);
							jsonOut.name("newid").value(set.getInt(1));
							jsonOut.endObject();

						} else {

							jsonOut.beginObject();
							jsonOut.name("type").value("BAD_OBJ");
							jsonOut.name("id").value(entries[2]);
							jsonOut.endObject();
						}

					} else {

						// Update the object definition

						st = c.prepareStatement(WDB_OBJ_UPDATEPOS_QUERY);

						st.setInt(1, tRealm);
						st.setInt(2, tX);
						st.setInt(3, tY);
						st.setInt(4, offX);
						st.setInt(5, offY);

						st.setInt(6, oldID);

						st.execute();

						// Send a simple update response

						jsonOut.beginObject();
						jsonOut.name("type").value("OK_OBJ_UP");
						jsonOut.name("id").value(entries[2]);
						jsonOut.endObject();
					}

					break;

				case OBJRETRIEVE:

					//
					// Retrieve all objects in a specified chunk. Optionally,
					// only retrieve those changed since a
					// specific timestamp.
					//

					st = c.prepareStatement(WDB_OBJ_RETRIEVE_QUERY);

					st.setInt(1, key.getRealmid());
					st.setInt(2, key.getXcoord());
					st.setInt(3, key.getYcoord());

					System.out.println("Getting all objects from realm " + key.getRealmid());

					set = st.executeQuery();

					jsonOut.beginObject();
					jsonOut.name("type").value("OBJDATA");
					jsonOut.name("objects");

					jsonOut.beginArray();
					while (set.next()) {
						jsonOut.beginObject();
						jsonOut.name("id").value(set.getInt(1));
						jsonOut.name("definition").value(set.getInt(2));
						jsonOut.name("xCoord").value(set.getInt(3) + set.getInt(5) * tileScale);
						jsonOut.name("yCoord").value(set.getInt(4) + set.getInt(6) * tileScale);
						jsonOut.name("deleted").value(set.getInt(7));
						jsonOut.name("width").value(set.getInt(8));
						jsonOut.name("height").value(set.getInt(9));
						jsonOut.endObject();
					}
					jsonOut.endArray();
					jsonOut.endObject();
					break;

				case OBJLIBRETRIEVE:

					// Retrieve the object library

					st = c.prepareStatement(WDB_GET_OBJLIB);

					set = st.executeQuery();

					// Print the response

					jsonOut.beginObject();
					jsonOut.name("type").value("OBJLIB");

					jsonOut.beginArray();
					while (set.next()) {
						jsonOut.beginObject();
						jsonOut.name("id").value(set.getInt(1));
						jsonOut.name("name").value(set.getString(2));
						jsonOut.name("tilesrc").value(set.getInt(3));
						jsonOut.name("imgXOff").value(set.getInt(4));
						jsonOut.name("imgYOff").value(set.getInt(5));
						jsonOut.name("imgWidth").value(set.getInt(6));
						jsonOut.name("imgHeight").value(set.getInt(7));
						jsonOut.endObject();
					}
					jsonOut.endArray();

					// Terminate the response string

					set.close();
					st.close();

					break;

				case OBJDELETE:

					//
					// Delete a specific object
					//

					// TODO: Check permissions

					// Execute query

					st = c.prepareStatement(WDB_OBJ_DELETE);
					st.setInt(1, Integer.parseInt(entries[2]));
					st.execute();

					System.out.println("DETELED: " + entries[2]);

					// Print a response
					jsonOut.beginObject();
					jsonOut.name("type").value("OBJ_DELETE_OK");
					jsonOut.name("id").value(entries[2]);
					jsonOut.endObject();

					break;

				}

			} catch (Exception ex) {
				ex.printStackTrace();

				jsonOut.beginObject();
				jsonOut.name("type").value("QUERY_ERR");
				jsonOut.endObject();
			}

		}
		jsonOut.endArray();

		//
		// Try to ensure a clean closure.
		//

		try {
			// c.commit();
			c.close();

			if (set != null)
				set.close();

			if (st != null)
				st.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		jsonOut.flush();
	}

	/**
	 * An expensive operation to retrieve all of the tile sets by names and IDs.
	 * Ideally, this will be replaced by a 'get just n entries'.
	 * 
	 * @return
	 */
	public static Collection<String[]> getAllTilesets() {
		LinkedList<String[]> results;
		Connection c;
		Statement st;
		ResultSet set;

		c = null;
		set = null;
		st = null;

		results = new LinkedList<String[]>();

		try {

			c = DBResourceManager.getConnection();

			st = c.createStatement();
			set = st.executeQuery(WDB_TILESET_LIST);

			while (set.next()) {
				String[] r;

				// Construct the pair

				r = new String[] { set.getString(1), set.getString(2) };

				// Add to our collection

				results.addLast(r);
			}

		} catch (Exception sqex) {
			sqex.printStackTrace();

		} finally {
			// Clean-up

			DataTools.safeCleanUp(c, st, set);
		}

		return results;

	}

	/**
	 * Builds a list of integers from the specified result set, presering the
	 * order.
	 * 
	 * @param set The results to pull information from.
	 * @return A list of integers.
	 * @throws SQLException Thrown if something goes wrong in procesing.
	 */
	public static Collection<Integer> getIntListFromResultSet(ResultSet set) throws SQLException {
		LinkedList<Integer> list = new LinkedList<Integer>();
		while (set.next()) {
			list.addLast(set.getInt(1));
		}
		return list;
	}

	/**
	 * Retrieve a list of tilesets for the specified realm.
	 * 
	 * @param realmid
	 * @return
	 * @throws SQLException
	 */
	public static Collection<Integer> getSupplementalTilesetsForRealm(int realmid) throws SQLException {
		Collection<Integer> list = new LinkedList<Integer>();
		QuickCon con = new QuickCon(WDB_REALM_TILESETS);

		try {
			// Setup the statement
			con.getStmt().setInt(1, realmid);
			// Grab results
			ResultSet results = con.query();
			list = getIntListFromResultSet(results);
			return list;
		} finally {
			con.release();
		}
	}

	/**
	 * Retrieves a list of layers for a particular realm.
	 * 
	 * @param realmid
	 * @return
	 * @throws SQLException
	 */
	public static Collection<Integer> getLayersForRealm(int realmid) throws SQLException {
		Collection<Integer> list = new LinkedList<Integer>();
		QuickCon con = new QuickCon(WDB_LAYERS_LIST);

		try {
			// Setup the statement
			con.getStmt().setInt(1, realmid);
			// Grab results
			ResultSet results = con.query();
			list = getIntListFromResultSet(results);
			return list;
		} finally {
			con.release();
		}
	}

	/**
	 * Grabs a complete list of tilesets for the realm, in order.
	 * 
	 * @param realmid The realm's ID.
	 * @return A list of all tilesets in the perscribed order.
	 * @throws SQLException
	 */
	public static Collection<Integer> getAllTilesetsForRealm(int realmid) throws SQLException {
		// Get the primary realm tileset
		QuickCon con = new QuickCon(WDB_REALM_PRIMARY_TILESET);
		LinkedList<Integer> results = new LinkedList<Integer>();
		try {
			con.getStmt().setInt(1, realmid);
			ResultSet realmQuery = con.query();
			realmQuery.next();
			results.add(realmQuery.getInt(1));
		} finally {
			con.release();
		}
		// Add in all auxillary tilesets
		results.addAll(getSupplementalTilesetsForRealm(realmid));
		return results;
	}

	/**
	 * A tidy package encapsulating overlapping functionality of pooled data
	 * connection queries on prepared statements. The general order of
	 * operations is:
	 * <p>
	 * <li>Create this object with the SQL to be templated
	 * <li>Get the statement and fill in the values
	 * <li>Execute the query to get the data
	 * 
	 * @author rplayfield
	 * 
	 */
	public static class QuickCon {
		Connection c = null;
		ResultSet set = null;
		PreparedStatement st = null;

		/**
		 * Do a quick statement.
		 * 
		 * @param sql
		 */
		public QuickCon(String sql) throws SQLException {
			try {

				// Grab a connection, create the statement, and
				// go ahead and execute the query.

				c = DBResourceManager.getConnection();

				st = c.prepareCall(sql);

			} catch (SQLException sqex) {
				// Make sure we release our resources safely before returning!

				release();

				throw sqex;

			}
		}

		/**
		 * Get the prepared statement. Use this method to manipulate the
		 * underlying object.
		 * 
		 * @return
		 */
		public PreparedStatement getStmt() {
			return st;
		}

		/**
		 * Executes the query and retrieves the data.
		 * 
		 * @return
		 */
		public ResultSet query() throws SQLException {
			set = st.executeQuery();

			return set;
		}

		/**
		 * Commits the changes.
		 * 
		 * @throws SQLException
		 */
		public void commit() throws SQLException {
			c.commit();
		}

		/**
		 * Release all resources. Must be called to properly clean up.
		 */
		public void release() {
			// Safely clean-up our connections.

			DataTools.safeCleanUp(c, st, set);

			// Nullify all entries.

			c = null;
			set = null;
			st = null;
		}
	}
}

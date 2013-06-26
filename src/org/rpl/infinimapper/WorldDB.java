package org.rpl.infinimapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkDelta;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.export.TilesetExport;
import org.rpl.infinimapper.data.management.ChunkCache;
import org.rpl.infinimapper.data.management.RealmCache;
import org.rpl.infinimapper.security.AuthMan;

import com.google.gson.stream.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Servlet implementation class WorldDB
 */
public class WorldDB extends HttpServlet {

    private static final Logger log = Logger.getGlobal();

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



	static final String WDB_GET_OBJLIB = "SELECT id, NAME, tilesrc, imgXOff, imgYOff, imgWidth, imgHeight FROM objlib";
	static final String WDB_OBJ_INSERT_QUERY = "INSERT INTO objects VALUE (NULL, ?, ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP, FALSE, ?, ?)";
	static final String WDB_OBJ_UPDATEPOS_QUERY = "UPDATE objects SET tilerealm=?,tilexcoord=?,tileycoord=?,offsetx=?,offsety=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_OBJ_UPDATEDATA_QUERY = "UPDATE objects SET custom=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_OBJ_RETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety, tilexcoord, tileycoord, deleted, width, height FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=?";
	static final String WDB_OBJ_SINGLE_RETRIEVE_QUERY = "SELECT definition, offsetx, offsety, tilexcoord, tileycoord, deleted, custom, width, height FROM objects WHERE id=?";
	static final String WDB_OBJ_INCRETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=? AND lastupdate<?";
	static final String WDB_OBJ_DELETE = "UPDATE objects SET deleted=TRUE,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
	static final String WDB_TILESET_LIST = "SELECT NAME, id FROM tilelib";
	static final String WDB_LAYERS_LIST = "SELECT realmid FROM layerdata WHERE masterrealmid=? ORDER BY ordernum";

    public ChunkCache getChunkCache() {
        return chunkCache;
    }

    public void setChunkCache(ChunkCache chunkCache) {
        this.chunkCache = chunkCache;
    }

    public RealmCache getRealmCache() {
        return realmCache;
    }

    public void setRealmCache(RealmCache realmCache) {
        this.realmCache = realmCache;
    }

    @Autowired
    private ChunkCache chunkCache;
    @Autowired
    private RealmCache realmCache;


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
	public WorldDB() {
		super();
	}


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
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
		PrintWriter respOut;
		Connection c;
		ResultSet set;
		PreparedStatement st;
		int tileScale;
		String userIDToken;
		int userID;

		// NOTE: In the future, all references to realm data should be cached
		// globally and retrievable when desired.


		// Setup links to the input and output

		queryReader = request.getReader();
		respOut = response.getWriter();
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

			respOut.println("OFFLINE!!!");

			return;
		}

		// Read each line in, up to a max of three.

		lineCount = 0;

		// Use the session information stored in the request cookie

		userIDToken = request.getSession().getId();

		while ((currentLine = queryReader.readLine()) != null && lineCount < MAX_REQUESTS_AT_ONCE) {

			// Trim the whitespace
			currentLine = currentLine.trim();

			log.finest(currentLine);

			// Are we at the first line?

			// How should we proceed?

			entries = currentLine.split("!!!");

			if (entries.length > 1 && entries[1].contains("x")) {
				idSet = entries[1].split("x");
                log.finest("CHUNKID: " + entries[1]);

			} else {

				idSet = null;

				respOut.println("BAD_CHUNK_ID");

				continue;
			}

			// Create a chunk key just in case we need it
			ChunkKey key = new ChunkKey(Integer.parseInt(idSet[0]), Integer.parseInt(idSet[1]),
					Integer.parseInt(idSet[2]));
            // Grab the appropriate realm
            Realm currentRealm = realmCache.getValue(key.getRealmid());


            // Try to parse the command

			try {

				cmd = QCommand.valueOf(entries[0]);

                log.finest("Command: " + cmd);

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

							respOut.print("CHUNK!!!");
							respOut.print(entries[1]);
							respOut.print("!!!");
							respOut.print(0);
							respOut.print("!!!");
							respOut.print(chunk.getData());
							respOut.print("!!!");
							respOut.print("0,0");
							respOut.print("!!!");
							respOut.print(chunk.getLastUpdate());
							respOut.print("!!!");
							respOut.println();

						} else {
							// No chunk was found.
							respOut.print("BLANK!!!");
							respOut.print(entries[1]);
							respOut.print("!!!");
							respOut.println();
						}

					} else {

						// Note authorization failure

						respOut.print("BADAUTH!!!RETRIEVE!!!");
						respOut.print(entries[1]);
						respOut.print("!!!");
						respOut.println();

                        log.fine("Not auth: " + userID);
					}

					break;

				case REFRESH:

					//
					// See if a chunk has been changed
					//

					log.finest("Timestamp: " + entries[2]);
					long timestamp = Long.parseLong(entries[2]);

					// Get the chunk at the specified key. If null, that means
					// nothing was there for the refresh.
					Chunk chunk = chunkCache.getValue(key);
					if (chunk != null && chunk.getLastUpdate() > timestamp) {
						respOut.print("REFRESHAVAIL!!!");
						respOut.print(entries[1]);
						respOut.print("!!!");
						respOut.print("0");
						respOut.print("!!!");
						respOut.print(chunk.getData());
						respOut.print("!!!");
						respOut.print("0,0");
						respOut.print("!!!");
						respOut.print(chunk.getLastUpdate());
						respOut.print("!!!");
						respOut.print(entries[3]);
						respOut.print("!!!");
					} else {
						respOut.print("NOUPDATE!!!");
						respOut.print(entries[1]);
						respOut.print("!!!");
						respOut.println();
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
						respOut.print("BADAUTH!!!INSERT!!!");
						respOut.print(entries[1]);
						respOut.print("!!!");
						respOut.print(entries[5]);
						respOut.print("!!!");
						respOut.println();
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

					rawX = Integer.parseInt(idSet[0]);
					rawY = Integer.parseInt(idSet[1]);
					tRealm = Integer.parseInt(idSet[2]);

					tX = (int) Math.floor(rawX / (double) chunkRealWidth) * ChunkData.TILES_WIDTH_IN_CHUNK;
					tY = (int) Math.floor(rawY / (double) chunkRealHeight) * ChunkData.TILES_HEIGHT_IN_CHUNK;
					offX = ((rawX % chunkRealWidth) + chunkRealWidth) % chunkRealWidth;
					offY = ((rawY % chunkRealHeight) + chunkRealHeight) % chunkRealHeight;
					instWidth = Integer.parseInt(entries[4]);
					instHeight = Integer.parseInt(entries[5]);

					log.finest("Raw coord: " + rawX + "," + rawY + " vs. Tile: " + tX + "," + tY);

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

						log.finer("DEBUG: raw: (" + rawX + "," + rawY + ")  official:"
								+ (tX * currentRealm.getTileWidth() + offX) + "," + (tY * currentRealm.getTileHeight() + offY));

						// Respond with an ID update if successful; otherwise,
						// respond negatively.

						if (set.next()) {
							log.finest("Autogenerated object ID: " + set.getInt(1));

							respOut.println("OK_OBJ!!!IDUP!!!" + entries[2] + "!!!" + set.getInt(1) + "!!!");

						} else {

							respOut.println("BAD_OBJ!!!" + entries[2] + "!!!");

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

						respOut.println("OK_OBJ_UP!!!" + entries[2] + "!!!");

					}

					break;

				case OBJRETRIEVE:

					//
					// Retrieve all objects in a specified chunk. Optionally,
					// only retrieve those changed since a
					// specific timestamp.
					//

					st = c.prepareStatement(WDB_OBJ_RETRIEVE_QUERY);

					st.setInt(1, Integer.parseInt(idSet[2]));
					st.setInt(2, Integer.parseInt(idSet[0]));
					st.setInt(3, Integer.parseInt(idSet[1]));

					log.finest("Getting all objects from realm " + idSet[2]);

					set = st.executeQuery();

					respOut.print("OBJDATA!!!");

					while (set.next()) {
						respOut.print(set.getInt(1));
						respOut.print(",");
						respOut.print(set.getInt(2));
						respOut.print(",");
						respOut.print(set.getInt(3) + set.getInt(5) * currentRealm.getTileWidth());
						respOut.print(",");
						respOut.print(set.getInt(4) + set.getInt(6) * currentRealm.getTileHeight());
						respOut.print(",");
						respOut.print(set.getInt(7));
						respOut.print(",");
						respOut.print(set.getInt(8));
						respOut.print(",");
						respOut.print(set.getInt(9));

						respOut.print("###");
					}

					respOut.println("!!!");

					break;

				case OBJLIBRETRIEVE:

					// Retrieve the object library

					st = c.prepareStatement(WDB_GET_OBJLIB);

					set = st.executeQuery();

					// Print the response

					respOut.print("OBJLIB!!!");

					while (set.next()) {
						respOut.print(set.getInt(1));
						respOut.print("###");
						respOut.print(set.getString(2));
						respOut.print("###");
						respOut.print(set.getInt(3));
						respOut.print("###");
						respOut.print(set.getInt(4));
						respOut.print("###");
						respOut.print(set.getInt(5));
						respOut.print("###");
						respOut.print(set.getInt(6));
						respOut.print("###");
						respOut.print(set.getInt(7));
						respOut.print("!!!");
					}

					// Terminate the response string

					set.close();
					st.close();

					respOut.println();

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

					log.finer("DETELED: " + entries[2]);

					// Print a response

					respOut.println("OBJ_DELETE_OK!!!" + entries[1] + "!!!");

					break;

				}

			} catch (Exception ex) {
				ex.printStackTrace();

				respOut.println("QUERY_ERR!!!");
			}

		}

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
		respOut.flush();

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
			list = TilesetExport.getIntListFromResultSet(results);
			return list;
		} finally {
			con.release();
		}
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

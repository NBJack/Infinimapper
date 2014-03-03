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
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.data.*;
import org.rpl.infinimapper.data.export.TilesetExport;
import org.rpl.infinimapper.data.management.*;
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
    @Autowired
    private LayerDataProvider layerProvider;
    @Autowired
    private ObjectProvider objectDefProvider;
    @Autowired
    private ObjectInstanceProvider objectProvider;

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
		String userIDToken;
        PreparedStatement st;

		// NOTE: In the future, all references to realm data should be cached
		// globally and retrievable when desired.


		// Setup links to the input and output

		queryReader = request.getReader();
		respOut = response.getWriter();
		JsonWriter jsonOut = new JsonWriter(response.getWriter());

		response.setContentType("text/plain");

		// Grab the user's ID (if available)

        int userID = -1;
		if (request.getSession().getAttribute("userid") != null) {
			userID = (Integer) request.getSession().getAttribute("userid");
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

                    retrieveChunk(respOut, userID, key);
                    break;

				case REFRESH:

                    refreshChunk(entries, respOut, key);
                    break;

				case UPDATE:

                    updateChunk(entries, idSet[2], respOut, userID, key);
                    break;

				case OBJINSERT:

                    addOrUpdateObject(entries, idSet, respOut, currentRealm);
                    break;

				case OBJRETRIEVE:

					//
					// Retrieve all objects in a specified chunk. Optionally,
					// only retrieve those changed since a
					// specific timestamp.
					//

                    int objRealmID = Integer.parseInt(idSet[2]);
                    int tileX = Integer.parseInt(idSet[0]);
                    int tileY = Integer.parseInt(idSet[1]);
					log.finest("Getting all objects from realm " + idSet[2]);

                    Realm objRealm = realmCache.getValue(Integer.parseInt(idSet[2]));
                    List<ObjectInstance> objects = objectProvider.getObjectsOnChunk(objRealm, tileX, tileY);


					respOut.print("OBJDATA!!!");

                    for ( ObjectInstance obj : objects )
                    {
                        respOut.print(obj.getId());
                        respOut.print(",");
                        respOut.print(obj.getDefinition());
                        respOut.print(",");
                        respOut.print(obj.getOffsetX() + obj.getTileXCoord() * currentRealm.getTileWidth());
                        respOut.print(",");
                        respOut.print(obj.getOffsetY() + obj.getTileYCoord() * currentRealm.getTileHeight());
                        respOut.print(",");
                        respOut.print(obj.isDeleted() ? 1:0);
                        respOut.print(",");
                        respOut.print(obj.getWidth());
                        respOut.print(",");
                        respOut.print(obj.getHeight());

						respOut.print("###");
					}

					respOut.println("!!!");

					break;

				case OBJLIBRETRIEVE:

					// Retrieve the object library
                    List<ObjectIdentity> definitions = objectDefProvider.getAllDefinitions();

					// Print the response
					respOut.print("OBJLIB!!!");

                    for ( ObjectIdentity definition : definitions ) {

                        respOut.print(definition.getId());
                        respOut.print("###");
                        respOut.print(definition.getName());
                        respOut.print("###");
                        respOut.print(definition.getTilesrc());
                        respOut.print("###");
                        respOut.print(definition.getImgXOff());
                        respOut.print("###");
                        respOut.print(definition.getImgYOff());
                        respOut.print("###");
                        respOut.print(definition.getImgWidth());
                        respOut.print("###");
                        respOut.print(definition.getImgHeight());

						respOut.print("!!!");
					}

					// Terminate the response string
					respOut.println();

					break;

				case OBJDELETE:

					//
					// Delete a specific object
					//

					// TODO: Check permissions

					// Execute query

                    int objID = Integer.parseInt(entries[2]);
                    objectProvider.deleteValue(objID);

					log.finer("DETELED: " + objID);

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

    private void updateChunk(String[] entries, String s, PrintWriter respOut, int userID, ChunkKey key) throws SQLException {
        //
        // Update an existing chunk
        //

        // Do we have appropriate permissions?
        if (RealmManager.isRealmPublic(Integer.parseInt(s))
                || AuthMan.getRealmPermissionsForUser(userID, Integer.parseInt(s)).canWrite()) {

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
    }

    private void refreshChunk(String[] entries, PrintWriter respOut, ChunkKey key) {
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
            respOut.print(key.generateID());
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
            respOut.print(key.generateID());
            respOut.print("!!!");
            respOut.println();
        }
    }

    private void retrieveChunk(PrintWriter respOut, int userID, ChunkKey key) throws SQLException {
        //
        // Retrieve a chunk
        //

        if (RealmManager.isRealmPublic(key.getRealmid())
                || AuthMan.getRealmPermissionsForUser(userID, key.getRealmid()).canRead()) {

            // Grab the chunk information
            Chunk chunk = chunkCache.getValue(key);

            respOut.println(generateChunkResponse(key, chunk));

        } else {

            // Note authorization failure

            respOut.print("BADAUTH!!!RETRIEVE!!!");
            respOut.print(key.generateID());
            respOut.print("!!!");
            respOut.println();

log.fine("Not auth: " + userID);
        }
    }


    /**
     * Generate a response for a given chunk. This eventually needs to be refactored; the l''
     *
     * @param key The key we were checking.
     * @param chunk The chunk we may have found. Can be null; if so, it means the chunk was never found.
     */
    public static String generateChunkResponse(ChunkKey key, Chunk chunk) {

        StringBuffer buf = new StringBuffer();


        if (chunk != null) {

            buf.append("CHUNK!!!");
            buf.append(key.generateID());
            buf.append("!!!");
            buf.append(0);
            buf.append("!!!");
            buf.append(chunk.getData());
            buf.append("!!!");
            buf.append("0,0");
            buf.append("!!!");
            buf.append(chunk.getLastUpdate());
            buf.append("!!!");

        } else {
            // No chunk was found.
            buf.append("BLANK!!!");
            buf.append(key.generateID());
            buf.append("!!!");
        }

        return buf.toString();
    }

    /**
     * Add or update an object as provided in the entries.
     *
     * @param entries
     * @param idSet
     * @param respOut
     * @param currentRealm
     * @return
     * @throws SQLException
     */
    private int addOrUpdateObject(String[] entries, String[] idSet, PrintWriter respOut, Realm currentRealm) throws SQLException {

        // Format of command:
        // OBJINSERT!!!XxYxRealmID!!!originalID!!!ObjTypeDefinition!!!instWidth!!!instHeight

        // Calculate the tile and coordinates

        int oldID = Integer.parseInt(entries[2]);

        int rawX = Integer.parseInt(idSet[0]);
        int rawY = Integer.parseInt(idSet[1]);
        int tRealm = Integer.parseInt(idSet[2]);

        int tX = (int) Math.floor(rawX / (double) currentRealm.getChunkWidthInPixels()) * ChunkData.TILES_WIDTH_IN_CHUNK;
        int tY = (int) Math.floor(rawY / (double) currentRealm.getChunkHeightInPixels()) * ChunkData.TILES_HEIGHT_IN_CHUNK;
        int offX = ((rawX % currentRealm.getChunkWidthInPixels()) + currentRealm.getChunkWidthInPixels()) % currentRealm.getChunkWidthInPixels();
        int offY = ((rawY % currentRealm.getChunkHeightInPixels()) + currentRealm.getChunkHeightInPixels()) % currentRealm.getChunkHeightInPixels();
        int instWidth = Integer.parseInt(entries[4]);
        int instHeight = Integer.parseInt(entries[5]);

        int instanceId = Integer.parseInt(entries[3]);

        log.finest("Raw coord: " + rawX + "," + rawY + " vs. Tile: " + tX + "," + tY);

        // TODO: Verify coordinates fall within the appropriate
        // area, and that we have authorization to access it.

        // Insert a new object or retrieve an existing one.
        if (oldID < 0) {
            // Insert a new object

            ObjectInstance obj = new ObjectInstance();
            obj.setDefinition(instanceId);
            obj.setOffsetX(offX);
            obj.setOffsetY(offY);
            obj.setTileRealm(tRealm);
            obj.setTileXCoord(tX);
            obj.setTileYCoord(tY);
            obj.setWidth(instWidth);
            obj.setHeight(instHeight);

            objectProvider.addValue(obj);


            log.finer("DEBUG: raw: (" + rawX + "," + rawY + ")  official:"
                    + (tX * currentRealm.getTileWidth() + offX) + "," + (tY * currentRealm.getTileHeight() + offY));

            // Respond with an ID update if successful; otherwise,
            // respond negatively.

            if (obj.hasId()) {
                int givenID = obj.getId();

                log.finest("Autogenerated object ID: " + givenID);
                respOut.println("OK_OBJ!!!IDUP!!!" + entries[2] + "!!!" + givenID + "!!!");

            } else {

                respOut.println("BAD_OBJ!!!" + entries[2] + "!!!");

            }

            return obj.getId();

        } else {

            // Update the object definition

            ObjectInstance obj = this.objectProvider.getValue(oldID);
            obj.setTileXCoord(tX);
            obj.setTileYCoord(tY);
            obj.setTileRealm(tRealm);
            obj.setOffsetX(offX);
            obj.setOffsetY(offY);


            // Send a simple update response
            respOut.println("OK_OBJ_UP!!!" + entries[2] + "!!!");

            return obj.getID();
        }

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
			set = st.executeQuery(TilesetProvider.WDB_TILESET_LIST);

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
		QuickCon con = new QuickCon(LayerDataProvider.WDB_LAYERS_LIST);

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

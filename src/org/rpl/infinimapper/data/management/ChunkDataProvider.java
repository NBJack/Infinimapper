package org.rpl.infinimapper.data.management;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.rpl.infinimapper.WorldDB.QuickCon;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.TilesetData;

/**
 * Retrieves and pushes chunks to the database. Right now, due to some limitations with
 * ORMLite, we must do this via SQL manually. ORMLite sadly cannot deal with complex
 * primary keys spanning more than one field.
 * 
 * @author Ryan
 * 
 */
public class ChunkDataProvider extends DataProvider<ChunkKey, Chunk> {

	static final String CHUNK_RETRIEVE_QUERY = "select data, lastupdate, userid FROM chunks WHERE xcoord = ? AND ycoord = ? AND realmid = ? LIMIT 1";
	static final String CHUNK_UPDATE_QUERY = "UPDATE chunks set data=?,lastupdate=?,userid=? WHERE xcoord = ? AND ycoord = ? AND realmid = ?";
	static final String CHUNK_INSERT_QUERY = "INSERT INTO chunks(xcoord, ycoord, realmid, data, lastupdate, userid) VALUES (?, ?, ?, ?, ?, ?)";



    public ChunkDataProvider() throws SQLException {
        //super(Chunk.class);
    }


	@Override
	public Chunk getValue(ChunkKey key) {

		Chunk chunk = null;
		QuickCon connection = null;
		try {
			// Setup the query itself
			connection = new QuickCon(CHUNK_RETRIEVE_QUERY);
			connection.getStmt().setInt(1, key.getXcoord());
			connection.getStmt().setInt(2, key.getYcoord());
			connection.getStmt().setInt(3, key.getRealmid());

			ResultSet results = connection.query();

			if (results.next()) {
				// Populate chunk
				chunk = new Chunk(key.getRealmid(), key.getXcoord(), key.getYcoord());
				chunk.setData(results.getString(1));
				chunk.setLastUpdate(results.getTimestamp(2).getTime());
			}

		} catch (SQLException exception) {
			// TODO: Proper logging
			exception.printStackTrace();
		} finally {
			// Cleanup
			if (connection != null) {
				connection.release();
			}
		}

		return chunk;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For the database access involved, a put may be saving a new function or
	 * updating an existing one. A check must be done to determine which is
	 * appropriate.
	 */
	@Override
	public void putValue(ChunkKey key, Chunk value) {

		Chunk existing = getValue(key);
		QuickCon con = null;
		try {
			if (existing == null) {

				// Insertion
				con = new QuickCon(CHUNK_INSERT_QUERY);
				con.getStmt().setInt(1, key.getXcoord());
				con.getStmt().setInt(2, key.getYcoord());
				con.getStmt().setInt(3, key.getRealmid());
				con.getStmt().setString(4, value.getData());
				con.getStmt().setTimestamp(5, new Timestamp(value.getLastUpdate()));
				con.getStmt().setInt(6, value.getUserId());
				con.getStmt().execute();

			} else {

				// Update
				con = new QuickCon(CHUNK_UPDATE_QUERY);
				con.getStmt().setString(1, value.getData());
				con.getStmt().setTimestamp(2, new Timestamp(value.getLastUpdate()));
				con.getStmt().setInt(3, value.getUserId());
				con.getStmt().setInt(4, key.getXcoord());
				con.getStmt().setInt(5, key.getYcoord());
				con.getStmt().setInt(6, key.getRealmid());
				con.getStmt().execute();

				if (con.getStmt().getUpdateCount() == 0) {
					System.err.println("There were no updates to an existing chunk!");
				}

			}

		} catch (SQLException exception) {
			// TODO: Do this in a proper logger
			exception.printStackTrace();
		} finally {
			if (con != null) {
				con.release();
			}
		}
	}

}

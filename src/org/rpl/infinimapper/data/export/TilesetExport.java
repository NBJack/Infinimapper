package org.rpl.infinimapper.data.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.rpl.infinimapper.WorldDB;
import org.rpl.infinimapper.WorldDB.QuickCon;

/**
 * Provides the ability to export specific tilesets.
 * 
 * @author Ryan
 * 
 */
public class TilesetExport {

	public static final String SQL_REALM_TILESETS = "SELECT tilesetid FROM realmtilesets WHERE realmid=? ORDER BY `order`";
	public static final String WDB_REALM_PRIMARY_TILESET = "SELECT tileset FROM realms WHERE id=?";
	private static final String SQL_GET_IMAGEDATA = "SELECT imagedata FROM tilelib WHERE id=?";

	/**
	 * Write the specified tileset to the provided stream.
	 * 
	 * @param tilesetID The ID of the tileset to write.
	 * @param outputStream The output stream. Cannot be null.
	 * @throws IOException if something went wrong transferring the data.
	 * @throws FileNotFoundException if the image wasn't found.
	 */
	public static void writeImagetoStream(int tilesetID, OutputStream outputStream) throws IOException {
		QuickCon con = null;

		try {
			con = new QuickCon(SQL_GET_IMAGEDATA);
			con.getStmt().setInt(1, tilesetID);
			ResultSet set = con.query();
			if (set.next()) {
				outputStream.write(set.getBytes(1));
			} else {
				throw new FileNotFoundException("The specified tileset (" + tilesetID + ") wasn't found.");
			}
		} catch (SQLException e) {
			// Something went wrong with the SQL. Bummer. Consider it an IO
			// exception.
			e.printStackTrace();
			throw new IOException("Error while attempting to get the image data.", e);
		} finally {
			// Clean up safely.
			if (con != null) {
				con.release();
			}
		}
	}

	/**
	 * Grabs a complete list of tilesets for the realm, in order.
	 * 
	 * @param realmid The realm's ID.
	 * @return A list of all tilesets in the perscribed order.
	 * @throws SQLException
	 */
	public static List<Integer> getAllTilesetsForRealm(int realmid) throws SQLException {
		// Get the primary realm tileset
		WorldDB.QuickCon con = new WorldDB.QuickCon(TilesetExport.WDB_REALM_PRIMARY_TILESET);
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
		results.addAll(TilesetExport.getSupplementalTilesetsForRealm(realmid));
		return results;
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
		WorldDB.QuickCon con = new WorldDB.QuickCon(TilesetExport.SQL_REALM_TILESETS);

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
	 * Writes a given tileset to the appropriate target. Note this assumes that
	 * the target name is setup properly.
	 * 
	 * @param tilesetId
	 * @param target
	 * @throws IOException
	 */
	public static void writeTilesetToFile(int tilesetId, File target) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(target);
		TilesetExport.writeImagetoStream(tilesetId, fileOut);
		fileOut.flush();
		fileOut.close();
	}

}

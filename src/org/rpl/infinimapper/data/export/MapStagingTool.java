package org.rpl.infinimapper.data.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.MapDataType;
import org.rpl.util.FileUtils;

/**
 * Writes out map and necessary resources to a common destination. Useful for
 * deployments.
 * 
 * @author Ryan Layfield
 * 
 */
public class MapStagingTool {

	private static final String TMX_FILE_EXTENSION = ".tmx";
	private static final String FILE_EXT = ".png";

	/**
	 * Where everything needs to go.
	 */
	private File destination;
	private String imagePrefix;

	/**
	 * Constructs a new output staging area.
	 * 
	 * @param destination The destination. Must be non-null and an existing
	 *            directory.
	 */
	public MapStagingTool(File destination, String imagePrefix) {
		Validate.notNull(destination);
		Validate.isTrue(destination.isDirectory());
		this.destination = destination;

		if (imagePrefix != null) {
			this.imagePrefix = imagePrefix;
		} else {
			this.imagePrefix = "image";
		}
	}

	/**
	 * Add a map to the staging directory.
	 * 
	 * @param mapName The name of the map. The .tmx extension will be appended.
	 * @param realmId The ID of the realm toe xport.
	 * @throws IOException If something went wrong.
	 */
	public File writeMap(String mapName, int realmId) throws IOException {
		// Verify map name is not null and ends with tmx.
		Validate.notNull(mapName);
		mapName = mapName + TMX_FILE_EXTENSION;

		// Write the map file out
		File mapFile = new File(destination, mapName);
		FileOutputStream outStream = new FileOutputStream(mapFile);
		MapExport.processAndExportMapTMX(realmId, outStream, mapName, imagePrefix, MapDataType.TMX_BASE64);
		outStream.flush();
		outStream.close();
		return mapFile;
	}

	/**
	 * Given a specific map, retrieves all of them and writes them to staging
	 * area.
	 * 
	 * @param realmId The ID of the realm to pull from.
	 * @throws IOException if something goes wrong.
	 * @throws SQLException if something goes wrong while retrieving the data.
	 */
	public List<File> writeAllTilesetsFromRealm(int realmId) throws IOException, SQLException {
		// Get a list of all tilesets
		List<Integer> tilesetList = TilesetExport.getAllTilesetsForRealm(realmId);
		List<File> filesWritten = new ArrayList<File>(tilesetList.size());

		// For each tileset, write to a new file and
		for (Integer tileset : tilesetList) {
			File imageFile = new File(destination, imagePrefix + tileset + FILE_EXT);
			TilesetExport.writeTilesetToFile(tileset, imageFile);
			filesWritten.add(imageFile);
		}
		// Return the built list
		return filesWritten;
	}

	/**
	 * Takes everything deployed to the destination and zips it up into a
	 * single, compressed archive. Right now, this is NOT done recursively.
	 * 
	 * @param target The target to write the zip file to. Cannot be null.
	 * @throws IOException if something goes wrong.
	 */
	public void zipUp(File target) throws IOException {
		Validate.notNull(target);
		FileOutputStream fileOut = new FileOutputStream(target);
		ZipOutputStream zipOut = new ZipOutputStream(fileOut);

		// Iterate through the destination directory's files
		for (File entry : destination.listFiles()) {
			if (entry.isFile()) {
				// Dump the file into the zip
				ZipEntry zipEntry = new ZipEntry(entry.getName());
				zipOut.putNextEntry(zipEntry);
				FileUtils.dumpFileToStream(entry, zipOut);
				zipOut.closeEntry();
			}
		}

		zipOut.flush();
		zipOut.close();
	}
}

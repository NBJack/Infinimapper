package org.rpl.infinimapper.data.export;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.context.ApplicationContext;

/**
 * Exports data with regards to melonJS.
 * 
 * @author Ryan
 * 
 */
public class MelonJsExporter {

	private static final String MAP_TYPE = "tmx";
	private static final String IMAGE_TYPE = "image";
	private static final String RESSOURCE_FILE = "ressources.js";
	private static final String RESOURCE_IDENTIFIER = "name";
	private static final String DATA_DIR_NAME = "data";
	private static final String FILE_EXT = "png";
	private static final String IMAGE_PREFIX = "tileset";

	private final File dataDir;

	TypeToken<List<Map<String, String>>> token = new TypeToken<List<Map<String, String>>>() {
	};

	/**
	 * Determines any resources to list.
	 */
	private ArrayList<HashMap<String, String>> resources;
	private File destination;

	public MelonJsExporter(File destination) {
		Validate.notNull(destination);
		Validate.isTrue(destination.isDirectory(), "The destination must be a directory.");
		this.destination = destination;
		this.resources = new ArrayList<HashMap<String, String>>();
		this.dataDir = new File(destination, DATA_DIR_NAME);
		this.dataDir.mkdir();
	}

	/**
	 * Read in resources from a JSON-formatted file that gives the 'stock' data
	 * for the Melon environment.
	 * 
	 * @param source The source file. Must not be null and must exist.
	 * @throws IOException when something goes wrong.
	 */
	public void addResourcesFromFile(File source) throws IOException {
		Validate.notNull(source);
		Validate.isTrue(source.exists(), "The file specified '" + source.getAbsolutePath() + "' doesn't exist.");

		// Deserialize it from the file provided.
		FileReader fileReader = new FileReader(source);
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		ArrayList<HashMap<String, String>> availableResources = gson.fromJson(fileReader, token.getType());

		// Pump it into the listed resources.
		this.resources.addAll(availableResources);
	}

	/**
	 * Get the current list of resources.
	 * 
	 * @return the resources as known.
	 */
	public ArrayList<HashMap<String, String>> getResources() {
		return resources;
	}

	private boolean doesEntryExist(String name) {
		Validate.notNull(name);
		for (HashMap<String, String> resource : resources) {
			if (name.equals(resource.get(RESOURCE_IDENTIFIER))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get rid of duplicates in the resource map. This
	 */
	public void cullDuplicates() {
		HashMap<String, HashMap<String, String>> dupeMap = new LinkedHashMap<String, HashMap<String, String>>();

		for (HashMap<String, String> entry : resources) {
			dupeMap.put(entry.get(RESOURCE_IDENTIFIER), entry);
			System.out.println(entry.get(RESOURCE_IDENTIFIER) + " - " + dupeMap.size());
		}

		// Replace the resource list contents with the de-duped ones.
		resources.clear();
		resources.addAll(dupeMap.values());
	}

	/**
	 * Add a map and its resources to the staging area along with the resource
	 * entries.
	 * 
	 * @param realmId
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addMap(int realmId) throws SQLException, IOException {
		String mapName = "map" + realmId;
		MapStagingTool mapOut = new MapStagingTool(dataDir, IMAGE_PREFIX);
		File mapFile = mapOut.writeMap(mapName, realmId);

		// Add to the resources
		addImagesFromMap(realmId);
		HashMap<String, String> resource = new HashMap<String, String>();
		resource.put("name", mapName);
		resource.put("type", MAP_TYPE);
		resource.put("src", dataDir.getName() + "/" + mapFile.getName());
		resources.add(resource);
	}

	/**
	 * Add all images from the specified map, noting their resource entries.
	 * 
	 * @param realmId
	 * @throws SQLException
	 * @throws IOException
	 */
	public void addImagesFromMap(int realmId) throws SQLException, IOException {
		// Get a list of all tilesets
		List<Integer> tilesetList = TilesetExport.getAllTilesetsForRealm(realmId);

		// For each tileset, write to a new file and
		for (Integer tileset : tilesetList) {
			String imageName = IMAGE_PREFIX + tileset;
			// Did we already generate that at some point?
			if (!doesEntryExist(imageName)) {
				File imageFile = new File(dataDir, IMAGE_PREFIX + tileset + "." + FILE_EXT);
				TilesetExport.writeTilesetToFile(tileset, imageFile);
				// Note the file
				HashMap<String, String> resource = new HashMap<String, String>();
				resource.put(RESOURCE_IDENTIFIER, imageName);
				resource.put("type", IMAGE_TYPE);
				resource.put("src", dataDir.getName() + "/" + imageFile.getName());
				resources.add(resource);
			}
		}

	}

	/**
	 * Get the directory the data will be put in.
	 * 
	 * @return
	 */
	public File getDataDir() {
		return dataDir;
	}

	/**
	 * Write the known resources to the destination, using the default internal
	 * filename.
	 * 
	 * @return The constructed file.
	 * @throws IOException
	 */
	public File writeResources() throws IOException {
		return writeResources(RESSOURCE_FILE);
	}

	/**
	 * Write the known resources to the destination.
	 * 
	 * @param targetName The name of the target.
	 * @return The constructed file.
	 * @throws IOException
	 */
	public File writeResources(String targetName) throws IOException {
		Validate.notNull(targetName);
		File resourceFile = new File(destination, targetName);
		FileWriter fileOut = new FileWriter(resourceFile);
		// Cull the herd of dupes
		cullDuplicates();
		// Write the boilerplate (just keep in mind that they use the term
		// 'ressources')
		fileOut.write("/** Generated Resources */\n");
		fileOut.write("var g_ressources = \n");

		// Write out each JSON entry
		Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
		fileOut.write(gson.toJson(resources));
		fileOut.write(";");
		// Tidy up the output
		fileOut.flush();
		fileOut.close();
		return resourceFile;
	}

	/**
	 * Recursively pulls in the entire file directory into the destination.
	 * 
	 * @param source
	 * @throws IOException
	 */
	public void pullInTemplate(File source) throws IOException {
		Validate.notNull(source);
		Validate.isTrue(source.exists());
		Validate.isTrue(source.isDirectory());

		FileUtils.copyDirectory(source, destination);
	}

}

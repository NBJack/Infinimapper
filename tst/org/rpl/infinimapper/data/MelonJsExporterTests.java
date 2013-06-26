package org.rpl.infinimapper.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.export.MelonJsExporter;

public class MelonJsExporterTests {

	private static final int TEST_REALM_ID = 7;
	private static final File TEST_RESOURCE_FILE = new File("TestData\\melonResources.json");
	private static final File TEST_EXAMPLE_RESOURCE_FILE = new File("TestData\\fromExample.json");
	private static final File TEST_EXAMPLE_TEMPLATE_DIR = new File("TestData\\exampleTemplate");

	HashMap<String, String> testData1;
	HashMap<String, String> testData2;

	MelonJsExporter exporter;

	@BeforeClass
	public static void setup() {
		DBSetupUtil.testSetupDatabase();
	}

	@Before
	public void setupExporter() {
		// File destination = FileUtils.generateTempDirLocation();
		// destination.mkdir();
		File destination = new File("ScratchDir");
		exporter = new MelonJsExporter(destination);
		testData1 = new HashMap<String, String>();
		testData1.put("name", "arg");
		testData1.put("type", "txt");
		testData1.put("src", "data\\secret.txt");
		testData2 = new HashMap<String, String>();
		testData2.put("name", "player");
		testData2.put("type", "png");
		testData2.put("src", "data\\player.png");
	}

	@Test
	public void testImageExport() throws SQLException, IOException {
		exporter.addImagesFromMap(TEST_REALM_ID);
		// Validate
		File dataDir = exporter.getDataDir();
		Assert.assertTrue(dataDir.listFiles().length > 0);
	}

	@Test
	public void testPropertiesRead() throws IOException {
		// Make sure nothing is in the resource file before we begin
		Assert.assertTrue(exporter.getResources().isEmpty());
		// Pump data from the file
		exporter.addResourcesFromFile(TEST_RESOURCE_FILE);
		// Test what was retrieved
		Assert.assertTrue(!exporter.getResources().isEmpty());
		Assert.assertEquals(3, exporter.getResources().size());
		HashMap<String, String> resource = exporter.getResources().get(1);
		Assert.assertEquals("PNG", resource.get("type"));
		Assert.assertEquals("player", resource.get("name"));
	}

	@Test
	public void testPropertiesWrite() throws IOException {
		exporter.getResources().add(testData1);
		exporter.getResources().add(testData2);
		File resourceFile = exporter.writeResources("testPropertyWrite.js");
		Assert.assertTrue(resourceFile.exists());
	}

	@Test
	public void testPropertyAugment() throws IOException {
		exporter.addResourcesFromFile(TEST_RESOURCE_FILE);
		exporter.getResources().add(testData1);
		exporter.getResources().add(testData2);
		Assert.assertEquals(5, exporter.getResources().size());
		File finalResource = exporter.writeResources("testPropertyAugment.js");
		Assert.assertEquals(4, exporter.getResources().size());
		Assert.assertTrue(finalResource.exists());
	}

	@Test
	public void testExampleProperties() throws IOException {
		exporter.addResourcesFromFile(TEST_EXAMPLE_RESOURCE_FILE);
		ArrayList<HashMap<String, String>> resources = exporter.getResources();
		Assert.assertEquals(12, resources.size());
	}

	@Test
	public void testPullInTemplate() throws IOException {
		exporter.pullInTemplate(TEST_EXAMPLE_TEMPLATE_DIR);
		exporter.writeResources("testPullIn.js");
	}

	@Test
	public void testItAll() throws IOException, SQLException {
		exporter.addResourcesFromFile(TEST_EXAMPLE_RESOURCE_FILE);
		exporter.pullInTemplate(TEST_EXAMPLE_TEMPLATE_DIR);
		exporter.addMap(TEST_REALM_ID);
		exporter.writeResources();
	}
}

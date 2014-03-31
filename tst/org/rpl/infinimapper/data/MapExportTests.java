package org.rpl.infinimapper.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.MapDataType;
import org.rpl.infinimapper.data.export.MapExport;

public class MapExportTests {

    public static final int MAP_ID = 52;

    @BeforeClass
	public static void setup() {
		DBSetupUtil.testSetupDatabase();
	}

	@Test
	public void testExport() throws IOException {
		File testMap = File.createTempFile("out", ".tmx");
		FileOutputStream fileOut = new FileOutputStream(testMap);
        //TODO: Re-enable these tests
        MapExport mapExporter = new MapExport();
		mapExporter.processAndExportMapTMX(MAP_ID, fileOut, testMap.getName(), "image", MapDataType.TMX_BASE64);
		fileOut.flush();
		fileOut.close();
		// Verify contents
		Assert.assertTrue(testMap.exists());
		System.out.println("Testmap: " + testMap.getAbsolutePath());
	}


}

package org.rpl.infinimapper.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.export.MapStagingTool;
import org.rpl.util.FileUtils;

public class MapStagingToolTest {

	@BeforeClass
	public static void setup() {
		DBSetupUtil.testSetupDatabase();
	}

	@Test
	public void testExportEverything() throws IOException, SQLException {
		File stagingArea = FileUtils.generateTempDirLocation();
		stagingArea.mkdir();
		MapStagingTool stager = new MapStagingTool(stagingArea, "testImage");

		stager.writeMap("testMap.tmx", 34);
		stager.writeAllTilesetsFromRealm(34);
		System.out.println("Staging area: " + stagingArea.getAbsolutePath());
	}

}

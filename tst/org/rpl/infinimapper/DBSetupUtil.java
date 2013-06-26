package org.rpl.infinimapper;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Initializes the database connections for test use.
 * 
 * @author Ryan
 * 
 */
public class DBSetupUtil {

	private static final String TEST_SETUP_FILE = "WebContent\\META-INF\\setup.properties";
	private static boolean isSetup = false;

	/**
	 * Convenience method that lazily wraps all possible bad outcomes in an
	 * exception and throws a runtime exception. Meant only for testing. Will
	 * not re-init if already setup.
	 */
	public static void testSetupDatabase() {
		if (isSetup) {
			return;
		}
		try {
			setupDatabase();
			isSetup = true;
		} catch (Exception ex) {
			throw new RuntimeException("Failure to setup database", ex);
		}
	}

	public static void setupDatabase() throws FileNotFoundException, IOException, PropertyVetoException, SQLException {
		Properties props = new Properties();
		File setupFile = new File(TEST_SETUP_FILE);
		System.out.println("Setup file location: " + setupFile.getAbsolutePath());
		props.load(new FileReader(setupFile));

		DBResourceManager.setupFromProperties(props);
	}

}

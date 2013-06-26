package org.rpl.infinimapper;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import org.rpl.infinimapper.security.AuthMan;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Application Lifecycle Listener implementation class DBResourceManager
 * 
 */
public class DBResourceManager implements ServletContextListener {

    /**
	 * The 'master' connection pool.
	 */
	private static final ComboPooledDataSource commPool;
    private static final DataSourceConnectionSource ormSource;
    public static final String DB_JDBC_URL = "db.JdbcURL";
    private static Properties setupProperties;

	static {
		commPool = new ComboPooledDataSource();
        try {
            ormSource = new DataSourceConnectionSource(commPool, new MysqlDatabaseType());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize the data source.");
        }
    }


	/**
	 * Grabs a connection from the common pool. Ensure that it and anything
	 * derived from it are closed correctly. Don't worry about concurrency here;
	 * the connection pool will take care of that
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return commPool.getConnection();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		commPool.close();

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		// Load the configuration

		try {

			// Load the properties file
			// TODO: Set this up via Spring instead. This is a horribly dated
			// way of doing it.

			Properties dbConfig = new Properties();
			dbConfig.load(arg0.getServletContext().getResourceAsStream("META-INF/setup.properties"));

			setupFromProperties(dbConfig);

		} catch (Exception ex) {
			// This is a failure to load.

			System.err.println("Error during intialization of the database resource manager: " + ex.toString());
			ex.printStackTrace();

		}

	}

	/**
	 * Initialize the database system.
	 * 
	 * @param dbConfig
	 * @throws PropertyVetoException
	 * @throws SQLException
	 */
	public static void setupFromProperties(Properties dbConfig) throws PropertyVetoException, SQLException {
        setupProperties = new Properties(dbConfig);
		commPool.setDriverClass(dbConfig.getProperty("db.driver"));
		commPool.setJdbcUrl(dbConfig.getProperty(DB_JDBC_URL));
		commPool.setUser(dbConfig.getProperty("db.user"));
		commPool.setPassword(dbConfig.getProperty("db.password"));
		commPool.setMaxIdleTime(Integer.parseInt(dbConfig.getProperty("db.maxIdleTime", "180")));

		AuthMan.setSalt(dbConfig.getProperty("pw.finalsalt"));

		// Test the database

		Connection testConnection = getConnection();
		Statement testStatement = testConnection.createStatement();
		testStatement.execute("SELECT count(1)");
		testStatement.close();
		testConnection.close();
	}

	/**
	 * Retrieves the underlying Data Source. Useful if another package needs the
	 * details.
	 * 
	 * @return
	 */
	public static ComboPooledDataSource getDataSource() {
		return commPool;
	}

    /**
     * Retrieves the object relational management connection source, which is attached to the underlying pool.
     * @return
     */
    public static DataSourceConnectionSource getConnectionSource() {
        return ormSource;
    }
}

package org.rpl.infinimapper.data.management;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.DBResourceManager;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.Layer;
import com.j256.ormlite.dao.BaseDaoImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * A data provider for Layers.
 *
 * User: Ryan
 * Date: 2/1/13 - 6:59 PM
 */
public class LayerDataProvider extends DaoDataProvider<Integer, Layer>{


    /**
     * Create a new layer data provider.
     * @throws SQLException if initialization goes awry with the SQL.
     */
    public LayerDataProvider() throws SQLException {
        super(Layer.class);
    }

    /**
     * Get a comprehensive list of layers for a given realm. Realms will be ordered by their
     * assigned oder number.
     *
     * @param realmid The realm to serch for.
     * @return A list of realms if found.
     * @throws SQLException if something goes wrong with the SQL.
     */
    public List<Layer> getLayersForRealm(int realmid) throws SQLException {
        return this.getQueryBuilder()
                .orderBy("ordernum", true)
                .where()
                .eq("masterrealmid", realmid)
                .query();
    }

}

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

/**
 * A simple data provider for Layers.
 * User: Ryan
 * Date: 2/1/13 - 6:59 PM
 */
public class LayerDataProvider extends DataProvider<Integer, Layer>{


    private Dao<Layer, Integer> dao;

    public LayerDataProvider() throws SQLException {
        dao = BaseDaoImpl.createDao(DBResourceManager.getConnectionSource(), Layer.class);
    }

    @Override
    public Layer getValue(Integer integer) {

        try {
            Layer result = dao.queryForId(integer);
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Nothing to return
        return null;
    }

    @Override
    public void putValue(Integer integer, Layer layer) {

        try {
            if ( layer.getId() == Layer.UNASSIGNED_ID ) {
                Validate.isTrue(integer == null);
                dao.create(layer);
            } else {
                Validate.isTrue(integer.intValue() == layer.getId());
                dao.update(layer);
            }
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }
}

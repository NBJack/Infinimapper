package org.rpl.infinimapper.data.management;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.DBResourceManager;
import org.rpl.infinimapper.data.Identable;

import java.sql.SQLException;
import java.util.Collections;

/**
 * A generic Dao-backed data provider.
 * User: Ryan
 * Date: 2/13/13 - 12:59 PM
 */
public class DaoDataProvider<Key, Value extends Identable<Key>>  extends DataProvider<Key, Value>{

    /**
     * The backing Data Access Object class.
     */
    private Dao<Value, Key> dao;

    public DaoDataProvider(Class clazz) throws SQLException {
        dao = BaseDaoImpl.createDao(DBResourceManager.getConnectionSource(), clazz);
    }

    /**
     * {@inheritDoc}
     * @param key
     * @return
     */
    @Override
    public Value getValue(Key key) {

        try {
            Value result = dao.queryForId(key);
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Nothing to return
        return null;
    }

    @Override
    public void putValue(Key key, Value value) {

        try {
            if ( !value.hasId() ) {
                Validate.isTrue(key == null);
                dao.create(value);
            } else {
                Validate.isTrue(value.getID().equals(value));
                dao.update(value);
            }
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }


    public void deleteValue(Key key) {

        try {
            dao.deleteIds(Collections.singleton(key));
        } catch (SQLException ex ) {
            ex.printStackTrace();
        }
    }


}

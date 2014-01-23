package org.rpl.infinimapper.data.management;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import org.rpl.infinimapper.data.ObjectInstance;
import org.rpl.infinimapper.data.TilesetData;

import java.sql.SQLException;
import java.util.List;

/**
 * User: Ryan
 * Date: 5/25/13 - 10:45 PM
 */
public class TilesetProvider extends DaoDataProvider<Integer, TilesetData> {

    public static final String WDB_TILESET_LIST = "SELECT NAME, id FROM tilelib";

    public TilesetProvider() throws SQLException {
        super(TilesetData.class);
    }

    /**
     * Get a complete list of all tilesets.
     * @return
     * @throws SQLException
     */
    public List<TilesetData> getListOfTilesets() throws SQLException {
        return this.getQueryBuilder().query();
    }


}

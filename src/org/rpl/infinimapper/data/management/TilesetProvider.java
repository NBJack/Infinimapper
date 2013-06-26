package org.rpl.infinimapper.data.management;

import org.rpl.infinimapper.data.TilesetData;

import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 5/25/13 - 10:45 PM
 */
public class TilesetProvider extends DaoDataProvider<Integer, TilesetData> {

    public TilesetProvider() throws SQLException {
        super(TilesetData.class);
    }
}

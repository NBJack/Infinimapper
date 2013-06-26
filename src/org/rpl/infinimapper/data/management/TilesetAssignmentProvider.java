package org.rpl.infinimapper.data.management;

import org.rpl.infinimapper.data.TilesetAssignment;

import java.sql.SQLException;

/**
 * Provides tileset assignments to realms.
 * User: Ryan
 * Date: 5/19/13 - 10:37 AM
 */
public class TilesetAssignmentProvider extends DaoDataProvider<Integer, TilesetAssignment>{


    public TilesetAssignmentProvider() throws SQLException {
        super(TilesetAssignment.class);
    }

}

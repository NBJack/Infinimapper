package org.rpl.infinimapper.data.management;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.TilesetAssignment;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides tileset assignments to realms.
 * User: Ryan
 * Date: 5/19/13 - 10:37 AM
 */
public class TilesetAssignmentProvider extends DaoDataProvider<Integer, TilesetAssignment>{


    public TilesetAssignmentProvider() throws SQLException {
        super(TilesetAssignment.class);
    }

    /**
     * Gets a list of all tilesets for a particular realm, ordered by their given ordering.
     * @param realm The realm to look-up. Cannot be null.
     * @return The list of tilesets found.
     * @throws SQLException if something goes wrong running the query.
     */
    public List<TilesetAssignment> getAllTilesetsForRealm(Realm realm) throws SQLException {
        Validate.notNull(realm);
        return this.getQueryBuilder()
                .orderBy("order", true)
                .where()
                .eq("realmId", realm.getId())
                .query();
    }
}

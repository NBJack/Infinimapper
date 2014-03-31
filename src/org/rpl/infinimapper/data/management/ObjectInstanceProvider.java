package org.rpl.infinimapper.data.management;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.ObjectInstance;
import org.rpl.infinimapper.data.Realm;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides access to instances of objects.
 * User: Ryan
 * Date: 5/25/13 - 7:10 PM
 */
public class ObjectInstanceProvider extends DaoDataProvider<Integer, ObjectInstance> {

    public static final String WDB_OBJ_INSERT_QUERY = "INSERT INTO objects VALUE (NULL, ?, ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP, FALSE, ?, ?)";
    public static final String WDB_OBJ_UPDATEPOS_QUERY = "UPDATE objects SET tilerealm=?,tilexcoord=?,tileycoord=?,offsetx=?,offsety=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
    public static final String WDB_OBJ_UPDATEDATA_QUERY = "UPDATE objects SET custom=?,lastupdate=CURRENT_TIMESTAMP WHERE id=?";
    public static final String WDB_OBJ_RETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety, tilexcoord, tileycoord, deleted, width, height FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=?";
    public static final String WDB_OBJ_SINGLE_RETRIEVE_QUERY = "SELECT definition, offsetx, offsety, tilexcoord, tileycoord, deleted, custom, width, height FROM objects WHERE id=?";
    static final String WDB_OBJ_INCRETRIEVE_QUERY = "SELECT id, definition, offsetx, offsety FROM objects WHERE tilerealm=? AND tilexcoord=? AND tileycoord=? AND lastupdate<?";
    public static final String WDB_OBJ_DELETE = "UPDATE objects SET deleted=TRUE,lastupdate=CURRENT_TIMESTAMP WHERE id=?";

    public ObjectInstanceProvider() throws SQLException {
        super(ObjectInstance.class);
    }

    /**
     * Get a list of all objects on the chunk.
     * @param realm the realm in which to search for the objects. Cannot be null.
     * @param tileXCoord the X coordinate of the tile to search.
     * @param tileYCoord the Y coordinate of the tile to search.
     * @return a list of objects found matching the results.
     * @throws SQLException thrown if something goes wrong.
     */
    public List<ObjectInstance> getObjectsOnChunk ( Realm realm, int tileXCoord, int tileYCoord ) throws SQLException {

        Validate.notNull(realm);

        QueryBuilder<ObjectInstance, Integer> query = this.getQueryBuilder();

        return query.where()
            .eq("tileRealm", realm.getId())
            .and()
            .eq("tileXCoord", tileXCoord)
            .and()
            .eq("tileXCoord", tileYCoord).query();
    }

    /**
     * Return a list of objects across the specified realm.
     * @param realmId the realm Id to search for.
     * @return a list of {@link ObjectInstance}s in their natural order.
     * @throws SQLException if something goes wrong with the query.
     */
    public List<ObjectInstance> getObjectsInRealm(int realmId) throws SQLException {
        QueryBuilder<ObjectInstance, Integer> query = this.getQueryBuilder();

        return query.where()
            .eq("tileRealm", realmId)
            .query();
    }
}

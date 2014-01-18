package org.rpl.infinimapper.data.management;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.ObjectIdentity;
import org.rpl.infinimapper.data.ObjectInstance;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides object definitions.
 * TODO: Provide the ability to retrieve a particular group of objects.
 * TODO: Provide the ability to page through objects. Otherwise, this will never scale.
 * User: Ryan
 * Date: 12/22/13 - 10:46 PM
 */
public class ObjectProvider extends DaoDataProvider<Integer, ObjectIdentity> {

    public static final String WDB_GET_OBJLIB = "SELECT id, NAME, tilesrc, imgXOff, imgYOff, imgWidth, imgHeight FROM objlib WHERE id > 0";

    public ObjectProvider() throws SQLException {
        super(ObjectIdentity.class);
    }

    /**
     * Get a list of all object definitions available, excluding 'special' ones. Note this is
     * never going to scale well unless paging is permitted.
     * @return a list of all objects.
     * @throws SQLException if something goes wrong with the query.
     */
    public List<ObjectIdentity> getAllDefinitions() throws SQLException {
        QueryBuilder<ObjectIdentity, Integer> query = this.getQueryBuilder();

        // Skip any special objects with identities <= 0.
        query.where()
                .gt("id", 0);
        PreparedQuery<ObjectIdentity> preparedQuery = query.prepare();


        return runQuery(preparedQuery);

    }

}

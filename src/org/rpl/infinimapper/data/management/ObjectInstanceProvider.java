package org.rpl.infinimapper.data.management;

import org.rpl.infinimapper.data.ObjectInstance;

import java.sql.SQLException;

/**
 * Provides access to instances of objects.
 * User: Ryan
 * Date: 5/25/13 - 7:10 PM
 */
public class ObjectInstanceProvider extends DaoDataProvider<Integer, ObjectInstance> {

    public ObjectInstanceProvider() throws SQLException {
        super(ObjectInstance.class);
    }
}

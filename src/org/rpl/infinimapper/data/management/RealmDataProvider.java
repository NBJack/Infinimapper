package org.rpl.infinimapper.data.management;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.WorldDB;
import org.rpl.infinimapper.WorldDB.QuickCon;
import org.rpl.infinimapper.data.Realm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 1/12/13
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RealmDataProvider extends DaoDataProvider<Integer, Realm> {

    /**
     * Initialize the provider.
     * @throws SQLException
     */
    public RealmDataProvider() throws SQLException {
        super(Realm.class);
    }

    static final String REALM_RETRIEVE_QUERY = "SELECT * FROM realms WHERE id = ? LIMIT 1";
    static final String REALM_UPDATE_QUERY = "UPDATE realms SET name=?, description=?, defaulttile=?, ownerid=?, publicflag=?, sublayer=?, tileWidth=?, tileHeight=? WHERE id=?";
    static final String REALM_CREATE_QUERY = "INSERT INTO realms(name, description, defaulttile, ownerid, publicflag, sublayer, tileWidth, tileHeight, tileset) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static final String REALM_DELETE_QUERY = "DELETE FROM realms WHERE id = ? LIMIT 1";

    /*
    @Override
    public Realm getValue(Integer integer) {
        Realm realm = null;
        QuickCon connection = null;

        try {
            connection = new WorldDB.QuickCon(REALM_RETRIEVE_QUERY);
            connection.getStmt().setInt(1, integer.intValue());

            ResultSet set = connection.query();
            List<Realm> realms = SimpleDeserializer.deserializeFromSet(set, Realm.class);
            if ( !realms.isEmpty() ) {
                return realms.get(0);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if ( connection != null ) {
                connection.release();
            }
        }

        // Either not available or something went wrong. Return null.
        return null;
    }*/

    /**
     * Adds or updates a realm. For now, this is a no-op.
     * TODO: Add in editing functionality.
     * @param key Specifies the key for the value. Must match what the realm has.
     * @param realm
     */
    @Override
    public void putValue(Integer key, Realm realm) {
        Validate.notNull(realm);
        WorldDB.QuickCon con = null;
        // Is this a new realm or an existing one?
        try {
        if ( realm.getId() != Realm.UNSPECIFIED_ID ) {

            // Realm already exists
            Validate.notNull(key);
            Validate.isTrue(key.intValue() == realm.getId());
            con = new QuickCon(REALM_UPDATE_QUERY);
            con.getStmt().setString(1, realm.getName());
            con.getStmt().setString(2, realm.getDescription());
            con.getStmt().setInt(3, realm.getDefaulttile());
            con.getStmt().setInt(4, realm.getOwnerid());
            con.getStmt().setBoolean(5, realm.getPublic());
            con.getStmt().setBoolean(6, realm.isSublayer());
            con.getStmt().setInt(7, realm.getTileWidth());
            con.getStmt().setInt(8, realm.getTileHeight());
            // Set the ID
            con.getStmt().setInt(9, realm.getId());

            con.getStmt().execute();


        } else {

            // New Realm
            Validate.isTrue(key == null);
            con = new QuickCon(REALM_CREATE_QUERY);
            con.getStmt().setString(1, realm.getName());
            con.getStmt().setString(2, realm.getDescription());
            con.getStmt().setInt(3, realm.getDefaulttile());
            con.getStmt().setInt(4, realm.getOwnerid());
            con.getStmt().setBoolean(5, realm.getPublic());
            con.getStmt().setBoolean(6, realm.isSublayer());
            con.getStmt().setInt(7, realm.getTileWidth());
            con.getStmt().setInt(8, realm.getTileHeight());
            con.getStmt().setInt(9, realm.getTileset());

            con.getStmt().execute();
            ResultSet results = con.getStmt().getGeneratedKeys();

            if ( results.next() ) {
                int assignedKey = results.getInt(1);
                realm.setId(assignedKey);
            } else {
                throw new IllegalArgumentException("Could not generate the realm");
            }
        }
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        } finally {
            if ( con != null ) {
                con.release();
            }
        }
    }

    public void deleteValue(Integer key) {

        WorldDB.QuickCon con = null;
        try {
            con = new QuickCon(REALM_DELETE_QUERY);

            con.getStmt().setInt(1, key);
            con.getStmt().execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if ( con != null ) {
                con.release();
            }
        }
    }
}

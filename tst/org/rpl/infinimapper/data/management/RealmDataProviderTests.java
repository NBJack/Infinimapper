package org.rpl.infinimapper.data.management;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.management.RealmDataProvider;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 1/29/13 - 1:00 PM
 */
public class RealmDataProviderTests {

    private RealmDataProvider realmProvider;

    private Integer TEST_REALM_EXISTS = 34;

    @BeforeClass
    public static void setupSuite() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        realmProvider = new RealmDataProvider();
    }

    @Test
    public void testWrites() {
        Realm realm = new Realm();
        realm.setName("Test Realm");
        realm.setDescription("A test realm created to determine the data provider's viability.");
        realm.setTileset(2);
        realm.setDefaulttile(-1);
        realm.setPublic(true);
        Assert.assertTrue(realm.getId() == Realm.UNSPECIFIED_ID);
        realmProvider.putValue(null, realm);
        Assert.assertFalse(realm.getId() == Realm.UNSPECIFIED_ID);
        // Now, see if we can actually retrieve it
        Realm fetchedRealm = realmProvider.getValue(realm.getId());
        Assert.assertEquals(realm, fetchedRealm);
        // Try to update it
        fetchedRealm.setDescription("A modified realm.");
        Assert.assertFalse(fetchedRealm.equals(realm));
        realmProvider.putValue(fetchedRealm.getId(), fetchedRealm);
        Realm modifiedRealm = realmProvider.getValue(fetchedRealm.getId());
        Assert.assertEquals(fetchedRealm.getDescription(), modifiedRealm.getDescription());
        // Finally, try to delete it
        realmProvider.deleteValue(fetchedRealm.getId());
        Realm deletedRealm = realmProvider.getValue(fetchedRealm.getId());
        Assert.assertNull(deletedRealm);
    }

    @Test
    public void testRead() {
        Realm realm = realmProvider.getValue(TEST_REALM_EXISTS);
        Assert.assertNotNull(realm);
        Assert.assertEquals(TEST_REALM_EXISTS.intValue(), realm.getId());
    }
}

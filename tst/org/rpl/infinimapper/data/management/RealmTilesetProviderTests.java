package org.rpl.infinimapper.data.management;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.RealmTileset;
import org.rpl.infinimapper.data.management.DaoDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 2/15/13 - 12:13 PM
 */
public class RealmTilesetProviderTests {

    public static final int TEST_KEY = 1;
    DaoDataProvider<Integer, RealmTileset> realmTilesetProvider;


    @BeforeClass
    public static void setupTests() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        realmTilesetProvider = new DaoDataProvider<Integer, RealmTileset>(RealmTileset.class);
    }


    @Test
    public void testRead() {
        RealmTileset tileset = realmTilesetProvider.getValue(TEST_KEY);
        Assert.assertNotNull(tileset);
        Assert.assertTrue(tileset.hasId());
        Assert.assertEquals(TEST_KEY, tileset.getId());
    }
}

package org.rpl.infinimapper.data;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.ObjectIdentity;
import org.rpl.infinimapper.data.management.DaoDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Tests the use of the @{link ObjectIdentity} object for use with a DAO provider.
 * User: Ryan
 * Date: 3/22/13 - 9:53 AM
 */
public class ObjectIdentityTests {


    public static final int TEST_KEY = 1;
    public static final int TEST_TILE_SIZE = 32;
    DaoDataProvider<Integer, ObjectIdentity> objectIdentityProvider;


    @BeforeClass
    public static void setupTests() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        objectIdentityProvider = new DaoDataProvider<Integer, ObjectIdentity>(ObjectIdentity.class);
    }


    @Test
    public void testReads() {
        ObjectIdentity testIdentity = objectIdentityProvider.getValue(TEST_KEY);
        Assert.assertNotNull(testIdentity);
        Assert.assertEquals(TEST_KEY, testIdentity.getId());
    }


    @Test
    public void testWrites() {

        // Create a new identity
        ObjectIdentity createdIdent = new ObjectIdentity();
        createdIdent.setName("Test Identity");
        createdIdent.setTilesrc(1);
        createdIdent.setImgWidth(TEST_TILE_SIZE);
        createdIdent.setImgHeight(TEST_TILE_SIZE);
        try {
            // Save it to the database
            objectIdentityProvider.putValue(null, createdIdent);
            Assert.assertTrue(createdIdent.hasId());

            // Check that it can be retrieved
            ObjectIdentity retrievedIdent = objectIdentityProvider.getValue(createdIdent.getID());
            Assert.assertEquals(createdIdent.getId(), retrievedIdent.getId());
            Assert.assertEquals(createdIdent.getName(), retrievedIdent.getName());
        } finally {
            // Always clean-up
            if ( createdIdent.hasId() ) {
                objectIdentityProvider.deleteValue(createdIdent.getID());
            }
        }
    }
}

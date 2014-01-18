package org.rpl.infinimapper.data.management;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.ChunkData;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.management.ChunkDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 1/27/13 - 2:52 PM
 */
public class ChunkDataProviderTests {

    public static final int TEST_REALM_ID = 34;
    private static Realm TEST_REALM = new Realm(TEST_REALM_ID);
    public static final ChunkKey TEST_CHUNK_KEY = new ChunkKey(TEST_REALM, 0, 0);

    private ChunkDataProvider provider;


    @BeforeClass
    public static void setup() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
        TEST_REALM = new Realm();
        TEST_REALM.setId(TEST_REALM_ID);

    }

    @Before
    public void setupProvider() throws SQLException {
        // Setup what we need for the chunk
        this.provider = new ChunkDataProvider();
    }

    @Test
    public void testReadChunk() {
        Chunk chunk = provider.getValue(TEST_CHUNK_KEY);
        Assert.assertEquals(TEST_CHUNK_KEY, chunk.getId());
        Assert.assertNotNull(chunk.getData());
        Assert.assertEquals(ChunkData.TILES_HEIGHT_IN_CHUNK * ChunkData.TILES_WIDTH_IN_CHUNK, chunk.getTiles().length);
    }

}

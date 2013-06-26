package org.rpl.infinimapper.data.inbound;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.data.MemoryChunkProvider;
import org.rpl.infinimapper.data.MemoryRealmProvider;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.management.ChunkCache;
import org.rpl.infinimapper.data.management.RealmCache;

/**
 * User: Ryan
 * Date: 1/25/13 - 5:57 PM
 */
public class MapDeltaCanvasTests {

    private static Realm TEST_REALM = new Realm();
    private static Realm OTHER_REALM = new Realm();

    private MemoryRealmProvider realmProvider;
    private RealmCache realmCache;
    private MemoryChunkProvider chunkProvider;
    private ChunkCache chunkCache;
    private MapDeltaCanvas deltaCanvas;

    @BeforeClass
    public static void setupStatics() {
        TEST_REALM.setDefaulttile(-1);
        TEST_REALM.setId(17);
        OTHER_REALM.setDefaulttile(3);
        OTHER_REALM.setId(41);
    }

    @Before
    public void setup() {
        realmProvider = new MemoryRealmProvider();
        realmProvider.putValue(TEST_REALM.getId(), TEST_REALM);
        realmProvider.putValue(OTHER_REALM.getId(), OTHER_REALM);
        realmCache = new RealmCache(realmProvider, false);
        chunkProvider = new MemoryChunkProvider();
        chunkCache = new ChunkCache(chunkProvider, false, realmCache);

        deltaCanvas = new MapDeltaCanvas(TEST_REALM, chunkCache);
    }

    @Test
    public void testWrites () {
        Assert.assertEquals(0, deltaCanvas.pendingSize());
        Assert.assertEquals(0, chunkProvider.getMap().size());
        deltaCanvas.writeTile(5, 5, 7);
        Assert.assertEquals(1, deltaCanvas.pendingSize());
        deltaCanvas.writeTile(-90, -90, 7);
        Assert.assertEquals(2, deltaCanvas.pendingSize());
        deltaCanvas.flush();
        chunkCache.flushCacheChanges();
        Assert.assertEquals(2, chunkProvider.getMap().size());
        Assert.assertEquals(0, deltaCanvas.pendingSize());
    }

}

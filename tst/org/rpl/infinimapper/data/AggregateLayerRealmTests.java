package org.rpl.infinimapper.data;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBResourceManager;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.Layer;
import org.rpl.infinimapper.data.RealmTileset;
import org.rpl.infinimapper.data.management.DaoDataProvider;
import org.rpl.infinimapper.data.management.LayerDataProvider;
import org.rpl.infinimapper.data.management.RealmDataProvider;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 2/9/13 - 10:53 AM
 */
public class AggregateLayerRealmTests {

    private static final int REALM_COUNT = 3;
    private LayerDataProvider layerProvider;
    private RealmDataProvider realmProvider;
    private DaoDataProvider<Integer, RealmTileset> realmTilesetProvider;


    @BeforeClass
    public static void setupTests() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        realmProvider = new RealmDataProvider();
        layerProvider = new LayerDataProvider();
        realmTilesetProvider = new DaoDataProvider<Integer, RealmTileset>(RealmTileset.class);
    }


    @Test
    public void testMultiLayerRealm() {

        // Setup our test storage structures here as we'll need to clean-up if something goes wrong. Someday, we won't
        // have to do this. Until this, no messing up the database!
        Layer [] layers = new Layer[REALM_COUNT];
        RealmTileset tileset = null;
        Realm [] realms = new Realm[REALM_COUNT];

        // Create 3 realms
        try {
            for ( int i = 0; i < REALM_COUNT; i++ ) {
                Realm realm = new Realm();
                realms[i] = realm;
                realm.setDefaulttile(-1);
                realm.setName("TestRealm" + i);
                realm.setDescription("A test realm");
                realm.setPublic(true);
                // All realms past the first one need to be setup as a sublayer
                realm.setSublayer(i != 0);
                realm.setTileWidth(32);
                realm.setTileHeight(32);

                realmProvider.putValue(null, realm);
            }

            // Now, insert some layers
            for ( int i = 0; i < REALM_COUNT; i++ ) {
                Layer layer = new Layer();
                layers[i] = layer;
                layer.setDefaultvisibility(true);
                layer.setMasterrealmid(realms[0].getId());
                layer.setRealmid(realms[i].getId());
                layer.setName("Layer" + i);
                layerProvider.putValue(null, layer);
            }

            // Add a tileset
            tileset = new RealmTileset();
            tileset.setRealmid(realms[0].getId());
            tileset.setTilesetid(22);
            tileset.setOrder(1);
            realmTilesetProvider.putValue(null, tileset);
        } finally {
            // TODO: Cleanup

        }
    }
}

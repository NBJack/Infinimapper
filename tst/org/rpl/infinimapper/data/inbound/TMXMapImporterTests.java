package org.rpl.infinimapper.data.inbound;

import org.apache.commons.lang3.Validate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.ObjectInstance;
import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.management.*;
import org.rpl.infinimapper.data.management.meta.MapDataProviders;
import tiled.core.MapLayer;
import tiled.core.TileLayer;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: Ryan
 * Date: 1/27/13 - 3:05 PM
 */
public class TMXMapImporterTests {

    private static final String GOOD_FILE_NAME = "TestData\\map7.tmx";
    private static final String TINY_FILE_NAME = "TestData\\tinyTest.tmx";
    private Point ROOT_ORIGIN = new Point(0,0);
    private MapDataProviders providers;

    @BeforeClass
    public static void setup() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setupCaches() throws SQLException {
        providers = MapDataProviders.generateProvider(false);
    }

    protected Realm setupOutputRealm() {
        Realm outputRealm = new Realm();
        outputRealm.setPublic(true);
        outputRealm.setTileset(9);
        outputRealm.setSublayer(false);
        outputRealm.setName("ImportedRealm");
        outputRealm.setDescription("Test TMX import");

        return outputRealm;
    }

    @Test
    public void testImporterTiny() throws Exception, MapProcessingException {
        TMXMapImporter importer = new TMXMapImporter(TINY_FILE_NAME, ROOT_ORIGIN, providers);

        importer.setName("TestImport Tiny - " + new Date());
        importer.processMap(true, -1);

        providers.realms().flushCacheChanges();
        providers.chunks().flushCacheChanges();
    }

    @Test
    public void testImporterNormal() throws Exception, MapProcessingException {
        TMXMapImporter importer = new TMXMapImporter(GOOD_FILE_NAME, ROOT_ORIGIN, providers);

        importer.setName("TestImport Normal - " + new Date());
        importer.processMap(true, -1);

        providers.realms().flushCacheChanges();
        providers.chunks().flushCacheChanges();
    }

}

package org.rpl.infinimapper.data.management;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.Layer;
import org.rpl.infinimapper.data.management.LayerDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 2/7/13 - 6:48 PM
 */
public class LayerDataProviderTests {

    private LayerDataProvider provider;
    private int TEST_LAYER_ID = 12;

    @BeforeClass
    public static void setup() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setupClass() throws SQLException {
        provider = new LayerDataProvider();
    }

    @Test
    public void testGet() {
        Layer layer = provider.getValue(TEST_LAYER_ID);
        Assert.assertNotNull(layer);
        Assert.assertEquals(TEST_LAYER_ID, layer.getId());
    }
}
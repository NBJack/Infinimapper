package org.rpl.infinimapper.data;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.management.DaoDataProvider;
import org.rpl.infinimapper.data.management.TilesetAssignmentProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 3/25/14
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TilesetAssignmentTests {



    @BeforeClass
    public static void setupTests() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        TilesetAssignmentProvider tilsetAssignmentProvider = new TilesetAssignmentProvider();
    }


    @Test
    public void testPropertyManagement() {
        // Test empty properties
        TilesetAssignment tAssign = new TilesetAssignment();
        Assert.assertEquals("{}", tAssign.getProperties());
        // Test assignment of new property
        tAssign.addProperty("foo", "bar");
        Assert.assertEquals("{\"foo\":\"bar\"}", tAssign.getProperties());
        // Test update of property
        tAssign.addProperty("foo", "baz");
        Assert.assertEquals("{\"foo\":\"baz\"}", tAssign.getProperties());
        // Test manually clearing properties
        tAssign.getPropertiesObject().entrySet().clear();
        Assert.assertEquals("{}", tAssign.getProperties());


    }

}

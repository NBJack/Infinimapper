package org.rpl.infinimapper.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rpl.infinimapper.DBSetupUtil;
import org.rpl.infinimapper.data.management.DaoDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 4/20/13 - 8:55 AM
 */
public class ObjectInstanceTests {

    private static final int TEST_IDENTITY = 236;
    private static final int TEST_OBJECT_DEFINITION = 0;
    public static final String FIELD_TEST_OBJECT = "TestObject";
    public static final String FIELD_TEST_VALUE = "HitPoints";
    private DaoDataProvider<Integer, ObjectInstance> objectInstanceProvider;


    @BeforeClass
    public static void setupTests() throws IOException, PropertyVetoException, SQLException {
        DBSetupUtil.setupDatabase();
    }

    @Before
    public void setup() throws SQLException {
        objectInstanceProvider = new DaoDataProvider<Integer, ObjectInstance>(ObjectInstance.class);
    }


    @Test
    public void testReads() throws SQLException {
        ObjectInstance inst = objectInstanceProvider.getValue(TEST_IDENTITY);
            Assert.assertNotNull(inst);
        Assert.assertEquals(TEST_IDENTITY, inst.getId());
        JsonArray obj = inst.getPropertyJson();
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.size() > 0);
    }

    @Test
    public void testWrites() throws SQLException {
        ObjectInstance inst = new ObjectInstance();
        Assert.assertFalse(inst.hasId());
        inst.setOffsetX(30);
        inst.setOffsetY(64);
        inst.setDefinition(TEST_OBJECT_DEFINITION);
        inst.addProperty(FIELD_TEST_OBJECT, true);
        inst.addProperty(FIELD_TEST_VALUE, 100);
        objectInstanceProvider.putValue(null, inst);
        Assert.assertTrue(inst.hasId());
        Assert.assertNotNull(inst.getID());
        // Verify it was put successfully
        ObjectInstance retrievedInstance = objectInstanceProvider.getValue(inst.getID());
        Assert.assertNotNull(retrievedInstance);
        Assert.assertEquals(inst.getID(), retrievedInstance.getID());
        Assert.assertEquals(inst.getOffsetX(), retrievedInstance.getOffsetX());
        Assert.assertEquals(inst.getOffsetY(), retrievedInstance.getOffsetY());
        JsonArray retrievedProperties = retrievedInstance.getPropertyJson();
        Assert.assertEquals(2, retrievedProperties.size());
        // Test the first object
        JsonObject retrievedTestObj = retrievedProperties.get(0).getAsJsonObject();
        Assert.assertEquals(FIELD_TEST_OBJECT, retrievedTestObj.get(ObjectInstance.PROPERTY_NAME_KEY));
        Assert.assertEquals(true, retrievedTestObj.get(ObjectInstance.PROPERTY_VALUE_KEY));
        // Clean up
        objectInstanceProvider.deleteValue(inst.getID());
        Assert.assertNull(objectInstanceProvider.getValue(inst.getID()));
    }

    @Test
    public void testCoordinateAssignment() {

    }
}

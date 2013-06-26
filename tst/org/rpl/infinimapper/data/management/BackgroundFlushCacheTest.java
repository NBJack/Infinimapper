package org.rpl.infinimapper.data.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rpl.infinimapper.data.Identable;

public class BackgroundFlushCacheTest {

	private static final String KEY_ALPHA = "One";
	private static final String KEY_BETA = "Two";
	private static final String KEY_MISSING = "Infinity";
	private static final String VALUE_ALPHA = "Foo";
	private static final String VALUE_ALPHA_DELTA = "manchu";
	private static final String VALUE_ALPHA_REVISED = VALUE_ALPHA + VALUE_ALPHA_DELTA;
	private static final String VALUE_BETA = "Bar";
	private TestObject OBJECT_ALPHA = new TestObject(VALUE_ALPHA);
	private TestObject OBJECT_BETA = new TestObject(VALUE_BETA);

	SimpleMemoryMapProvider<String, TestObject> stubProvider;
	BackgroundFlushCache<String, TestObject, String> cache;

	class TestObject implements Incrementable<TestObject, String>, Identable<String> {
		private String value;
        private String id;

        private static final String UNASSIGNED_ID = "";

		public TestObject(String value) {
			this.value = value;
            this.id = UNASSIGNED_ID;
		}

		@Override
		public TestObject applyDelta(String delta) {
			return new TestObject(this.value + delta);
		}


        @Override
        public String getID() {
            return id;
        }

        @Override
        public boolean hasId() {
            return id != UNASSIGNED_ID;
        }
    }

	@Before
	public void setup() {
		// Create a new cache with a stubbed provider but don't turn on the
		// flush thread.
		stubProvider = new SimpleMemoryMapProvider<String, TestObject>();
		cache = new BackgroundFlushCache<String, TestObject, String>(stubProvider, false);
		// Tell the stubbed provider about the initial objects
		stubProvider.putValue(KEY_ALPHA, OBJECT_ALPHA);
		stubProvider.putValue(KEY_BETA, OBJECT_BETA);
	}

	@Test
	public void testFlushCacheChanges() {
		// Populate the cache
		assertEquals(0, cache.getCachedCount());
		TestObject alpha = cache.getValue(KEY_ALPHA);
		assertEquals(OBJECT_ALPHA, alpha);
		TestObject beta = cache.getValue(KEY_BETA);
		assertEquals(OBJECT_BETA, beta);
		assertEquals(2, cache.getCachedCount());
		assertEquals(0, cache.getChangedCount());
		// Apply the delta
		TestObject alphaV2 = cache.updateValue(KEY_ALPHA, VALUE_ALPHA_DELTA);
		assertEquals(VALUE_ALPHA_REVISED, alphaV2.value);
		assertEquals(1, cache.getChangedCount());
		assertEquals(VALUE_ALPHA_REVISED, cache.getValue(KEY_ALPHA).value);
		assertEquals(OBJECT_ALPHA, stubProvider.getValue(KEY_ALPHA));
		// Flush
		cache.flushCacheChanges();
		// Check to make sure the provider recieved it
		assertEquals(0, cache.getChangedCount());
		assertEquals(alphaV2, stubProvider.getValue(KEY_ALPHA));
	}

	@Test
	public void testFlushCacheChangesEmpty() {
		assertEquals(0, cache.getChangedCount());
		cache.flushCacheChanges();
		assertEquals(0, cache.getChangedCount());
		// Add two elements from the provider indirectly, but don't change
		// anything
		cache.getValue(KEY_ALPHA);
		cache.getValue(KEY_BETA);
		assertEquals(2, cache.getCachedCount());
		assertEquals(0, cache.getChangedCount());
		cache.flushCacheChanges();
		assertEquals(2, cache.getCachedCount());
		assertEquals(0, cache.getChangedCount());
	}

	@Test
	public void testGetValueNotThere() {
		assertTrue(null == cache.getValue(KEY_MISSING));
	}

	@Test
	public void testGetValueAlreadyThere() {
		assertEquals(0, cache.getCachedCount());
		TestObject alpha = cache.getValue(KEY_ALPHA);
		// Remove items from the provider and test the cache is functioning
		stubProvider.clear();
		TestObject cachedAlpha = cache.getValue(KEY_ALPHA);
		assertEquals(alpha, cachedAlpha);
	}

	@Test
	public void testUpdateValueNotThere() {
		assertEquals(0, cache.getCachedCount());
		TestObject alphaV2 = cache.updateValue(KEY_ALPHA, VALUE_ALPHA_DELTA);
		assertEquals(VALUE_ALPHA_REVISED, alphaV2.value);
	}

	@Test
	public void testUpdateValueSeveralThreads() {

	}

}

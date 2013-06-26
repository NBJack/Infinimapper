package org.rpl.infinimapper.data.management;

import java.util.HashMap;

/**
 * A very basic map-backed provider of objects. Does not actually fetch
 * anything.
 * 
 * @author Ryan
 * 
 */
class SimpleMemoryMapProvider<Key, T> extends DataProvider<Key, T> {

	HashMap<Key, T> dataStore;

	public SimpleMemoryMapProvider() {
		this.dataStore = new HashMap<Key, T>();
	}

	@Override
	public T getValue(Key key) {
		return dataStore.get(key);
	}

	@Override
	public void putValue(Key key, T value) {
		dataStore.put(key, value);
	}

	public void clear() {
		dataStore.clear();
	}
}

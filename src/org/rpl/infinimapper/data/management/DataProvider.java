package org.rpl.infinimapper.data.management;

/**
 * A simple data provider interface.
 * 
 * @author Ryan
 * 
 * @param <Key>
 * @param <Value>
 */
public abstract class DataProvider<Key, Value> {

	/**
	 * Gets the value from the provider based on the key.
	 * 
	 * @param key They key to use.
	 * @return The value if found, null otherwise.
	 */
	public abstract Value getValue(Key key);

	/**
	 * Puts the value into the provider.
	 * 
	 * @param key The key to use.
	 * @param value The value to put.
	 */
	public abstract void putValue(Key key, Value value);

    /**
     * Adds a new value without a key to the data store.
     * @param value The value to put.
     */
    public void addValue(Value value) {
        putValue(null, value);
    }
}

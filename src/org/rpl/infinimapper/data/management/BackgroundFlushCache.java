package org.rpl.infinimapper.data.management;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.Validate;
import org.rpl.infinimapper.data.Identable;

/**
 * Keeps objects in a thread-safe cache while flushing changed objects to a more
 * persistent store in a background thread.
 * 
 * @author Ryan
 * 
 */
public class BackgroundFlushCache<Key, T extends Incrementable<T, Delta> & Identable<Key>, Delta> {

	private static final String BACKGROUND_THREAD_NAME = "BackgroundCacheFlush";
	private static final int DEFAULT_CACHE_INTERVAL = 10000;
	private static final int MAX_CACHE_AGE_MS = 120000;
	private static final int MAX_CACHE_SIZE = 1000;
	private ConcurrentHashMap<Key, CacheUpdateWrapper> cache;
	private DataProvider<Key, T> provider;
	private Thread backgroundUpdateThread;
	private CacheFlushThread flushProcess;

	public BackgroundFlushCache(DataProvider<Key, T> provider) {
		this(provider, true);
	}

	/**
	 * Determines behavior in the event nothing exists at the specified key
	 * during a Get. By default, does nothing and returns null. Alternatively,
	 * it can create a placeholder and store in the cache.
	 * 
	 * @param key The key to use. Should not be null.
	 * @return An 'empty' T instance, or null if not appropriate.
	 */
	protected T handleEmptyGet(Key key) {
		return null;
	}

	/**
	 * Determines behavior in the event nothing exists at the specified key
	 * during a Get. By default, does nothing and returns null. Alternatively,
	 * it can create a empty value for T.
	 * 
	 * @param key The key to use. Should not be null.
	 * @return An 'empty' T instance, or null if not appropriate.
	 */
	protected T handleEmptyWrite(Key key) {
		return null;
	}

	/**
	 * Creates a new cache with background-flush enabled immediate it desired.
	 * Pulls and pushes data to the given provider.
	 * 
	 * @param provider
	 * @param startBackgroundFlush
	 */
	public BackgroundFlushCache(DataProvider<Key, T> provider, boolean startBackgroundFlush) {
		Validate.notNull(provider);

		this.cache = new ConcurrentHashMap<Key, CacheUpdateWrapper>();
		this.provider = provider;
		this.flushProcess = new CacheFlushThread(DEFAULT_CACHE_INTERVAL);
		this.backgroundUpdateThread = new Thread(null, flushProcess, BACKGROUND_THREAD_NAME);

		if (startBackgroundFlush) {
			this.backgroundUpdateThread.start();
		}
	}

	/**
	 * Retrieves a value from the cache. If it does not already exist, pull it
	 * from the provider.
	 * 
	 * @param key
	 * @return
	 */
	public T getValue(Key key) {
		CacheUpdateWrapper wrapper = cache.get(key);

		if (wrapper == null) {
			// It doesn't exist in the cache yet; retrieve it and store it.
			T value = provider.getValue(key);
			if (value == null) {
				// Nothing exists. Generate an empty. It's OK if it returns
				// null.
				value = handleEmptyGet(key);
			}

			wrapper = new CacheUpdateWrapper(value);
			CacheUpdateWrapper existing = cache.putIfAbsent(key, wrapper);
			// Make sure we don't put two concurrently.
			if (existing != null) {
				wrapper = existing;
			}
		}

		return wrapper.getValue();
	}

	/**
	 * Update the specified value. If it wasn't already cached, retrieve it and
	 * do so. TODO: Require the cache to be a certain size and, if that size is
	 * reached, skip trying to store values and write entries directly.
	 * 
	 * @param key The key of the value to lookup.
	 * @param delta The delta to apply to the value.
	 * @return The new value resulting from the delta.
	 */
	public T updateValue(Key key, Delta delta) {

		CacheUpdateWrapper wrapper = cache.get(key);
		if (wrapper == null) {
			// We need to add it.
			getValue(key);
			wrapper = cache.get(key);
		}

		// Lock the value via the wrapper
		synchronized (wrapper) {

			// Apply the delta and update the wrapper.
			T oldValue = wrapper.getValue();
			if (oldValue == null) {
				// We need something to write to.
				oldValue = handleEmptyWrite(key);
			}
			T newValue = oldValue.applyDelta(delta);
			wrapper.value = newValue;
			wrapper.markChanged();

			// Put it back in the map in case it was removed (such as by the
			// cleaning mechanism)
			CacheUpdateWrapper prior = cache.putIfAbsent(key, wrapper);
			if (prior != null && prior != wrapper) {
				// The wrapper was replaced by a newer instance; this is a
				// grievous problem. Hopefully, it will also be rare.
				System.err.println("A grievous error has occurred; the wrapper,"
						+ " after a write, was replaced. Data has been lost.");
				System.err.println("Data: " + wrapper.getValue().toString());
				// TODO: Treat this as a version control system and try to apply
				// the delta to the new version. This isn't immune to trouble,
				// but it's better than nothing; it can theoretically suffer
				// from the same problem.
			}
		}

		return wrapper.getValue();
	}

	/**
	 * Flush out everything changed in the cache. Designed to be as
	 * thread-friendly as possible, but should be left to the managing thread.
	 */
	public void flushCacheChanges() {

		int flushCount = 0;
		for (Entry<Key, CacheUpdateWrapper> wrapperEntry : cache.entrySet()) {
			CacheUpdateWrapper wrapper = wrapperEntry.getValue();
			if (wrapper.hasChanged()) {
				// While it's possible the wrapper will be given new data while
				// we do this, we know that when updates occur the flag is set
				// after. Thus, by clearing it before, we know that new data
				// will always be written (even if it's written twice). We don't
				// want to block here as an update may take some time.
				wrapper.markUpdated();
				wrapper.getValue();
				provider.putValue(wrapperEntry.getKey(), wrapper.getValue());
				flushCount++;
			}
		}
		if (flushCount > 0) {
			//System.out.println("Number of entries flushed:" + flushCount + " (out of " + cache.size() + ")");
		}
	}

	/**
	 * Attempts to clean up the cache by removing entries which haven't been
	 * updated in a while. Everything is done as safely as possible, with
	 * locking on the same entity as the write method. Only entries that are
	 * older than MAX_CACHE_AGE_MS are removed.
	 */
	public void cleanCache() {
		int cleanedCount = 0;
		long currentTime = System.currentTimeMillis();
		for (Entry<Key, CacheUpdateWrapper> wrapperEntry : cache.entrySet()) {
			CacheUpdateWrapper value = wrapperEntry.getValue();

			if ((currentTime - value.lastUsed) > MAX_CACHE_AGE_MS) {
				synchronized (value) {
					// If we don't have it flagged as changed, let's go ahead
					// and remove it from the map. The change/delta function
					// will add it back if necessary.
					// TODO: Analyze what happens when everyone wants to change
					// things at once after removal or during it.
					if (!value.hasChanged()) {
						cache.remove(wrapperEntry.getKey());
						cleanedCount++;
					}
				}
			}

		}
		//System.out.println("Entries removed from cache: " + cleanedCount);
	}

	/**
	 * Shut down the cache's background processes in preparation for deleting
	 * the object.
	 */
	public void shutdown() {
		this.flushProcess.shutdown();
	}

	/**
	 * Wraps a stored class with metadata about updates. Also, keeps track of
	 * the last time an update occurred (to allow removal).
	 * 
	 * @author Ryan
	 * 
	 * @param <T>
	 */
	protected class CacheUpdateWrapper {
		private volatile boolean changed;
		private volatile T value;
		private volatile long lastUsed;

		CacheUpdateWrapper(T value) {
			this.value = value;
			this.lastUsed = System.currentTimeMillis();
		}

		/**
		 * Something changed.
		 * 
		 * @return
		 */
		boolean hasChanged() {
			return this.changed;
		}

		/**
		 * Sets a new value for the wrapper to wrap, and sets the flag for it as
		 * 'changed'.
		 * 
		 * @param newValue
		 */
		void setValue(T newValue) {
			this.value = value;
			markChanged();
		}

		/**
		 * Indicates that an update *should* happen. Needs to be done after the
		 * value is altered to avoid happens-before problems. Also, this updates
		 * the timestamp.
		 */
		void markChanged() {
			this.changed = true;
			this.lastUsed = System.currentTimeMillis();
		}

		/**
		 * Indicates that an update *will* happen. This must be done before the
		 * udpate actually takes place.
		 */
		void markUpdated() {
			this.changed = false;
		}

		/**
		 * The stored value.
		 * 
		 * @return
		 */
		T getValue() {
			this.lastUsed = System.currentTimeMillis();
			return value;
		}
	}

	private class CacheFlushThread implements Runnable {

		private int flushInterval;
		private boolean continueFlag;

		CacheFlushThread(int interval) {
			this.continueFlag = true;
			this.flushInterval = interval;
		}

		/**
		 * Stop the thread flush on the next run.
		 */
		void shutdown() {
			this.continueFlag = false;
		}

		@Override
		public void run() {

			while (continueFlag) {
				try {
					Thread.sleep(flushInterval);
					flushCacheChanges();
					cleanCache();
				} catch (InterruptedException exception) {
					// Ignore
				} catch (Exception exception) {
					// TODO: Logging
					System.err.println(exception.toString());
				}
			}
		}

	}

	/**
	 * Calculates the number of items in the cache that have changed but have
	 * not been flushed.
	 * 
	 * @return The number of items in the cache that haven't been flushed.
	 */
	public int getChangedCount() {
		int count = 0;
		for (CacheUpdateWrapper wrapper : cache.values()) {
			if (wrapper.hasChanged()) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Returns the number of objects cached total.
	 * 
	 * @return
	 */
	public int getCachedCount() {
		return cache.size();
	}

    /**
     * Adds a new entry to the cache that does not already
     * have an identity/key associated with it. This makes
     * the most sense when the data being stored isn't
     * something that has a calcuable key (ex. Realms have
     * no key, but Chunks do). When finished, it adds it to
     * the cache.
     * @param value The value to add. Cannot be null and
     *              must not have an ID.
     */
    public void addEntry ( T value ) {
        // Add it to the provider
        provider.putValue(null, value);
        // Does it already exist in the cache?
        CacheUpdateWrapper wrapper = cache.get(value.getID());
        if ( wrapper != null ) {
            synchronized (wrapper) {
                if (wrapper.value == null) {
                    cache.remove(value.getID());
                }
            }
        }
        // Attempt to retrieve the value, which will add it automatically to the cache
        getValue(value.getID());
    }
}

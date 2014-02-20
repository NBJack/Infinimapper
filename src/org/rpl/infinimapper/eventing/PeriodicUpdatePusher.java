package org.rpl.infinimapper.eventing;

import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Periodically pushes out updates to all the {@link UpdateListener}s
 * registered to it.  The interval needs to be specified.
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 2/19/14
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeriodicUpdatePusher<Key, T> extends Thread {

    /**
     * The default number of milliseconds to wait between updates
     */
    public static final long MS_BETWEEN_UPDATES = 200;

    private final UpdateCollector<T> updateCollector;
    private final boolean keepWorking = true;
    private final long updateInterval;
    private final ConcurrentHashMap<Key, UpdateListener<Key, List<T>>> listeners;

    /**
     * Create a new threaded observer of the provided pusher. The thread will start once
     * intialization is complete.
     * @param updateCollector the update pusher to monitor. Cannot be null.
     * @param updateInterval the interval to wait between. Must be >= 0.
     */
    public PeriodicUpdatePusher(UpdateCollector<T> updateCollector, final long updateInterval) {
        Validate.notNull(updateCollector);
        Validate.isTrue(updateInterval >= 0, "The duration between updates must be >= 0");
        this.updateInterval = updateInterval;
        this.listeners = new ConcurrentHashMap<>();
        this.updateCollector = updateCollector;

        // Go ahead and start this thread.
        this.start();
    }

    /**
     * Create a new threaded observer that uses the default update interval.
     * @param updateCollector
     */
    public PeriodicUpdatePusher(UpdateCollector<T> updateCollector) {
        this(updateCollector, MS_BETWEEN_UPDATES);
    }

    /**
     * Sends out the updates found in the attached {@link UpdateCollector}. Normally
     * invoked by the internal thread system.
     */
    public void sendOutUpdates() {
        // Get the latest changes
        List<T> changes = updateCollector.grabChanges();
        if ( changes.size() > 0 ) {
            // Notify each listener
            for ( UpdateListener<Key, List<T>> listener : listeners.values() ) {
                listener.updateArrived(changes);
            }
        }
    }

    /**
     * Add a new listener. Since we're possible inserting a new listener in place
     * of an old one if the keys match, return what was replaced.
     * @param listener the listener to add. Cannot be null.
     */
    public UpdateListener<Key, List<T>> addListener(UpdateListener<Key, List<T>> listener) {
        Validate.notNull(listener);
        return listeners.put(listener.getID(), listener);
    }


    public UpdateListener<Key, List<T>> removeListener(Key listenerKey) {
        Validate.notNull(listenerKey);
        return listeners.remove(listenerKey);
    }


    @Override
    public void run() {

        while (keepWorking) {
            try {
                TimeUnit.MILLISECONDS.sleep(updateInterval);
            } catch (InterruptedException iEx) {
                // No-op
            }

            // Push the updates
            sendOutUpdates();
        }

    }
}

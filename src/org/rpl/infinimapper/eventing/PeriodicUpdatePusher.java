package org.rpl.infinimapper.eventing;

import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Periodically pushes out updates to all the {@link UpdateListener}s
 * registered to it.  The interval needs to be specified. since updates
 * may or may not be the same type as the originals, make room for
 * transformations.
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 2/19/14
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PeriodicUpdatePusher<Key, T, Out> extends Thread {

    /**
     * The default number of milliseconds to wait between updates
     */
    public static final long MS_BETWEEN_UPDATES = 200;

    private final UpdateCollector<T> updateCollector;
    private final boolean keepWorking = true;
    private final long updateInterval;
    private final ConcurrentHashMap<Key, UpdateListener<Key, List<Out>>> listeners;

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
     * Generate output based on the available updates.
     * @param in
     * @return
     */
    protected abstract List<Out> generateOutput (List<T> in);

    /**
     * Sends out the updates found in the attached {@link UpdateCollector}. Normally
     * invoked by the internal thread system.
     */
    public void sendOutUpdates() {
        // Get the latest changes
        List<Out> changes = generateOutput(updateCollector.grabChanges());
        if ( changes.size() > 0 ) {
            // Notify each listener
            for ( UpdateListener<Key, List<Out>> listener : listeners.values() ) {
                listener.updateArrived(changes);
            }
            System.out.println("Pushed " + changes.size() + " updates.");
        }
    }

    /**
     * Add a new listener. Since we're possible inserting a new listener in place
     * of an old one if the keys match, return what was replaced.
     * @param listener the listener to add. Cannot be null.
     */
    public UpdateListener<Key, List<Out>> addListener(UpdateListener<Key, List<Out>> listener) {
        Validate.notNull(listener);
        return listeners.put(listener.getID(), listener);
    }


    public UpdateListener<Key, List<Out>> removeListener(Key listenerKey) {
        Validate.notNull(listenerKey);
        return listeners.remove(listenerKey);
    }


    /**
     * Our main event pushing loop. At the specified interval, updates
     * collected will be pushed out to all listeners.
     */
    @Override
    public void run() {

        while (keepWorking) {
            try {
                TimeUnit.MILLISECONDS.sleep(updateInterval);
            } catch (InterruptedException iEx) {
                // No-op
            }

            // Push the updates
            try {
                sendOutUpdates();
            } finally {
                // Notify parent of problems
                //System.err.println("There was a serious problem in the update event loop.");
            }
        }

    }
}

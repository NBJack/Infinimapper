package org.rpl.infinimapper.eventing;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores updates until flushed.
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 2/19/14
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateCollector<T> {

    private ConcurrentHashMap<T, Boolean> updates;

    /**
     * Create a new update pusher.
     */
    public UpdateCollector() {
        updates = new ConcurrentHashMap<>();
    }

    /**
     * Put a new update into the update list. Note that since this is backed by a hash,
     * duplicates will be naturally eliminated.
     * @param data
     */
    public void pushUpdate (T data) {
        Validate.notNull(data);
        updates.put(data, true);
    }

    /**
     * Get a complete list of changes. While thread safe, behavior when called by multiple
     * threads may not be entirely consistent in the results. The changes will be removed
     * the list.
     * @return  A list of the changes.
     */
    public List<T> grabChanges() {
        List<T> changes = new ArrayList<T>(updates.keySet());

        // Remove those changes
        for ( T change : changes ) {
            updates.remove(change);
        }

        return changes;
    }

    /**
     * Determines if no updates are available.
     * @return true if the underlying map is empty, false otherwise.
     */
    public boolean isEmpty() {
        return updates.isEmpty();
    }

}

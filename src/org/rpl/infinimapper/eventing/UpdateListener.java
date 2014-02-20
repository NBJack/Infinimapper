package org.rpl.infinimapper.eventing;

/**
 * Observes a particular kind of data. Listeners must provide a uniquely
 * identifiable key in order to be properly tracked
 *
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 2/18/14
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UpdateListener<Key, T> {

    /**
     * Called to push data when available.
     * @param data the data that 'arrived'.
     */
    void updateArrived(T data);

    /**
     * Returns the unique identifier for this listener. In the event that the identifiers
     * are not unique, the behavior of those that depend on this is not defined.
     * @return the unique identifier.
     */
    Key getID();
}

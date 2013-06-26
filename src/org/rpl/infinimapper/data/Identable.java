package org.rpl.infinimapper.data;

/**
 * User: Ryan
 * Date: 2/14/13 - 12:06 PM
 */
public interface Identable<V> {

    /**
     * Get the assigned ID.
     * @return The ID. May or may not be a 'valid' generated ID.
     */
    V getID();

    /**
     * Returns true if this has actually been assigned an ID.  Note that simply invoking {@link #getID()}
     * isn't sufficient as there may be 'special' ID codes assigned.
     * @return True if a valid ID has been assigned, false otherwise.
     */
    boolean hasId();

}

package org.rpl.infinimapper.data.management;

import org.rpl.infinimapper.data.Realm;

/**
 * Caches the Realm information.
 * User: Ryan
 * Date: 1/13/13 - 8:40 AM
 */
public class RealmCache extends BackgroundFlushCache<Integer, Realm, Realm> {

    public RealmCache(DataProvider<Integer, Realm> provider, boolean startBackgroundFlush) {
        super(provider, startBackgroundFlush);
    }



}

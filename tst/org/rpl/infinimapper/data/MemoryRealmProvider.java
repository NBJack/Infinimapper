package org.rpl.infinimapper.data;

import org.rpl.infinimapper.data.management.DataProvider;

import java.util.HashMap;

/**
 * In-memory provision of realm data.
 * User: Ryan
 * Date: 1/25/13 - 9:48 PM
 */
public class MemoryRealmProvider extends DataProvider<Integer, Realm> {

    private HashMap<Integer, Realm> realmMap;

    public MemoryRealmProvider() {
        realmMap = new HashMap<Integer, Realm>();
    }

    @Override
    public Realm getValue(Integer id) {
        return realmMap.get(id);
    }

    @Override
    public void putValue(Integer id, Realm realm) {

        realmMap.put(id, realm);
    }

    public HashMap<Integer, Realm> getMap() {
        return realmMap;
    }
}

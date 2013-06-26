package org.rpl.infinimapper.data.management.meta;

import org.rpl.infinimapper.data.ObjectInstance;
import org.rpl.infinimapper.data.TilesetAssignment;
import org.rpl.infinimapper.data.management.*;

import java.sql.SQLException;

/**
 * Provides a single source of all data providers for map data.
 * User: Ryan
 * Date: 5/25/13 - 2:32 PM
 */
public class MapDataProviders {

    private ChunkCache chunkCache;
    private LayerDataProvider layerProvider;
    private ObjectInstanceProvider instanceProvicder;
    private TilesetAssignmentProvider assignmentProvider;
    private RealmCache realmCache;
    private TilesetProvider tilesetProvider;



    public static MapDataProviders generateProvider(boolean acitvateBackgroundThreads) throws SQLException {
        RealmCache realmCache = new RealmCache(new RealmDataProvider(), acitvateBackgroundThreads);
        return new MapDataProviders(
                new ChunkCache(new ChunkDataProvider(), acitvateBackgroundThreads, realmCache),
                new LayerDataProvider(),
                new ObjectInstanceProvider(),
                new TilesetAssignmentProvider(),
                new TilesetProvider(),
                realmCache
        );

    }

    public MapDataProviders ( ChunkCache chunkCache, LayerDataProvider layerProvider,ObjectInstanceProvider instanceProvicder,TilesetAssignmentProvider assignmentProvider, TilesetProvider tilesetProvider, RealmCache realmCache ) {
        this.chunkCache = chunkCache;
        this.layerProvider = layerProvider;
        this.instanceProvicder = instanceProvicder;
        this.assignmentProvider = assignmentProvider;
        this.realmCache = realmCache;
        this.tilesetProvider = tilesetProvider;
    }

    public RealmCache realms() {
        return realmCache;
    }

    public ChunkCache chunks() {
        return chunkCache;
    }

    public LayerDataProvider layers() {
        return layerProvider;
    }

    public ObjectInstanceProvider instances() {
        return instanceProvicder;
    }

    public TilesetAssignmentProvider tilesetAssignments() {
        return assignmentProvider;
    }

    public TilesetProvider tilesets() {
        return tilesetProvider;
    }

    /**
     * Flush every provider with a cache.
     */
    public void flushAll() {
        chunkCache.flushCacheChanges();
        realmCache.flushCacheChanges();
    }
}

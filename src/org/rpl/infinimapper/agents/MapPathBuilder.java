package org.rpl.infinimapper.agents;

import org.rpl.infinimapper.data.Realm;
import org.rpl.infinimapper.data.inbound.MapDeltaCanvas;
import org.rpl.infinimapper.data.management.ChunkCache;

/**
 * A simple drawing agent that 'wanders' around the map drawing a path.
 * User: Ryan
 * Date: 1/26/13 - 7:47 AM
 */
public class MapPathBuilder extends Thread {

    private MapDeltaCanvas deltaCanvas;
    private int tile;
    private boolean stopMe;
    private int currentDirection;
    private int x;
    private int y;


    public MapPathBuilder( ChunkCache chunkCache, Realm realm, int tileToUse ) {
        this.deltaCanvas = new MapDeltaCanvas(realm, chunkCache);
        this.tile = tileToUse;
        this.stopMe = false;
        this.x = 0;
        this.y = 0;
        this.currentDirection = 1;

        // Fire-off the thread
        this.start();

    }


    @Override
    public void run () {

        while ( !stopMe ) {

            // Figure out if we want to change direction.
            if ( Math.random() > 0.9 ) {
                // Turn left or right?
                if ( Math.random() > 0.5 ) {
                    currentDirection = (currentDirection + 1) % Direction.values().length;
                } else {
                    currentDirection = (Direction.values().length + currentDirection - 1) % Direction.values().length;
                }
            }

            // Move to the next tile
            this.x += Direction.values()[currentDirection].x;
            this.y += Direction.values()[currentDirection].y;
            // Write a tile
            deltaCanvas.writeTile(x, y, tile);
            deltaCanvas.flush();

            try {
                Thread.sleep(1);
            } catch ( InterruptedException ex ) {
                // Don't care
            }
        }
    }


    public enum Direction {

        North(0, -1),
        East(1, 0),
        South(0, 1),
        West(-1, 0)
        ;

        int x;
        int y;

        Direction ( int x, int y ) {
            this.x = x;
            this.y = y;

        }
    }
}

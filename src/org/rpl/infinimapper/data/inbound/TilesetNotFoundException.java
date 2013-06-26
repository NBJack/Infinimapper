package org.rpl.infinimapper.data.inbound;

/**
 * User: Ryan
 * Date: 5/25/13 - 10:52 PM
 */
public class TilesetNotFoundException extends MapProcessingException {

    private String name;

    public TilesetNotFoundException(String name) {
        super("Could not find tileset '" + name  + "'");
    }

}

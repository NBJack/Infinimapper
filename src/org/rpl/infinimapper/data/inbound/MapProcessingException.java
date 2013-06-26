package org.rpl.infinimapper.data.inbound;

/**
 * User: Ryan
 * Date: 5/26/13 - 10:12 PM
 */
public class MapProcessingException extends Throwable {

    public MapProcessingException(String message) {
        super(message);
    }

    public MapProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}

package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Handles all messaging from WebSockets.
 * User: Ryan
 * Date: 6/26/13 - 3:11 PM
 */
public class EventMessagingInbound extends MessageInbound {

    private WsOutbound outbound;

    public EventMessagingInbound() {
        super();
    }

    @Override
    protected void onOpen(WsOutbound outbound) {
        System.out.println("Client status: OPEN");
        this.outbound = outbound;
        try {
            outbound.writeTextMessage(CharBuffer.wrap("Calling Major Tom, Calling Major Tom"));
            // TODO: Record this client properly so we can let them know when new data is available.
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        // Ignore
    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {
        // Check to see what kind of data is inbound
        System.out.println("New Message: " + charBuffer);
        // Route it

    }
}

package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rpl.infinimapper.WorldDB;
import org.rpl.infinimapper.data.Chunk;
import org.rpl.infinimapper.data.ChunkKey;
import org.rpl.infinimapper.eventing.ChunkUpdateCollector;
import org.rpl.infinimapper.eventing.UpdateCollector;
import org.rpl.infinimapper.eventing.UpdateListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;

/**
 * Handles all messaging from WebSockets.
 * User: Ryan
 * Date: 6/26/13 - 3:11 PM
 */
public class EventMessagingInbound extends MessageInbound implements UpdateListener<String, List<Chunk>> {

    private WsOutbound outbound;
    private String identifier;

    private boolean socketClosed = false;

    private ChunkUpdateCollector updateCollector;


    public EventMessagingInbound(String identifier, ChunkUpdateCollector updateCollector) {
        super();
        this.identifier = identifier;
        this.updateCollector = updateCollector;
    }

    @Override
    protected void onOpen(WsOutbound outbound) {
        System.out.println("Client status: OPEN");
        this.outbound = outbound;
    }

    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        // Ignore
    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {
        // Check to see what kind of data is inbound
        System.out.println("New Message: " + charBuffer);
        // Route it to the collector; convert it to a key.
        updateCollector.pushUpdate(ChunkKey.fromID(charBuffer.toString()));
    }

    @Override
    public void updateArrived(List<Chunk> data) {
        // If we've been closed, don't bother here.
        if (socketClosed == true) {
            return;
        }
        // TODO: Figure out an appropriate protocol to send this stuff.
        try {
            // Send each converted chunk.
            for (Chunk chunk : data) {
                outbound.writeTextMessage(CharBuffer.wrap(WorldDB.generateChunkResponse(chunk.getId(), chunk)));
            }
        } catch (IOException ioex) {
            System.err.println("Problem sending update to socket at " + identifier);
            ioex.printStackTrace();
        }
    }

    @Override
    public String getID() {
        return identifier;
    }

    @Override
    protected void onClose(int status) {
        super.onClose(status);    //To change body of overridden methods use File | Settings | File Templates.
        // We need to let our container know we're unavailable.
        socketClosed = true;
    }

    /**
     * Checks if this socket was closed.
     * @return true if the socket was closed, false oterwise.
     */
    public boolean isClosed() {
        return socketClosed;
    }


}

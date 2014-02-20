package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
public class EventMessagingInbound extends MessageInbound implements UpdateListener<String, List<String>> {

    private WsOutbound outbound;
    private String identifier;

    private UpdateCollector<String> updateCollector;


    public EventMessagingInbound(String identifier, UpdateCollector<String> updateCollector) {
        super();
        this.identifier = identifier;
        this.updateCollector = updateCollector;
    }

    @Override
    protected void onOpen(WsOutbound outbound) {
        System.out.println("Client status: OPEN");
        this.outbound = outbound;
        // Let every know we've joined. This is kinda dumb, but it'll be a good test.
        // TODO: Consider whether sending anything is a good idea.
        updateCollector.pushUpdate("I have joined: " + identifier);

    }

    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        // Ignore
    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {
        // Check to see what kind of data is inbound
        System.out.println("New Message: " + charBuffer);
        // Route it to the collector
        updateCollector.pushUpdate(charBuffer.toString());
    }

    @Override
    public void updateArrived(List<String> data) {
        // TODO: Figure out an appropriate protocol to send this stuff.
        try {
            outbound.writeTextMessage(CharBuffer.wrap(StringUtils.join(data)));
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
    }
}

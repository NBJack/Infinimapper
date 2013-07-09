package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.MessageInbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * User: Ryan
 * Date: 6/26/13 - 3:11 PM
 */
public class EventMessagingInbound extends MessageInbound {



    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        // Ignore
    }

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {

    }
}

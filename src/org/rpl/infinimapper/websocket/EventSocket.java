package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a fast WebSocket-based interface to data available from the server. Meant to be used for event notification
 * and bypassing the need to poll for updates.
 * User: Ryan
 * Date: 6/25/13 - 4:02 PM
 */
public class EventSocket extends WebSocketServlet {


    @Override
    protected StreamInbound createWebSocketInbound(String s, HttpServletRequest httpServletRequest) {
        return null;
    }
}

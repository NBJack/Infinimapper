package org.rpl.infinimapper.websocket;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.rpl.infinimapper.eventing.PeriodicUpdatePusher;
import org.rpl.infinimapper.eventing.UpdateCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides a fast WebSocket-based interface to data available from the server. Meant to be used for event notification
 * and bypassing the need to poll for updates.
 * User: Ryan
 * Date: 6/25/13 - 4:02 PM
 */
public class EventSocket extends WebSocketServlet {

    @Autowired
    private PeriodicUpdatePusher<String, String> updatePusher;
    @Autowired
    private UpdateCollector<String> updateCollector;

    /**
     * Manual Spring setup.
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }


    @Override
    protected StreamInbound createWebSocketInbound(String s, HttpServletRequest httpServletRequest) {
        System.out.println("ATTEMPED CONNECTION: " );
        EventMessagingInbound msgInboundTracker = new EventMessagingInbound(httpServletRequest.getSession().getId(), updateCollector);
        updatePusher.addListener(msgInboundTracker);
        return msgInboundTracker;
    }


}

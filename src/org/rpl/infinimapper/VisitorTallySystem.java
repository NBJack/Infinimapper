package org.rpl.infinimapper;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Application Lifecycle Listener implementation class VisitorTallySystem
 *
 */
public class VisitorTallySystem implements HttpSessionListener {

    /**
     * Default constructor. 
     */
    public VisitorTallySystem() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent arg0) {
        arg0.getSession().getServletContext().log("SESSION: New visitor arrived @ " + arg0.getSession().getCreationTime() + " (" + arg0.getSession().getId() + ")");

    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent arg0) {
        // TODO Auto-generated method stub
    }
	
}

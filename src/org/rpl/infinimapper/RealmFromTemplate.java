package org.rpl.infinimapper;

import org.rpl.infinimapper.data.inbound.MapProcessingException;
import org.rpl.infinimapper.data.inbound.TMXMapImporter;
import org.rpl.infinimapper.data.management.meta.MapDataProviders;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: Ryan
 * Date: 6/13/13 - 2:24 PM
 */
public class RealmFromTemplate extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // Right now, there's only one template
        try {
            MapDataProviders providers = MapDataProviders.generateProvider(false);

            TMXMapImporter importer = new TMXMapImporter(this.getServletContext()
                    .getRealPath("WEB-INF/templates/melonJs-alex/tinyTest.tmx"), new Point(0,0), providers);
            String filename = request.getParameter("name");
            if ( filename == null ) {
                filename = "melonJs Template - " + new Date();
            }

            importer.setName(filename);

            importer.processMap(true, -1);
            providers.flushAll();

            // Redirect to the constructed realm
            int gotoRealm = importer.getRealms().get(0).getId();
            response.sendRedirect("TiledCanvas.jsp?realm=" + gotoRealm);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MapProcessingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}

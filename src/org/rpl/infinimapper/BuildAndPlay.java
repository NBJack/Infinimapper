package org.rpl.infinimapper;

import org.rpl.infinimapper.data.export.MelonJsExporter;
import org.rpl.util.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Ryan
 * Date: 7/9/13 - 3:54 PM
 */
@WebServlet(name = "BuildAndPlay")
public class BuildAndPlay extends HttpServlet {

    /**
     * Determines where the results should be stored for publishing
     */
    private File outputDir = new File("C:\\Program Files (x86)\\Apache Group\\Apache2\\htdocs\\maps");

    /**
     * Determines where we can find the results online.
     */
    private String rootUrl = "http://localhost/maps/";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if ( request.getParameter("realm") != null ) {
            int realmId = Integer.parseInt(request.getParameter("realm"));

            // Setup the exporter
            File destination = FileUtils.generateTempDir(outputDir);
            destination.mkdir();

            MelonJsExporter exporter = new MelonJsExporter(destination);
            File resourcesSource = new File("C:\\Java\\workspace\\ChunkMan\\TestData\\melonResources.json");
            File templateSource = new File("C:\\Java\\workspace\\ChunkMan\\TestData\\exampleTemplate");
            try {
                System.out.println("Going to try for " + destination.toString());
                exporter.addResourcesFromFile(resourcesSource);
                exporter.pullInTemplate(templateSource);
                exporter.addMap(realmId);
                exporter.writeResources();

            } catch ( SQLException sqlex ) {
                throw new ServletException("Error while trying to export", sqlex);
            }

            response.getWriter().println("<html><body>");
            response.getWriter().println("Success! Put it in " + destination.toString() + " <a href='" + rootUrl + destination.getName() + "'>Click here to play!</a>" );
            response.getWriter().println("</body></html>");

        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

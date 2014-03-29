package org.rpl.infinimapper;

import org.rpl.infinimapper.data.export.MapExport;
import org.rpl.infinimapper.data.export.MelonJsExporter;
import org.rpl.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
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
    private File outputDir = new File("C:\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\\htdocs\\maps");

    /**
     * Determines where we can find the results online.
     */
    @Autowired
    @Qualifier(value = "BuiltGameHostPath")
    private String rootUrl;

    @Autowired
    private MapExport mapExporter;

    /**
     * Setup the Spring configuration system for this servlet.
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if ( request.getParameter("realm") != null ) {
            int realmId = Integer.parseInt(request.getParameter("realm"));

            // Setup the exporter
            File destination = FileUtils.generateTempDir(outputDir);
            destination.mkdir();
            System.out.println("Going to try for " + destination.toString());

            MelonJsExporter exporter = new MelonJsExporter(destination, mapExporter);


            File resourcesSource = new File(this.getServletContext().getRealPath("WEB-INF/TestData/melonResources.json"));
            File templateSource = new File(this.getServletContext().getRealPath("WEB-INF/TestData/exampleTemplate"));
            try {
                exporter.addResourcesFromFile(resourcesSource);
                exporter.pullInTemplate(templateSource);
                exporter.addMap(realmId);
                exporter.writeResources();

            } catch ( SQLException sqlex ) {
                throw new ServletException("Error while trying to export", sqlex);
            }

            response.getWriter().println("<html><body><p>" + rootUrl + "</p>");
            response.getWriter().println("<p>Success! Put it in " + destination.toString() + " <a href='" + rootUrl + destination.getName() + "'>Click here to play!</a>" );
            response.getWriter().println("</p></body></html>");

        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

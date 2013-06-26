package org.rpl.infinimapper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.data.export.TilesetExport;

/**
 * Servlet implementation class FetchTiles
 */
public class FetchTiles extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FetchTiles() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int tilesetID;
		boolean asDownload;
		try {
			// What tile set was requested?
			tilesetID = Integer.parseInt(request.getParameter("id"));

			// Should we do a download instead of a stream?
			asDownload = (request.getParameter("download") != null);
			if (asDownload) {
				response.setHeader("Content-Disposition", "attachment; filename=\"image" + tilesetID + ".png\"");
			}

			// Setup the content type and send the image
			response.setContentType("image/png");
			TilesetExport.writeImagetoStream(tilesetID, response.getOutputStream());
			response.flushBuffer();

		} catch (Exception ex) {
			// Note the exception
			ex.printStackTrace();
			response.sendError(404, ex.toString());
		}
	}

}

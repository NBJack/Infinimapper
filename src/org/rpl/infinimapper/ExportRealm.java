package org.rpl.infinimapper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.data.export.MapExport;

/**
 * Servlet implementation class ExportRealm
 */
public class ExportRealm extends HttpServlet {

	private static final String[] neededParams = new String[] { "realmid" };

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExportRealm() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Parses the provided string as an integer if not null; otherwise, returns
	 * defaultValue.
	 * 
	 * @param value
	 * @param defaultValue
	 */
	private int parseIntWithDefault(String value, int defaultValue) {
		if (value == null)
			return defaultValue;

		return Integer.parseInt(value);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Treat as if it were a post

		doPost(req, resp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		int realmID; // Specifies which realm is being exported
		int limitLeft, limitTop, limitRight, limitBottom; // Allows exporting of
															// just sections of
															// a realm
		MapDataType targetFormat; // The target file format to use
		String fileName;

		//
		// Read parameter data
		//

		// Presence check for the bare minimum

		if (!DataTools.areParameterNamesPresent(neededParams, request)) {
			response.sendError(406, "Insufficient parameters.");
		}

		// Parse

		realmID = Integer.parseInt(request.getParameter("realmid"));

		String packaging = request.getParameter("package");

		if (request.getParameter("format") != null) {
			targetFormat = MapDataType.valueOf(request.getParameter("format"));
		} else {
			targetFormat = MapDataType.TMX_BASE64;
		}

		fileName = request.getParameter("filename");
		if (fileName == null) {
			// Use a default filename.
			fileName = "map" + realmID + ".tmx";
		}
		PackageType pType;
		if (packaging != null) {
			pType = PackageType.valueOf(packaging);
		} else {
			pType = PackageType.JUST_MAP;
		}

		// TODO: Actually use this...
		limitLeft = parseIntWithDefault(request.getParameter("left"), -1);
		limitRight = parseIntWithDefault(request.getParameter("right"), -1);
		limitTop = parseIntWithDefault(request.getParameter("top"), -1);
		limitBottom = parseIntWithDefault(request.getParameter("bottom"), -1);

		// Apply
		switch (pType) {
		case JUST_MAP:
			writeMapToResponse(realmID, response, fileName, targetFormat);
			break;
		case ZIP_EVERYTHING:
			// TODO: Setup the staging system to write the whole map.
			break;
		default:
			throw new UnsupportedOperationException("Sorry, '" + pType + "' isn't supported at this time.");
		}
	}

	void writeMapToResponse(int realmid, HttpServletResponse response, String fileName, MapDataType mapDataFormat)
			throws IOException {

		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		try {
			MapExport.processAndExportMapTMX(realmid, response.getOutputStream(), fileName, "image", mapDataFormat);
		} catch (IOException ioex) {
			// Something went wrong. :/ Notify them of the error.
			System.err.println("Problem exporting map " + fileName + " (" + realmid + ")" + ioex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		response.flushBuffer();
	}
}

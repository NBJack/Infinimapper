package org.rpl.infinimapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.WorldDB.QuickCon;

import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class ObjInstManager
 */
public class ObjInstManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ObjInstManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Use the same action as the post

		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String action;

		action = request.getParameter("action");

		if (action == null) {
			response.sendError(403, "This operation isn't permitted.");

		} else if (action.equals("retrieve")) {
			// Retrieve the object information

			doInstRetrieve(request, response);

		} else if (action.equals("update")) {
			// Update an existing object

			doInstUpdate(request, response);

		}
	}

	protected void doInstUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		QuickCon connection = null;

		try {
			String dataBody = request.getParameter("dataBody");

			// TODO: Validate the data body as a 'legit' set of properties.

			connection = new QuickCon(WorldDB.WDB_OBJ_UPDATEDATA_QUERY);
			connection.getStmt().setString(1, dataBody);
			connection.getStmt().setInt(2, Integer.parseInt(request.getParameter("dataID")));

			connection.getStmt().execute();

			response.getWriter().println("<html><body><textarea>OK</textarea></body></html>");

		} catch (SQLException sqlEx) {
			// For now, just print a stack trace.
			sqlEx.printStackTrace();
			response.getWriter().println("<html><body><textarea>ERROR</textarea></body></html>");

		} finally {
			// Release resources
			if (connection != null) {
				connection.release();
			}
		}
	}

	protected void doInstRetrieve(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		Connection c;
		PreparedStatement st;
		ResultSet set;
		JsonWriter jOut;
		int objid;

		c = null;
		st = null;
		set = null;

		// Retrieve the object ID

		if (request.getParameter("objid") == null) {
			response.sendError(406, "The object's ID must be specified.");

			return;
		}

		objid = Integer.parseInt(request.getParameter("objid"));

		try {

			// Prep and retrieve the object data

			c = DBResourceManager.getConnection();

			st = c.prepareStatement(WorldDB.WDB_OBJ_SINGLE_RETRIEVE_QUERY);
			st.setInt(1, objid);

			set = st.executeQuery();

			if (set.next()) {
				// Great, we had a hit. Prep and write a JSON response

				response.setContentType("text/html");
				response.getWriter().print("<html><body><textarea>");

				jOut = new JsonWriter(response.getWriter());

				jOut.beginObject();

				jOut.name("dataID").value(objid);
				jOut.name("dataType").value(10);
				jOut.name("dataBody").value(set.getString(7));
				jOut.name("dataXCoord").value(set.getInt(2));
				jOut.name("dataYCoord").value(set.getInt(3));
				jOut.name("dataWidth").value(set.getInt(8));
				jOut.name("dataHeight").value(set.getInt(9));

				jOut.endObject();

				set.close();

				response.getWriter().println("</textarea></body></html>");

			} else {

				// No good; nothing

				response.sendError(404, "Object was not found");
			}

		} catch (SQLException sqex) {

		} finally {

			// Safely clean-up the items

			DataTools.safeCleanUp(c, st, set);
		}
	}

}

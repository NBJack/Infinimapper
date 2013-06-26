package org.rpl.infinimapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.WorldDB.QuickCon;
import org.rpl.infinimapper.security.AuthMan;
import org.rpl.infinimapper.security.AuthMan.AuthEntities;
import org.rpl.infinimapper.security.UserPermissionCache.Permission;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class RealmManager
 */
public class RealmManager extends HttpServlet {
	
		
	
	private static final long serialVersionUID = 1L;
	
	
	static final String RM_CREATE_REALM = "INSERT INTO realms(name, description, tileset, defaulttile, ownerid, public) VALUES (?, ?, ?, ?, ?, ?)";
	static final String RM_CHECK_PUBLIC_STATUS = "SELECT public FROM realms WHERE id=? LIMIT 1";
	static final String RM_LIST_REALMS = "SELECT realmid, name, defaultVisibility FROM layerdata WHERE masterrealmid=?";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RealmManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		Connection				c;
		PreparedStatement		st;
		ResultSet				set;
		String					title;
		String					desc;
		String					defaultTile;
		String					tileSet;
		String					publicFlag;
		Integer					userid;			//	The ID of the user requesting a realm creation.
		

		
		
		//	First question: Is the user permitted?
		
		userid = -1;
		
		if ( request.getSession().getAttribute("userid") != null )
		{
			//	Get the user ID
			
			userid = (Integer) request.getSession().getAttribute("userid");			
			
		} else {
			
			//	No ID available
			
			userid = null;
		}
		
		//	Check for a valid user ID and check for the right to add realms
		
		if ( userid == null || !AuthMan.doesUserHaveRight(userid, AuthMan.Rights.AddRealms) )
		{
			response.sendError(401, "You are not authorized to add realms.");			
			response.flushBuffer();
			
			return;
		}
		
		//	Init the DB parameters
		
		c   = null;
		set = null;			
		st  = null;

		
		//	Grab what we have		
		
		title = request.getParameter("title");
		desc = request.getParameter("desc");
		defaultTile = request.getParameter("defaulttile");
		tileSet = request.getParameter("tileset");
		publicFlag = request.getParameter("publicflag");
		
		//	Since the flag is a check-box, assume it's lack of presence in the query means false.
		
		if ( publicFlag == null || publicFlag.trim().length() == 0 )
			publicFlag = "false";
		
		//	See if we have enough data to proceed
		
		if ( title == null || desc == null || defaultTile == null || tileSet == null  )
		{
			response.sendError(403);
			return;
		}
		
		try {
			//	Prep the query to the database
				
			c = DBResourceManager.getConnection();
		
			//	Insert the data
			
			st = c.prepareStatement(RM_CREATE_REALM, PreparedStatement.RETURN_GENERATED_KEYS);
			
			st.setString(1, title);
			st.setString(2, desc);
			st.setInt(3, Integer.parseInt(tileSet));
			st.setInt(4, Integer.parseInt(defaultTile));
			st.setBoolean(6, Boolean.parseBoolean(publicFlag));
			
			//	Set the user ID
			
			if ( userid > -1 )
				st.setInt(5, userid );
			else
				st.setNull(5, java.sql.Types.INTEGER);
			
			
			st.executeUpdate();
			
			set = st.getGeneratedKeys();
			
			if ( set.next() )
			{				
				int realmID = set.getInt(1);
				
				//	Success; give the user explicit permission to access it fully.
				
				AuthMan.addUserAuthFor(userid, AuthEntities.Realm, realmID, Permission.write );
				
				//	Set our current realm
				
				request.getSession().setAttribute("currentrealm", realmID );
								
				response.sendRedirect("TiledCanvas.jsp");
				response.flushBuffer();
				
			} else {
				
				//	Failure!
				
				response.sendError(401, "You don't have permission to create realms on your account.");
			}
			
			
		
		} catch ( Exception ex )
		{
			ex.printStackTrace();
			response.sendError(402);
		} finally {
			//	Clean-up; notify the creator of the new ID.
			
			DataTools.safeCleanUp(c, st, set);
			
		}
		
		
		return;
	}

	
	
	/**
	 * Check to see if a realm is public or not.
	 * 
	 * @param realmid
	 * @return
	 */
	public static boolean isRealmPublic ( int realmid ) throws SQLException
	{
		boolean		result;		
		QuickCon 	con;
		ResultSet	data;
		
		
		
		// Setup our query 
		
		con = new QuickCon(RM_CHECK_PUBLIC_STATUS);
		
		try {
			
			// Grab our data
			
			con.getStmt().setInt(1, realmid);		
			data = con.query();
			
			if ( data.next() )
			{
				// Stuff the results into our return varaible
				
				result = data.getBoolean(1);
				
			} else {
				result = false;
			}
			
		} finally {
			
			//	Cleanup no matter what
			
			con.release();
		}
		
		
		return result;
	}
	
	
	/**
	 * Constructs an object representing detailed layer data for a given realm to the specified stream.  This is primarily desgined for Javascript/JSP output.
	 * @throws SQLException
	 * @throws IOException 
	 */
	public static String getLayerDataObject ( int realmid ) throws SQLException, IOException {
		
		StringWriter stringOut = new StringWriter();
		// Grab a list of layers and their basic data
		QuickCon dbCon = new QuickCon(RM_LIST_REALMS);
		try {
			dbCon.getStmt().setInt(1, realmid);
			ResultSet results = dbCon.query();
			JsonWriter	jsonOut = new JsonWriter(stringOut);
			// Start the array of objects and list each entry
			jsonOut.beginArray();
			if ( results.next() ) {
				do {
					jsonOut.beginObject();
					jsonOut.name("name").value(results.getString("name"));
					jsonOut.name("realmid").value(results.getInt("realmid"));
					jsonOut.name("visible").value(results.getBoolean("defaultVisibility"));
					jsonOut.endObject();				
				} while ( results.next() );
			} else {
				// No layer data; use just the realm itself.
				jsonOut.beginObject();
				jsonOut.name("name").value("Primary");
				jsonOut.name("realmid").value(realmid);
				jsonOut.name("visible").value(true);
				jsonOut.endObject();
			}
			// End the array		
			jsonOut.endArray();
			jsonOut.flush();
			jsonOut.close();
			stringOut.flush();
			stringOut.close();
		} finally {
			dbCon.release();
		}
		
		return stringOut.toString();
	}
}

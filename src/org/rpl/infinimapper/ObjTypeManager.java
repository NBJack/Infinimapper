package org.rpl.infinimapper;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.sql.*;
import java.util.LinkedList;


/**
 * Encapsulates what is needed in a response as a JSon object
 * @author rplayfield
 *
 */
class FormResponse 
{
	boolean					formAccepted;
	String					result;
	
	LinkedList<String[]>	paramResultPairs;
	LinkedList<String>		messages;
	
	public FormResponse ()
	{
		result			 = "";
		formAccepted 	 = true;
		paramResultPairs = new LinkedList<String[]>();
		messages		 = new LinkedList<String>();
	}
	
	
	/**
	 * Sets a result message. It is recommended that this result be kept very
	 * short and used from a strict set of options (ex. OK, FAIL). If any
	 * detailed info should be passed, use the messages or parameter results.
	 * 
	 * @param r
	 */
	public void setResult ( String r )
	{
		result = r;
	}
	
	
	/**
	 * Determines whether or not the form was accepted.
	 * 
	 * @param b
	 */
	public void setAcceptance ( boolean b )
	{
		formAccepted = b;
	}
	
	/**
	 * Was the form accepted?
	 * 
	 * @return
	 */
	public boolean isAccepted ()
	{
		return formAccepted;		
	}
	
	/**
	 * Add a messages.  Each message will be displayed as a general 
	 * notification.
	 * 
	 * @param m
	 */
	public void addMessage ( String m )
	{
		messages.add(m);
	}
	
	/**
	 * Add a parameter result.  Blank means everything is OK, a message
	 * indicates the result is incorrect.
	 *  
	 * @param name
	 * @param result
	 */
	public void addParamResult ( String name, String result )
	{
		paramResultPairs.add( new String [] {name, result} );
	}
}


/**
 * Servlet implementation class ObjTypeManager. This servlet handles
 * adding, editing, and deleting of object types from the database.
 * 
 */
public class ObjTypeManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String OBJTYPE_INSERT = "INSERT INTO objlib(name, tilesrc, imgXOff, imgYOff, imgWidth, imgHeight, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String OBJTYPE_LIST_JSON = "SELECT * FROM objlib WHERE id > 0 LIMIT ?, ?";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObjTypeManager() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    
    
    void checkIntWithinRange ( HttpServletRequest request, String paramName, int min, int max, FormResponse response )
    {
    	String		rawValue;
    	int			value;
    	
    	
    	
    	//	First, does this parameter exist in the response?
    	
    	rawValue = request.getParameter(paramName);
    	
    	if ( rawValue == null )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Field is missing from request.");
    		
    		return;
    	}
    	
    	//	Parse the raw value into an integer
    	
    	try {
    		
    		value = Integer.parseInt(rawValue);
    	} catch ( Exception ex )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Value is not an integer");
    		
    		return;
    	}
    	
    	//	Check to see if the integer is within range.
    	
    	if ( value > max )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Value cannot be greater than " + max);
    		
    		return;
    	}
    	
    	if ( value < min )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Value cannot be less than " + min);
    		
    		return;
    	}
    	
    	//	All checks have been cleared
    	
		response.addParamResult(paramName, "");
    }
    
    
    /**
     * Just see if a parameter is present or not, and update the response as
     * appropriate.
     * 
     * @param request
     * @param paramName
     * @param response
     */
    void checkIsValidString ( HttpServletRequest request, String paramName, FormResponse response, boolean canBeBlank )
    {
    	String		rawValue;
    	
    	
    	
    	rawValue = request.getParameter(paramName);
    	
    	if ( rawValue == null )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Field is missing from request.");
    		
    		return;
    	}
    	
    	if ( rawValue.trim().length() == 0 && !canBeBlank )
    	{
    		response.setAcceptance(false);
    		response.addParamResult(paramName, "Value cannot be blank.");
    	}
    	
    	
    	//	Parameter is present; acccept it.
    	
    	response.addParamResult(paramName, "");
    }
    
    
    /**
     * Performs basic validation that insures the object added is correctly
     * specified. No database lookups or image dimension lookups are done.
     * 
     * @param request
     * @return
     */
    FormResponse validateAddForm ( HttpServletRequest request )
    {
    	FormResponse	resp;
    	
    	
    	
    	//	Create a new response
    	
    	resp = new FormResponse(); 
    	
    	//	Check all entries
    	
    	
    	checkIntWithinRange(request, "objXOff", 0, 4096, resp); 
    	checkIntWithinRange(request, "objYOff", 0, 4096, resp);
    	checkIntWithinRange(request, "objWidth", 0, 1024, resp);
    	checkIntWithinRange(request, "objHeight", 0, 1024, resp);
    	checkIntWithinRange(request, "tileset", 0, Integer.MAX_VALUE, resp);
    	checkIntWithinRange(request, "objid", Integer.MIN_VALUE, Integer.MAX_VALUE, resp);
    	checkIsValidString(request, "title", resp, false);
    	checkIsValidString(request, "desc", resp, true);
    	
    	//	Return the response
    	
    	return resp;
    }
    
    /**
     * When browsing objects, we'd like to make it possible to interface the object type list
     * with a Dojo Data Source. To do so, we stream the objects out as a JSON object.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    
    private void doObjTypeRequestJSON (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	int					fromIndex, maxResult;
		Connection			c;
		PreparedStatement	st;
		ResultSet			set;		
		JsonWriter			jOut;
		int					recCount;
		
		
		
		
		//	Set the content up
		
		response.setContentType("application/json");
		
		//	Setup our database information		
		
		c = null;
		st = null;
		set = null;
		
		//	Setup the rest of our environment
		
		recCount = 0;
		
		jOut = new JsonWriter( response.getWriter() );

		
		try {
			
			//	Parse request parameters
			
			fromIndex = Integer.parseInt(request.getParameter("start"));
			maxResult = Integer.parseInt(request.getParameter("count"));
			
			//	Create connection to the database and prep the query
			
			c = DBResourceManager.getConnection();
			
			st = c.prepareStatement(OBJTYPE_LIST_JSON);
			st.setInt(1, fromIndex);
			st.setInt(2, maxResult);
			
			//	Execute the query
			
			set = st.executeQuery();
			
			//	Now, stream the results out as if they were an object
			
			jOut.beginObject();
			jOut.name("items").beginArray();
			
			
			while ( set.next() )
			{
				//	Write each element out as an object
				
				jOut.beginObject();
				
				jOut.name("objid").value(set.getInt("id"));
				jOut.name("objname").value(set.getString("name"));
				jOut.name("objtilesrc").value(set.getString("tilesrc"));
				jOut.name("description").value(set.getString("description"));
				
				jOut.endObject();

				//	Increment the record count
				
				recCount++;
			}

			//	Terminate the array
			
			jOut.endArray();
			
			//	Note the number of rows
						
			jOut.name("numRows").value(recCount);
			
			//	End the overall object
			
			jOut.endObject();
			
			//	Flush the output
			
			jOut.flush();
			
		} catch ( Exception ex )
		{
			ex.printStackTrace();
			
		} finally {
			
			DataTools.safeCleanUp(c, st, set);			
			
		}
    }

    /**
     * For GETs, only basic queries are permitted.
     * 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException {
    	

    	doObjTypeRequestJSON(req, resp);
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String		action;
		
		
		
		//	Determine the type of action desired
		
		action = request.getParameter("action");
		
		if ( action == null )
		{
			//	Assume we're looking for a list
			
			action = "listjson";		
		}
		
		
		if ( action.equals("add") )
		{
			//	Add an object 
			
			addObject(request, response);
			
		} else if ( action.equals("listjson") )
		{
			//	Object listing via JSON for use in data sources
			
			doObjTypeRequestJSON(request, response);
		}
		
	}
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void addObject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Connection			c;
		PreparedStatement	st;
		ResultSet			set;
		
		String		objName;
		int			objTileSrc;
		int			objXOff;
		int			objYOff;
		int			objWidth;
		int			objHeight;
		String		objDesc;
		int			objId;
		int 		objAuth;
		Gson		Gson;
		FormResponse	resp;
		
		
		
		resp = null;
		
		c = null;
		st = null;
		set = null;
		
		
		Gson = new Gson();
		
		try {
			
			//	Verify we have authorization to do this! TODO: Set a special response code on authorization failure.
			
			//	Perform a quick, cursory validation
			
			resp = validateAddForm(request);
			
			if ( !resp.isAccepted() )
			{
				resp.setResult("FAIL");
				
			} else {
			
				//	Apply parameters
				
				objName 	= request.getParameter("title");
				objDesc 	= request.getParameter("desc");
				objTileSrc	= Integer.parseInt(request.getParameter("tileset"));
				objXOff		= Integer.parseInt(request.getParameter("objXOff"));
				objYOff		= Integer.parseInt(request.getParameter("objYOff"));
				objWidth	= Integer.parseInt(request.getParameter("objWidth"));
				objHeight	= Integer.parseInt(request.getParameter("objHeight"));
				//objAuth 	= (Integer) request.getSession().getAttribute("userid");
				//TODO: Get the public flag
				
				System.out.println("Object name: '" + objName + "'" );
				
				//	Generate the query
				
				c = DBResourceManager.getConnection();
				
				st = c.prepareStatement(OBJTYPE_INSERT, PreparedStatement.RETURN_GENERATED_KEYS );
				
				st.setString(1, objName);
				st.setInt(2, objTileSrc);
				st.setInt(3, objXOff);
				st.setInt(4, objYOff);
				st.setInt(5, objWidth);
				st.setInt(6, objHeight);
				st.setString(7, objDesc);
				
				st.execute();
				
				set = st.getGeneratedKeys();
				
				//	Grab the generated key
				
				
				
				if ( set.next() )
				{
					objId = set.getInt(1);
					
					resp.setResult("OK");
					resp.addParamResult("objId", "VALUEUP:" + objId);
					
					//	Send the OK. Note that Dojo requires us to wrap the response
					//	in an html doc's text area for max. compatibility.
					
					
				} else {
					
					System.out.println("Failure to create object.");
					
					resp.setResult("FAIL");
					
				}
			}

			//	Send the resposne object no matter what
			
			response.getWriter().println("<html><body><textarea>" + Gson.toJson(resp) + "</textarea></html></body>");
			
			
		} catch ( Exception ex )
		{
			ex.printStackTrace();
			
			if ( resp == null )
			{
				resp = new FormResponse();						
				resp.setAcceptance(false);
			}
			
			resp.setResult("FAIL");
			resp.addMessage(ex.toString());
			
			response.getWriter().println("<html><body><textarea>" + ex.toString() + "</textarea></html></body>");
			
		} finally {
			
			DataTools.safeCleanUp(c, st, set);			
			
		}
		
		
	}

}

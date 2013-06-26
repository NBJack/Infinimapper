package org.rpl.infinimapper.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class RegisterMan
 */
public class RegisterMan extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterMan() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String		username;
		String		phash;
		String		email;
		JsonWriter		responseJson;
		boolean		wasSuccessful;
		String		resultMsg;
		
		
		
		
		//	Init our response
		
		wasSuccessful = false;
		resultMsg 	  = "";

		
		//	Create the Json object to construct our response
		
		responseJson = new JsonWriter(response.getWriter());
		
		//response.setContentType("application/json");
		
		
		try {
			
			//	Grab the data we need
			
			username = request.getParameter("username");
			phash	 = request.getParameter("phash");
			email	 = request.getParameter("email");
			
			//	Have sll fields been provided?
			
			if ( username == null ||  phash == null || email == null )
			{
				resultMsg = "All fields must be provided.";
			} 
			
			// 	TODO: Perform thorough validation of field sizes.
			
			//	Try to add the user
			//	TODO: Check for whether they already exist via e-mail
			
			if ( AuthMan.doesUserExist(username, email) )
			{
				//	Send an appropriate message
				
				resultMsg = "Username and/or e-mail already exists.";
				
			} else {
			
				// Attempt to add the user
				
				if ( AuthMan.addUser(username, phash, email) )
				{
					wasSuccessful = true;
				} else {
					resultMsg = "Registration was not successful.";
				}
			}
			
		} catch ( Exception ex )
		{
			resultMsg = "Error during registration:" + ex.toString();
		}
		
		
		System.out.println("Result of new user: " + resultMsg);
		
		//	Write the response
		
		response.getWriter().println("<html><body><textarea>");
		
		responseJson.beginObject();
		responseJson.name("message").value(resultMsg);
		responseJson.name("success").value(wasSuccessful);
		responseJson.endObject();
		
		responseJson.flush();
		
		response.getWriter().println("</textarea></body></html>");

		response.flushBuffer();
				
	}

}

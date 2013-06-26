package org.rpl.infinimapper;

import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rpl.infinimapper.security.AuthMan;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A servlet designed to handle login requests.
 * 
 */
public class LoginMan extends HttpServlet {
	private static final long serialVersionUID 		= 1L;
    
    
    /**
     * Non-thread safe secure random number generator. 
     */
	SecureRandom	sRand;
	
	/**
	 * A credential map to quickly determine if someone is logged-in.
	 */
	static final ConcurrentHashMap<String, String>		credMap = new ConcurrentHashMap<String, String>();
    
    public LoginMan() {
        super();
        
        sRand = new SecureRandom();
    }
    
    
    /**
     * Generate an authentication key for use in subsequent calls.
     * 
     * @return
     */
    synchronized protected String generateAuthenticationKey ()
    {
    	BigInteger		bigInt;
    	
    	
    	
    	bigInt = new BigInteger(32, sRand);
    	
    	return bigInt.toString(32);
    }
    
    
    /**
     * Do a quick look-up of the user for the authentication key provided.
     * TODO: Add a much more robust system for user login status.
     * 
     * @param authKey
     * @return
     */
    public String verifyAuthKey ( String authKey )
    {
    	return credMap.get(authKey);
    }
    

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String			userName;
		String			loginHash;
		String			authKey;
		int				userid;
		
		
		
		
		
		
		userName = request.getParameter("username");
		loginHash= request.getParameter("phash");
		
		
		//	Check to see if this user is authenticated		
		
		userid = AuthMan.verifyUser(userName, loginHash);
		
		if ( userid >= 0 )
		{
			//	Generate a secure key
			
			authKey = generateAuthenticationKey();
			
			//	Setup the session credentials and authentication set
			
			request.getSession().setAttribute("username",userName);
			request.getSession().setAttribute("authkey", authKey );
			request.getSession().setAttribute("userid", userid );
			
			// Move them to the main editing page
			
			response.sendRedirect("TiledCanvas.jsp");
			
			//	Mention it in the log
			
			System.out.println("Logged in: " + userName);
			
			//	Note their credentials in our manager
			
			credMap.put(authKey, userName);
			
		} else {
			response.getWriter().print("User login failed for " + userName );
			
			
		}
	}
	

	

}

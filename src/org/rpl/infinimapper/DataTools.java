package org.rpl.infinimapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

public class DataTools {

	/**
	 * Prepares a string for storage as string in Javascript code. Does not include
	 * the actual quotations, but instead replaces special disruptive characters
	 * where needed.
	 * 
	 * @param original
	 * @return
	 */
	public static String prepStringForJavascript ( String original )
	{
		String		result;
		
		
		
		result = original;
		result = result.replace("\n", "\\n");
		result = result.replace("\r", "\\r");
		result = result.replace("\"", "\\\"");
		
		return result;
	}
	
	
	/**
	 * Checks a list of parameters against a servlet request. If anything is missing, returns false.
	 * Otherwise, returns true.
	 * 
	 * @param namesToCheck
	 * @param request
	 * @return
	 */
	public static boolean areParameterNamesPresent ( String [] namesToCheck, HttpServletRequest request )
	{
		for ( String s : namesToCheck )
		{
			if ( request.getParameter(s) == null )
				return false;
		}
		
		return true;
	}


	/**
	 * An often-used clean-up routine that ensures all passed
	 * resources are safely closed (if not null). All exceptions
	 * are silently caught. If a parameter is not initialized, 
	 * it should be null.
	 * 
	 * @param c
	 * @param st
	 * @param set
	 */
	public static void safeCleanUp ( Connection c, Statement st, ResultSet set )
	{
		try {
			if ( c != null )
				c.close();
		} catch ( Exception ex ) {};
	
		try {
			if ( set != null )
				set.close();
		} catch ( Exception ex ) {};
	
		try {
			if ( st != null )
				st.close();
		} catch ( Exception ex ) {};		
	}
}

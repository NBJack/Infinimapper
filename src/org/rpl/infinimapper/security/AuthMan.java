package org.rpl.infinimapper.security;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import org.rpl.infinimapper.DBResourceManager;
import org.rpl.infinimapper.DataTools;
import org.rpl.infinimapper.WorldDB;
import org.rpl.infinimapper.WorldDB.QuickCon;
import org.rpl.infinimapper.security.UserPermissionCache.Permission;


/**
 * Manages authorization clearances for various parts of the system. 
 * 
 * 
 * @author rplayfield
 *
 */
public class AuthMan {
	
	
	/**
	 * Basic rights permitted in the system.
	 * 
	 * @author rplayfield
	 *
	 */
	public enum Rights {
		
		Admin     (1),
		AddRealms (2),
		AddImages (3);
		
		
		
		/**
		 * The unique, persistent ID for authorization in the database.
		 */
		int		id;
		
		
		Rights( int id )
		{
			this.id = id;
		}
		
		public int getDBID ()
		{
			return id;
		}
	}
	
	public enum AuthEntities {
		
		//	Entities that need authorization
		
		Realm ("realm"),
		Image ("image"),
		Right ("right");

		//	Stores the entity string used in the database
		
		String		s;
		

		AuthEntities (String s)
		{
			this.s = s;
		}
		
		
		/**
		 * What name do we use for this type in the database's enumerations?
		 * 
		 * @return
		 */
		public String getDBName ()
		{
			return s;
		}
		
	}
	
	
	private static byte [] LOGIN_FINAL_SALT;
	
	

	/**
	 * Verify that a user's credentials are valid.  
	 */
	private static final String AUTH_VERIFY_USER_CRED = "SELECT userid FROM users WHERE username=? AND phash=? AND enabled=True LIMIT 1";
	
	/**
	 * Change a user's status (whether they can login or not)
	 */
	private static final String AUTH_CHANGE_USER_STATUS = "UPDATE users SET enabled=? WHERE userid=?";
	
	/**
	 * Record a user's successful login by updating the last-login timestamp.
	 */
	private static final String AUTH_RECORD_USER_LOGIN = "";
	
	/**
	 * Add a new user to the database. All details must be provided
	 */
	private static final String AUTH_ADD_USER = "INSERT INTO users (username, phash, email) VALUES (?, ?, ?)";
	
	/**
	 * Is the user authorized to see this entity?  Assumes entity is not public.
	 */
	private static final String AUTH_GET_ENTITY_AUTH_FOR_USER = "SELECT idauth FROM userauth WHERE userid=? AND objtype=? AND objid=? LIMIT 1";	
	
	/**
	 * Adds a user to the realm's authorization list.
	 */ 
	private static final String AUTH_ADD_ENTITY_AUTH_FOR_USER = "INSERT INTO userauth(userid, objtype, objid, authtype) VALUES (?,?,?,?)";
	
	private static final String AUTH_USER_GET_REALM_PERMISSIONS = "SELECT authtype FROM userauth WHERE objtype='realm' AND objid=? AND userid=? LIMIT 1";

	
	/**
	 * Construct a final hash for use in the database 
	 * 
	 * @param loginHash
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	private static byte [] finalizeStringHash ( String loginHash ) throws NoSuchAlgorithmException
	{
		MessageDigest	md5Hasher;
		
		
		
		md5Hasher = MessageDigest.getInstance("MD5");
		
		md5Hasher.update(loginHash.getBytes());
		md5Hasher.update(LOGIN_FINAL_SALT);			

		return md5Hasher.digest();		
	}

	
	
	
	/**
	 * Grant a user access to a specific resource
	 * 
	 * @param userid
	 * @param entType
	 * @param entID
	 */
	public static void addUserAuthFor ( int userid, AuthEntities entType, int entID, Permission permission )
	{
		Connection			c;
		ResultSet			rs;
		PreparedStatement	st;
		
		

		c  = null;
		st = null;
		rs = null;
		
		 
		try {
			
			//	Setup the connection
			
			c = DBResourceManager.getConnection();
			
			//	Setup the query			
			
			st = c.prepareStatement(AUTH_ADD_ENTITY_AUTH_FOR_USER);
			
			st.setInt(1, userid);
			st.setString(2, entType.getDBName());
			st.setInt(3, entID);
			st.setString(4, permission.name());
			
			//	Execute
			
			st.execute();
			
			
			
		} catch ( Exception ex )
		{
			//	Note the problem
			
			ex.printStackTrace();
			
		} finally {
			
			//	Safely destroy resources
			
			DataTools.safeCleanUp(c, st, rs);
			
		}
	}
	
	
	/**
	 * Checks to see if a user is authorized for a particular entity.
	 * 
	 * @param userid
	 * @param entType
	 * @param entID
	 * @return
	 */
	public static boolean isUserAuthFor ( int userid, AuthEntities entType, int entID )
	{
		boolean				resultFlag;
		Connection			c;
		ResultSet			rs;
		PreparedStatement	st;
		
		

		c  = null;
		st = null;
		rs = null;
		
		resultFlag = false;
		 
		try {
			
			//	Setup the connection
			
			c = DBResourceManager.getConnection();
			
			//	Setup the query			
			
			st = c.prepareStatement(AUTH_GET_ENTITY_AUTH_FOR_USER);
			
			st.setInt(1, userid);
			st.setString(2, entType.getDBName());
			st.setInt(3, entID);
			
			//	Execute
			
			rs = st.executeQuery();
			
			//	If we had a result at all, we've got authorization.
			
			if ( rs.next() )
			{
				resultFlag = true;
			}
			
			
		} catch ( Exception ex )
		{
			//	Note the problem
			
			ex.printStackTrace();
			
		} finally {
			
			//	Safely destroy resources
			
			DataTools.safeCleanUp(c, st, rs);
			
		}
		
		
		//	Return our calculated result
		
		return resultFlag;
	}
	
	

	
	
	/**
	 * Determine whether or not the provided user credentials are valid or not.
	 * If successful, returns the user's ID.  Otherwise, returns -1. 
	 * 
	 * @param userName
	 * @param loginHash
	 * @return
	 */
	public static int verifyUser ( String userName, String loginHash )
	{
		Connection				c;
		PreparedStatement		st;
		ResultSet				rs;
		byte []					finalHash;
		int						userID;
		

		
		c  = null;
		st = null;
		rs = null;
		
		userID = -1;
		
		
		try {		
			
			//	Construct the hash

			finalHash = finalizeStringHash(loginHash);
			
			//	Setup the query
			
			c = DBResourceManager.getConnection();
			
			st = c.prepareStatement(AUTH_VERIFY_USER_CRED);
			
			st.setString(1, userName);
			st.setBytes(2, finalHash);
			
			System.out.println("Trying to verify " + userName + " with hash " + finalHash);
			
			//	Execute
			
			rs = st.executeQuery();
			
			//	Do the results yield a match?
			
			if ( rs.next() )
			{				
				userID = rs.getInt(1);					
			}
			
			
			
		} catch ( Exception ex )
		{
			//	Report an error.
			
			ex.printStackTrace();
			
		} finally {
			
			//	Safely destroy resources
			
			DataTools.safeCleanUp(c, st, rs);
		}
		
		
		//	Return the user ID, if any, found.
		
		return userID;
	}
	
	
	/**
	 * Checks to see if the username or email is already registered.
	 * 
	 * @param userName
	 * @param email
	 * @return
	 */
	public static boolean doesUserExist ( String userName, String email )
	{
		
		return false;
	}
	
	
	
	/**
	 * Adds a user to the database. Note that, by default, all new user accounts
	 * must be enabled before they can be used.
	 * 
	 * @param userName
	 * @param loginHash
	 * @param email
	 * @return
	 */
	public static boolean addUser ( String userName, String loginHash, String email )
	{
		Connection				c;
		PreparedStatement		st;
		ResultSet				rs;
		byte []					finalHash;
		boolean					successFlag;

		
		
		c  = null;
		st = null;
		rs = null;
		
		successFlag = false;
		
		try {
		
			//	Construct the hash

			finalHash = finalizeStringHash(loginHash);
			
			//	Setup the query
			
			c = DBResourceManager.getConnection();
			
			st = c.prepareStatement(AUTH_ADD_USER);
			
			st.setString(1, userName);
			st.setBytes(2, finalHash);
			st.setString(3, email);
			
			//	Execute
			
			st.execute();
			
			System.out.println("Adding " + userName);
			
			//	If we made it here, we're fine.
			
			successFlag = true;
			
			//	Note it in the log.
			
		} catch ( Exception ex )
		{
			ex.printStackTrace();
			
			successFlag = false;
			
		} finally 
		{
			//	Safely destroy resources
			
			DataTools.safeCleanUp(c, st, rs);
		}
		
		
		return successFlag;
	}
	

	
	
	/**
	 * Sets a user's status to enabled or disabled.
	 * 
	 * @param userid The ID of the user.
	 * @param status The status to set them to.
	 * @return
	 */
	public static void setUserStatus ( int userid, boolean status )
	{
		Connection			c;
		ResultSet			rs;
		PreparedStatement	st;
		
		

		c  = null;
		st = null;
		rs = null;
		
		 
		try {
			
			//	Setup the connection
			
			c = DBResourceManager.getConnection();
			
			//	Setup the query			
			
			st = c.prepareStatement(AUTH_CHANGE_USER_STATUS);
			
			st.setBoolean(1, status);
			st.setInt(2, userid);
			
			//	Execute
			
			rs = st.executeQuery();
			
			
			
		} catch ( Exception ex )
		{
			//	Note the problem
			
			ex.printStackTrace();
			
		} finally {
			
			//	Safely destroy resources
			
			DataTools.safeCleanUp(c, st, rs);
			
		}
		
		
	}

	/**
	 * Checks to see if a user has a particular right.
	 * 
	 * @param userid
	 * @param right
	 * @return
	 */
	public static boolean doesUserHaveRight ( int userid, Rights right )
	{
        return true;
		//return isUserAuthFor( userid, AuthEntities.Right, right.getDBID() );
	}
	
	
	public static void setSalt ( String str )
	{
		LOGIN_FINAL_SALT = str.getBytes();
	}




	/**
	 * Retrieves the type of permission a specified user has for the specified realm.
	 * 
	 * @param userid The user id of the logged-in user. If they are not logged in, use
	 * value
	 */
	public static Permission getRealmPermissionsForUser ( int userid, int realmid ) throws SQLException
	{
		Permission				result;
		WorldDB.QuickCon				con;
		
		
		
		// Nullify our connection and default to 'None' for permissions.
		
		con = null;
		result = Permission.none;
		
		try {
	
			// See if specific user permissions specify the realm if a valid
			// user ID was provided.
	
			con = new WorldDB.QuickCon(AUTH_USER_GET_REALM_PERMISSIONS);
			
			con.getStmt().setInt(1, realmid);
			con.getStmt().setInt(2, userid);
			
			ResultSet data = con.query();
			
			if ( data.next() )
			{
				//	Convert the result into an enumeration we can use.
				
				result = Permission.valueOf(data.getString(1));
			} else {
				
				//	Default to no permissions if data wasn't found.
				
				result = Permission.none;
			}
		} catch ( Exception ex )
		{
		
		} finally {
			
			// Clean up all resources
			
			if ( con != null )
				con.release();
		}
		
		return result;
	}
}

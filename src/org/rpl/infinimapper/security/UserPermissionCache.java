package org.rpl.infinimapper.security;

import java.util.concurrent.ConcurrentHashMap;





/**
 * Keeps track of what a user can (and can't) access.  In the event of a
 * permissions change-up, this cache should be invalidated.
 * 
 * @author rplayfield
 *
 */
public class UserPermissionCache {
	
	

	/**
	 * Types of permissions the user has for a particular object.
	 * 
	 * @author rplayfield
	 *
	 */
	public static enum Permission {
		
		
		/**
		 * The user can read data.
		 */
		read(true, false),	
		/**
		 * The user can write data (implies read)
		 */
		write(true, true),
		/**
		 * The user cannot access anything about the object.
		 */
		none(false, false);
		
		
		boolean readFlag;
		boolean writeFlag;
		
		Permission(boolean canRead, boolean canWrite)
		{
			this.readFlag = canRead;
			this.writeFlag = canWrite;
		}
		
		public boolean canRead()
		{
			return this.readFlag;			
		}
		
		public boolean canWrite()
		{
			return this.writeFlag;
		}
	}
	
	
	/**
	 * Manages information regarding realm permissions 
	 */
	ConcurrentHashMap <Integer, Permission> realmPermissions = new ConcurrentHashMap<Integer, Permission>();
	
	
	
	
	public UserPermissionCache ()
	{
		
	}
}

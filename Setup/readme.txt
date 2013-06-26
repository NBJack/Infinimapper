
=======================================
  Boot-Strapping Infinimapper
=======================================

To setup Infinimapper, you're going to need the following:

 * MySQL v5.x (or better)
 * Tomcat v6.x (or better)
 * 756MB of RAM on your host
 * ~200MB of Space (YMMMV) on your host

The rough outline goes as follows:

1. Setup your MySQL database with a set of credentials
2. Run the CreateDatabase.sql script by 'restoring' it to your database
3. Setup your setup.properties file (located in WebContent\META-INF) with the following values:

 db.driver=com.mysql.jdbc.Driver
 db.JdbcURL=jdbc:mysql://your_host/jacobsdefense?autoReconnectForPools=true&autoReconnect=true
 db.user=your_user_name
 db.password=your_password
 db.maxIdleTime=180
 pw.finalsalt=your_salt

4. Replace your_user_name and your_password with the credentials you established in step 1.   I 
  strongly recommend changing your_salt to something else, preferrably secret; this will be used
  to ensure your users' login credentials are kept safe. Don't forget to set your_host to the 
  server where your database is hosted (can be localhost).
5. Boot your server 
6. Upload a tileset of your choice (uploadtiles.html)
7. Create a default object (editobject.jsp)
8. See if your setup is correct by viewing TiledCanvas.jsp


This is a DRAFT of the instructions.  If you need any help, contact:

	me@ryanlayfield.com
	
	
- Ryan
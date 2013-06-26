<%@page import="org.rpl.infinimapper.*, java.sql.*, java.util.*"%>
<%@page import="org.rpl.infinimapper.data.export.*"%>
        <%@ page import="org.springframework.web.context.support.*"%>
        <%@ page import="org.springframework.web.context.WebApplicationContext"%><%@ page import="org.rpl.infinimapper.data.management.RealmCache"%><%@ page import="org.rpl.infinimapper.data.Realm"%>
        <%@ page language="java" contentType="text/javascript; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%
        WebApplicationContext appCon = WebApplicationContextUtils.getWebApplicationContext(application);
 Enumeration e = application.getAttributeNames();

%>
<% 
String				realmID;
Connection			c;
PreparedStatement	prepSt;
ResultSet			set;



c = null; 
prepSt = null;
set = null;


//	Retrieve the realm information request

realmID = request.getParameter("id");
RealmCache realmCache = (RealmCache) appCon.getBean("RealmCache");
Realm realm = realmCache.getValue(Integer.parseInt(realmID));

if ( realmID == null )
{
%>

var realmLoadError = 'Could not retrieve the requested tile sets information';

<%	
} else {

c = DBResourceManager.getConnection();

//	Ask about the realm information and tile information in a single query.

prepSt = c.prepareStatement("SELECT rlm.name,rlm.description,rlm.tileset,rlm.defaulttile,tile.name,tile.tilecount,tile.tilewidth,tile.defaulttile,tile.usebackground,tile.description,tile.border,tile.spacing FROM realms as rlm, tilelib as tile WHERE rlm.id=? AND tile.id=rlm.tileset;");

prepSt.setInt(1, Integer.parseInt(realmID));

set = prepSt.executeQuery();


if (!set.next())
{
	%>
	var realmLoadError = 'Could not retrieve the requested tile sets information';
	<%
} else {

	// Fill-out the javascript template.
%>
// The new Javascript generator

var realmInfo;
var imageInfo;
var tileScale;



// The realm data 

realmInfo            = new Object();

realmInfo.id          = <%= realmID %>;                          // <%= realm.getId() %>
realmInfo.name        = "<%= set.getString(1) %>";               // <%= realm.getName() %>
realmInfo.description = "<%= DataTools.prepStringForJavascript(set.getString(2)) %>"; // <%= realm.getDescription() %>
realmInfo.tileset     = <%= set.getInt(3) %>;
realmInfo.tiledefault = <%= set.getInt(4) %>;                    // <%= realm.getDefaulttile()%>
realmInfo.layers = <%= RealmManager.getLayerDataObject(Integer.parseInt(realmID)) %>;

// The tileset data

imageInfo           = new Object();

imageInfo.id        = <%= set.getInt(3) %>;
imageInfo.name      = "<%= set.getString(5)%>;";
imageInfo.tilecount = <%=set.getInt(6)%>;
imageInfo.tilewidth = <%=set.getInt(7)%>;                        // <%= realm.getTileWidth() %>
imageInfo.tileheight= <%=set.getInt(7)%>;                        // <%= realm.getTileHeight() %>
imageInfo.tilesPerRow = 3;
imageInfo.tiledefault= <%=set.getInt(8)%>;                       // <%= realm.getDefaulttile() %>
imageInfo.useBackground = true;
imageInfo.description = "<%=DataTools.prepStringForJavascript(set.getString(10))%>";

imageInfo.supplement = <%= Arrays.toString(TilesetExport.getSupplementalTilesetsForRealm(Integer.parseInt(realmID)).toArray()) %>;

imageInfo.border      = <%=set.getInt(11)%>;
imageInfo.gap         = <%=set.getInt(12)%>;

tileScale = <%=set.getInt(7)%>;

<%

}
}

// Free up resources

try {
	
	c.close();
	
	if ( set != null )
		set.close();
	
	if ( prepSt != null )
		prepSt.close();
	
} catch ( Exception ex )
{

}
%>
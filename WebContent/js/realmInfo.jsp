<%@page import="org.rpl.infinimapper.*, java.sql.*, java.util.*"%>
<%@page import="org.rpl.infinimapper.data.export.*"%>
        <%@ page import="org.springframework.web.context.support.*"%>
        <%@ page import="org.springframework.web.context.WebApplicationContext"%><%@ page import="org.rpl.infinimapper.data.management.RealmCache"%><%@ page import="org.rpl.infinimapper.data.Realm"%><%@ page import="org.rpl.infinimapper.data.TilesetData"%><%@ page import="org.rpl.infinimapper.data.management.TilesetProvider"%><%@ page import="org.rpl.infinimapper.data.management.TilesetAssignmentProvider"%><%@ page import="org.rpl.infinimapper.data.TilesetAssignment"%>
        <%@ page language="java" contentType="text/javascript; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%
WebApplicationContext appCon = WebApplicationContextUtils.getWebApplicationContext(application);

//	Retrieve the realm information request. Not how I like to do it, but necessary until I
//  get Spring hosting this information.

String realmID = request.getParameter("id");
RealmCache realmCache = (RealmCache) appCon.getBean("RealmCache");
Realm realm = realmCache.getValue(Integer.parseInt(realmID));

if ( realm == null ) {

    %> alert("Could not locate the specified realm!"); <%
}

// Retrieve the tileset information from the first tileset present
TilesetAssignmentProvider tileAssignProvider = (TilesetAssignmentProvider) appCon.getBean("TilesetAssignmentProvider");
TilesetProvider tilesetProvider = (TilesetProvider) appCon.getBean("TilesetProvider");
List<TilesetAssignment> assignments = tileAssignProvider.getAllTilesetsForRealm(realm);
TilesetData tileset = tilesetProvider.getValue(assignments.get(0).getTilesetId());

// Prepare the way by assigning the major data points to the request, allowing use of EL (which is how Spring will deliver in the future).
request.setAttribute("realm", realm);
request.setAttribute("tilesetInfo", tileset);



	// Fill-out the javascript template.
%>
// The new Javascript generator

var realmInfo;
var imageInfo;
var tileScale;



// The realm data 

realmInfo            = new Object();

realmInfo.id          = ${realm.id};
realmInfo.name        = "${realm.name}";
realmInfo.description = "<%= DataTools.prepStringForJavascript(realm.getDescription()) %>";
realmInfo.tiledefault = ${realm.defaulttile};
realmInfo.layers = <%= RealmManager.getLayerDataObject(realm.getId()) %>;

// The tileset data
// TODO: Get the information on the first tileset from a provider.
imageInfo           = new Object();

imageInfo.id        = ${tilesetInfo.id};
imageInfo.name      = "${tilesetInfo.name}";
imageInfo.tilecount = 1;
imageInfo.tilewidth = ${realm.tileWidth};
imageInfo.tileheight= ${realm.tileHeight};
imageInfo.tilesPerRow = 3;
imageInfo.tiledefault= ${realm.defaulttile};
imageInfo.useBackground = true;
imageInfo.description = "<%=DataTools.prepStringForJavascript(realm.getDescription())%>";

imageInfo.supplement = <%= Arrays.toString(TilesetExport.getSupplementalTilesetsForRealm(realm.getId()).toArray()) %>;

imageInfo.border      = ${tilesetInfo.border};
imageInfo.gap         = ${tilesetInfo.spacing};

tileScale = ${tilesetInfo.tileWidth};


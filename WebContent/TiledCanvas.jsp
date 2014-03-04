<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<!--

Infinimapper - Browser-based tiled map editing tool
Copyright (C) 2011 Ryan Layfield

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

http://www.ryanlayfield.com/

-->

<%

int		initialX;
int		initialY;

//
//	Staging of initial parameters
//

initialX     = 0;
initialY	 = 0;



//	We need to figure out which realm the user is interested in. The hierarchy 
//	runs as follows, with the highest priority set on top:
//	   - Request parameter 'realm'
//	   - Session variable 'currentrealm'
//  We do it this way in order to allow other services to shift our realm of
//	interest.






%>
<html>
<link rel="stylesheet" href="corestyle.css" type="text/css" />
<meta name="viewport"
    content="user-scalable=no, width=device-width" />
<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
<!-- <script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojo/dojo.xd.js" djConfig="parseOnLoad: true"></script> -->
<script src="js/dojo/dojo.js.uncompressed.js" djConfig="parseOnLoad: true"></script>
<title>Infinimapper v0.80alpha</title>
<body onload="setup()" oncontextmenu="return false;" ontouchmove="blockEvent();" >
<table id='menubar' width=200 height=36>
<tbody>
<tr><td>
	<div class="toolribbon">
	<img alt="Move" id="toolMove" src="toolbar/arrow_out.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<!-- <img alt="Undo" id="toolUndo" src="toolbar/arrow_rotate_anticlockwise.png" width="32" height="32" class="ToolUnselected"  onClick="doToolClick(this);" ontouchstart="doToolClick(this);">-->
	<img alt="Draw Tile" id="toolDraw" src="toolbar/billiard_marker.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<!-- <div class="subribbon" id="drawribbon" style="display:none;"> -->
		<img alt="Erase Tile" id="toolDefault" src="toolbar/draw_eraser.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<!-- <img alt="Layers" id="toolLayer" src="toolbar/layers.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);"> -->
	<img alt="Select Tiles" id="toolTileSelect" src="toolbar/select.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<!-- </div> -->
	<!--<img alt="Properties" id="toolProperties" src="toolbar/book.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">-->
	<img alt="Export" id="toolExport" src="toolbar/box_down.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<!--  <img alt="Delete" id="toolDelete" src="toolbar/cross.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this); ontouchstart="doToolClick(this);"">
	<img alt="Copy" id="toolCopy" src="toolbar/cut_red.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Save" id="toolSave" src="toolbar/disk.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">-->	
	<!--<img alt="Claim Tile" id="toolClaim" src="toolbar/flag_2.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Lock/Unlock Chunk" id="toolLock" src="toolbar/key.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">-->
	<!-- </div> -->
	<!-- </div> -->
	<img alt="Bookmark" id="toolBookmark" src="toolbar/asterisk_orange.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Add Region" id="toolRegion" src="toolbar/layer_edit.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Add Object" id="toolObjAdd" src="toolbar/chess_horse.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Select Object" id="toolObjSelect" src="toolbar/chess_horse_select.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Delete Object" id="toolObjDelete" src="toolbar/chess_horse_delete.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="Change Realm" id="toolRealm" src="toolbar/photos.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">
	<img alt="About" id="toolAbout" src="toolbar/ask_and_answer.png" width="32" height="32" class="ToolUnselected" onClick="doToolClick(this);" ontouchstart="doToolClick(this);">


	<span  id="statusToolText">...</span>
	<!--
	<%
	String		userName;
	
	
	userName = (String) session.getAttribute("username");

	if ( userName == null )
		userName = "<a href='login.jsp'>Login</a>";
	%>  -->
	<span id="mapStatus">0,0</span>

	</div>
</td></tr>
</tbody>
</table>
<table id='lowerhalf'><tbody><tr><td style="padding:0px;">
	<canvas id='mainscreen' width="100" height="300" style="padding:0px;margin: 0px" ></canvas>
	</td><td style="padding:0px;vertical-align:top;">
	<table id='toolbar'>
	<tbody>
		<tr>
		<td style="padding:0px;vertical-align:top;">
		   <!--  Our layers -->
		   <div id="layerList"></div>
		</td>
		</tr>
		<tr>
		<td style="padding:0px;vertical-align:top;">
		<div id="paintPalette">
			<!-- Our palette -->
		</div>
		</td></tr><tr><td width=120px>
		<div id="updateStatus" class="statusBox">
			Loading...Please wait... 
		</div>
		</td></tr>

	</tbody>
	</table>
	</td>
	</tr>
</table>


<c:choose>
    <c:when test="${not empty param.realm}">
        <c:set var="initialRealm" value="${param.realm}" />
        <c:set var="realmOverride" value="true" />
    </c:when>
    <c:otherwise>
        <c:set var="initialRealm" value="1" />
        <c:set var="realmOverride" value="false" />
    </c:otherwise>

</c:choose>

<script type='text/javascript' id='realmdatascript' src='js/realmInfo.jsp?id=${initialRealm}'></script>
<script type='text/javascript' src='js/dynamic.js'></script>
<script type='text/javascript' src='js/realms.js'></script>	
<script type='text/javascript' src='js/toolbar.js'></script>	
<script type='text/javascript' src='js/ajax.js'></script>	
<script type='text/javascript' src='js/chunks.js'></script>	
<script type='text/javascript' src='js/objects.js'></script>
<script type='text/javascript' src='js/layers.js'></script>	
<script type='text/javascript' src='js/render/tiles_render.js'></script>	
<script type='text/javascript'>
/**
 * Setup all JS that is determined by the JSP.
 */
	// Initialize the user id 
	var userID = '<%= session.getId() %>';
	
	// Initialize offset from URL

    mouseX = 0;
    mouseY = 0;

    if ( sessionStorage ) {
	    mouseX = parseInt(sessionStorage.getItem("mouseX"));
	    mouseY = parseInt(sessionStorage.getItem("mouseY"));

    }



</script>
<script type='text/javascript' src='js/core.js'></script>	
<div id='realmselect'   class="popWindowSmall"><iframe width="300px" height="120px" style="border: single;border:0px;" src="ui/pickrealm.jsp"></iframe></div>
<div id='objectselect' class="popWindowSmall"><iframe width="300px" height="120px" style="border: single;border:0px;" src="ui/pickobject.jsp"></iframe></div>

<c:if test="realmOverride">
    <script type="text/javascript">

        var currentRealm = sessionStorage.getItem("currentRealm");
        if ( currentRealm ) {
            // Switch to the stored realm
            changeRealm(currentRealm);
        }
    </script>
</c:if>

</body>
</html>

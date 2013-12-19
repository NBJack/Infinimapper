<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.util.*,org.rpl.infinimapper.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Edit Object Type</title>
</head>
<link rel="stylesheet" href="default.css" type="text/css" />
<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js"></script>
<script>

var tilesetSelector, tilesetPreview, objectPreview;
var objXOff, objYOff, objWidth, objHeight;
var	currentTilesetImage;
var tilesetContext;
var mousemoveHandle;


//	Init dojo

dojo.require("dojo.io.iframe");

dojo.addOnLoad( function ()
{
	//	Grab each element by ID
	
	tilesetSelector = dojo.byId("tileset");
	tilesetDisplay  = dojo.byId("tilesetPreview");
	objectPreview   = dojo.byId("objectPreview");
	
	objXOff 	    = dojo.byId("objXOff");
	objYOff 	    = dojo.byId("objYOff");
	objWidth 	    = dojo.byId("objWidth");
	objHeight	    = dojo.byId("objHeight");
	objType			= dojo.byId("objType");
	
	//	Connect the drop-down to the tileset preview changer
	
	dojo.connect(tilesetSelector,"onchange",null,"changeTilesetPreview");
	

	//	Connect each attribute of the object to an object preview
	
	dojo.connect(objXOff,	"onchange", null, "updateAllPreviews");
	dojo.connect(objYOff,	"onchange", null, "updateAllPreviews");
	dojo.connect(objWidth,	"onchange", null, "updateAllPreviews");
	dojo.connect(objHeight,	"onchange", null, "updateAllPreviews");
	
	//	Allow picking a new offset
	
	dojo.connect(tilesetDisplay, "onmousedown", null, "pickNewOffset");
	dojo.connect(tilesetDisplay, "onmouseup", null, "removePickOffsetMouseListener");
	
	mousemoveHandle = null;
	
	//	Setup tileset preview context
	
	tilesetContext = tilesetDisplay.getContext('2d');
	
	//	Do we need to change the preview before we begin?
			
	changeTilesetPreview();
	renderObjectPreview();
});


function updateAllPreviews ()
{
	renderObjectPreview();
	renderPreview();
}


//
//	Stops listening to a mouse move
//
function removePickOffsetMouseListener ()
{
	if ( mousemoveHandle != null )
		dojo.disconnect(mousemoveHandle);
	
	mousemoveHandle = null;
}


//
//	Render the object preview 
//
function renderObjectPreview ()
{
	var		ctxt;
	var		renderImg;
	
	
	//	Grab the context
	
	ctxt = objectPreview.getContext('2d');
	
	
	//	Validate the dimensions are correct
	
	if ( objXOff.value < 0 || objYOff.value < 0 || objWidth.value < 1 || objHeight.value < 1 )
	{
		//	Note an error
		
		return;
	}
	
	
	//	Resize the canvas 
	
	objectPreview.width = objWidth.value;
	objectPreview.height= objHeight.value;	
	
	//	Clean
	
	ctxt.fillStyle = '#000000';
	ctxt.fillRect( 0, 0, objectPreview.width, objectPreview.height );	
	
	//	Draw
	
	ctxt.drawImage( currentTilesetImage, 
					objXOff.value, objYOff.value,			
					objWidth.value, objHeight.value,
					0, 0,
					objWidth.value, objHeight.value
				);
	
		
}




//
//	Change the tileset preview. We'll eventually try to use this to 
//	change the offset of the image.
//
function changeTilesetPreview () {
	
	//	Change the src of the display
	
	//tilesetDisplay.src = "FetchTiles?id=" + tilesetSelector.value;
	
	//	Change the canvas image
	
	currentTilesetImage = new Image();
	currentTilesetImage.src = "FetchTiles?ref=tileChange&id=" + tilesetSelector.value;
	
	//	When loaded, setup the render preview 
	
	dojo.connect(currentTilesetImage, "onload", null, renderPreview);
		
}


//
//	Render the preview of the object within the tileset. 
//
function renderPreview ()
{
	//	Setup width and height 

	tilesetDisplay.width = currentTilesetImage.width;
	tilesetDisplay.height = currentTilesetImage.height;

	//	Clear 
	
	tilesetContext.fillStyle = '#000000';
	tilesetContext.fillRect( 0, 0, tilesetDisplay.width, tilesetDisplay.height );	

	//	Render
	
	tilesetContext.drawImage( currentTilesetImage,
			0, 0,
			currentTilesetImage.width, currentTilesetImage.height,
			0, 0,
			currentTilesetImage.width, currentTilesetImage.height
			);

	//	Render a small box around the object

	tilesetContext.strokeStyle = "#000000";
	tilesetContext.strokeRect(
			objXOff.value-1, objYOff.value-1,			
			objWidth.value, objHeight.value
		);
	
	tilesetContext.strokeStyle = "#FFFF00";
	tilesetContext.strokeRect(
			objXOff.value, objYOff.value,			
			objWidth.value, objHeight.value
		);
}

function calcRelativeObjCoord (evt)
{
	var		cobj;
	var		x, y;
	
	
	
	//	Start at the target
	
	cobj = evt.target;
	
	x = 0;
	y = 0;
	
	//	Work up progressively
	
	while ( cobj != null )
	{
		x -= cobj.offsetLeft;
		y -= cobj.offsetTop;
		
		cobj = cobj.offsetParent;		
	}
	
	//	Store final coordinates
	
	evt.calcdX = evt.pageX + x;
	evt.calcdY = evt.pageY + y;
}



function pickNewOffset (evt)
{
	var x, y;
	
	
	calcRelativeObjCoord(evt);
	
	x = evt.calcdX;
	y = evt.calcdY;
	
	x -= tilesetDisplay.offsetLeft;
	y -= tilesetDisplay.offsetTop;

	
	//	Change the controls
	
	objXOff.value = x;
	objYOff.value = y;
	
	//	Fire-off a preview update
	
	updateAllPreviews();
	
	//	Capture mouse-clicks
	
	if ( mousemoveHandle == null )
		mousemoveHandle = dojo.connect(tilesetDisplay, "onmousemove", null, "pickNewOffset");
}



//
//	Package the object for shipment to the server.
//
function submitObject ()
{
	dojo.io.iframe.send({
		
		url : "ObjTypeManager",
		handleAs : "json",
		form : "objtypeinfo",
		
		load : function (response, ioArgs)
		{
			if ( response.formAccepted )
			{
				// 	Note the success to the user, and set the ID up we assigned it.
			
				alert("The object was successfully added: " + response);
				
			} else {
				
				alert("There were problems adding the object");
				//	Update form fields in error
				
			}
		},
		
		error : function ( response, ioArgs )
		{
			alert("Sorry, I couldn't add the object: " + response);
		}
	
		
	});

}



</script>
<body>
	<h1>Edit Object Type</h1>
  <form name="objtypeinfo" id="objtypeinfo">
  <input type="hidden" name="action" value="add">
  <table style="width:600px;margin-left:auto;margin-right:auto;" border="0">
    <col />
    <tbody>
      <tr>
        <td style="width:200px;" class="cellhead">Name </td>
        <td style="width:500px;" class="cellbody"><input type="text" name="title" style="width:200px" /></td>
      </tr>
      <tr>
        <td style="width:200px;" class="cellhead">Type </td>
        <td style="width:500px;" class="cellbody"><input type="text" name="objtype" style="width:200px" /></td>
      </tr>
      <tr>
        <td style="width:200px;" class="cellhead">Object ID </td>
        <td style="width:500px;" class="cellbody"><input type="text" readonly="readonly" name="objid" style="width:50px" value="-1" /></td>
      </tr>
      <tr>
        <td class="cellhead">Description </td>
        <td class="cellbody"><textarea rows="5" cols="40" name="desc"></textarea></td>
      </tr>
      <tr>
        <td class="cellhead">Author </td>
        <td class="cellbody"><%= session.getAttribute("username") %> </td>
      </tr>
      <tr>
        <td class="cellhead">Tile Set </td>
        <td class="cellbody">
        	<select name="tileset" id="tileset">
        		<OPTION value=""></OPTION>
        		<%
        		Collection<String[]>	tilesets;
        		
        		
        		
        		//	Grab the tilesets en-masse (will not always be able to do this!)
        		
        		tilesets = WorldDB.getAllTilesets();
        		
        		for ( String [] s : tilesets )
        		{
        		%> <OPTION value="<%=s[1]%>"><%=s[0]%></OPTION> <%
        		}
        		
        		%>        		
        	</select>
        </td>
      </tr>
      <tr>
        <td class="cellhead">Dimensions</td>
        <td class="cellbody"><input size="5" name="objWidth" id="objWidth" value="32"> x <input size="5" name="objHeight" id="objHeight" value="32"></td>
      </tr>
      <tr>
        <td class="cellhead">Image Offset</td>
        <td class="cellbody"><input size="5" name="objXOff" id="objXOff" value="0"> x <input size="5" name="objYOff" id="objYOff" value="0"></td>
      </tr>
      <tr>
        <td class="cellhead">Public </td>
        <td class="cellbody"><input type="checkbox" name="publicflag" /></td>
      </tr>
      <tr>
        <td colspan="2">
	  <p></p>
          <button type="button" onclick="submitObject();return false;">Save Type</button> </td>
      </tr>
    </tbody>
  </table>
  <table style="margin-left:auto;margin-right:auto;"><tbody><tr><td>
  <canvas  id="objectPreview"></canvas><br>
  </td></tr><tr><td>
  <canvas  id="tilesetPreview"></canvas>
  </td></tr>
  </tbody></table>
</form>	
</body>
</html>
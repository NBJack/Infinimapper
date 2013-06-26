<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dijit/themes/claro/claro.css" />
<style type="text/css">
    @import "http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojox/grid/resources/claroGrid.css";
</style>
<link href="corestyle.css" rel="stylesheet" type="text/css" />
<%

int		objectID;



//	Determine the initial object value

objectID = -1;

if ( request.getParameter("objid") != null )
	objectID = Integer.parseInt(request.getParameter("objid"));


%>
<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojo/dojo.xd.js" djConfig="parseOnLoad: true"></script>
<script>
	dojo.require("dijit.form.Form");
	dojo.require("dijit.form.Button"); 
	dojo.require("dijit.form.SimpleTextarea");
	dojo.require("dojo.io.iframe");
    dojo.require("dojox.grid.DataGrid");
    dojo.require("dojo.data.ItemFileWriteStore");

	var objLoadInProgress = false;
	var objSaveInProgress = false;
	var fakeIdCounter = 1;
	
	
	function loadObjectInstance (objId)
	{
		// Do nothing if we have something pending
		
		if ( objLoadInProgress )
			return;
		
		objLoadInProgress = true;
		
		//	Request the object asynchronously
		
		dojo.io.iframe.send({
			
			url : "ObjInstManager",
			handleAs : "json",
			method: "post",
			content: {
				action: "retrieve",
				objid: objId
			},
			
			load: function(response, ioArgs) {
				
				//	Apply to the form, but extract the attributes for seperate processing.
				var attribData = response["dataBody"];
				response["dataBody"] = null;
				// Clear out the existing table
				fakeIdCounter = 0;
				objectProperties.items = [];
				propertyStore = new dojo.data.ItemFileWriteStore({data: objectProperties});
				propertyGrid.setStore(propertyStore);
				
				// Try parsing the attribute payload.
				try {
					var parsedAttribs = eval(attribData);
					if ( parsedAttribs instanceof Array ) {
						for ( entryNum in parsedAttribs ) {
							var entry = parsedAttribs[entryNum];
							propertyStore.newItem({name: entry.name, value: entry.value, id: fakeIdCounter++});
						}
					} else {
						throw "Not an array";
					}
					
					// Add each as a separate entry in the grid.
					
				} catch (ex) {
					// Evaluation won't always work. If that fails, log it and fallback.
					console.log("Bad JSON: " + attribData);
				}
				
				// Post the remainder in the form directly
				objForm.set("value", response);
				// Note that we're done
				objLoadInProgress = false;
			},
			
			error: function(response, ioArgs) {
				
				//	Report the problem
				
				alert("Error retrieving object data");
				
				objLoadInProgress = false;
			}
							
		});			

	}
	
	function saveObjectInstance ()
	{
		// Send the object's updated data back to the server
		
		if ( objSaveInProgress ) {
			alert("Please wait while the previous save completes.");
		}
		
		objSaveInProgress = true;

		// Build the result object
		var pojoAttributes = dojo.toJson(getAllProperties());
		
		dojo.io.iframe.send({
			// Since this is under /ui when called, refer to the head
			url : "../ObjInstManager",
			method: "post",
			handleAs : "text",
			
			content: {
				action: "update",
				dataID: dojo.byId("dataID").value,
				dataBody: pojoAttributes
			},
			
			load: function(response, ioArgs) {
				
				objSaveInProgress = false;
			},
			
			error: function(response, ioArgs) {
				
				//	Report the problem
				
				alert("Error saving object data:" + response);
				
				objSaveInProgress = false;
			}
		});
	}
	
	var objectProperties = {
		identifier: "id",
		label: "name",
		items: [
		{ id: "-1", name: ".", value:"." }
		]
	};
	
	// Clear out a datastore of all items
	function clearStore(store) {
		for ( item in store.items ) {
			store.deleteItem(store.items[item].id);
		}
	}
	
	// Transform the property data store into a cleaned-up JSON object.
	function getAllProperties() {
		var builtList = [];
		for ( entryNumber in objectProperties.items ) 
		{
			var entry = objectProperties.items[entryNumber]
			var filteredEntry = {};
			for ( attributeName in entry)
			{
				// No IDs, no underscores allowed.
				if ( attributeName.charAt(0) != "_" && attributeName != "id" ) {
					console.log(attributeName);
					filteredEntry[attributeName] = entry[attributeName][0];
				}
			}
			builtList.push(filteredEntry);
		}
		
		return builtList;
	}
	
	function addAttributeRow() {
		propertyStore.newItem({id: fakeIdCounter++});
	}
	

</script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Object Editor</title>
</head>
<body>
<div dojoType="dijit.form.Form" id="objForm" jsId = "objForm" encType="multipart/form-data" action="">
ID: <input type="text" dojoType="dijit.form.TextBox" name="dataID" id="dataID" style="width:5em;" readonly="readonly" value="<%= objectID %>"><br>
Type: <input type="text" dojoType="dijit.form.TextBox" name="dataType" id="dataType" style="width:10em;" readonly="readonly" value="<retrieving>"><br>
X: <input type="text" dojoType="dijit.form.TextBox" name="dataXCoord" id="dataXCoord" style="width:4em;" readonly="readonly" value="<retrieving>"> Y: <input type="text" dojoType="dijit.form.TextBox" name="dataYCoord" id="dataYCoord"" style="width:4em;" readonly="readonly" value="<retrieving>"><br>
Data<br>
<!-- <textarea id="dataBody" name="dataBody" dojoType="dijit.form.SimpleTextarea" rows="10" cols="30"></textarea>  -->
<!-- Table properties -->
<div data-dojo-type="dojo.data.ItemFileWriteStore" data-dojo-props="data:objectProperties" data-dojo-id="propertyStore"></div>
<button type="button" onclick="addAttributeRow(); return false;">Add Row</button>
<table dojoType="dojox.grid.DataGrid" jsId="propertyGrid" store="propertyStore" query="{ name: '*' }"
rowsPerPage="20" clientSort="true" style=" height: 300px;"
rowSelector="20px">
    <thead>
        <tr>
        	<th width="50px" field="name" editable="true">Name</th>
        	<th width="80%" field="value" editable="true">Value</th>
        </tr>
    </thead>
</table>        
<br>

<button type="button" onclick="saveObjectInstance(); return false;">Save</button>
</div>

</body>
</html>
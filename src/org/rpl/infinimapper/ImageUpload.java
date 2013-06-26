package org.rpl.infinimapper;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.rpl.infinimapper.security.AuthMan;

import java.sql.*;
import java.util.List;



/**
 * 
 * Handles requests to upload images to the database for use as tilesets. The basic logic
 * performs a few validation checks on the input (ex. size limitations), but more advanced
 * checks such as ensuring images are PNG files are not performed at this time.
 * 
 * The final version of the module should ensure that the image sets cannot be poisoned,
 * that all data provided is valid for database consumption, and ensure that the user
 * performing the operation has the appropriate rights and quota space left.
 * 
 * 
 * 
 * Servlet implementation class ImageUpload
 */
public class ImageUpload extends HttpServlet 
{
	private static final long serialVersionUID = 1L;

	private static final int  MAX_IMAGE_SIZE = 1024 * 1024 * 2;
	
	static final String	DB_IMG_UPLOAD = "INSERT INTO tilelib (name, imagedata, tilecount,tilewidth,description, fullwidth, fullheight) VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageUpload() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String				imgName;
		String				imgDescription;
		int					tileWidth;
		int					tileCount;
		Connection			conn;
		PreparedStatement	prep;
		DiskFileItemFactory		fileFactory;
		ServletFileUpload	upldHandler;
		List<FileItem>		reqList;
		File				tempTileFile;	//	Stores our image data on disk
		FileInputStream		fileIn;			//	Deals with streaming file data to the prepared statement.
		byte []				rawBytes;
		Dimension			imgDim;
		Integer				userid;
		
		
		
		//	Initialize our variables
		
		conn 		= null;
		prep 		= null;
		imgName		= null;
		imgDescription = null;
		tileWidth	= -1;
		tileCount 	= 1;
		tempTileFile	= null;
		rawBytes	= null;
		
		
		//	First question: Is the user permitted?
		
		userid = -1;
		
		if ( request.getSession().getAttribute("userid") != null )
		{
			//	Get the user ID
			
			userid = (Integer) request.getSession().getAttribute("userid");			
			
		} else {
			
			//	No ID available
			
			userid = null;
		}
		
		//	Check for a valid user ID and check for the right to add images
		
		if ( userid == null || !AuthMan.doesUserHaveRight(userid, AuthMan.Rights.AddImages) )
		{
			response.sendError(401, "You are not authorized to add images.");			
			response.flushBuffer();
			
			return;
		}
		
		
		
		System.out.println("New request.");
		
		
		try {

			//	Parse the multi-part form data and prep it for use
			
			fileFactory = new DiskFileItemFactory();
			fileFactory.setSizeThreshold(MAX_IMAGE_SIZE);
			
			upldHandler = new ServletFileUpload(fileFactory);
			upldHandler.setFileSizeMax(MAX_IMAGE_SIZE);			
			
			reqList = upldHandler.parseRequest(request);

			System.out.println("Request parsed.");
			
			
			//	Double-check the attributes we need from the server
	
			for ( FileItem fi : reqList )
			{
				System.out.println("File item: " + fi.getFieldName() + " (" + fi.getName() + ")");
				
				if ( fi.isFormField() )
				{
					//	Field processing
					
					if ( fi.getFieldName().equals("title") )
						imgName = fi.getString();
					
					if ( fi.getFieldName().equals("desc") )
						imgDescription = fi.getString();
					
					if ( fi.getFieldName().equals("tilewidth") )
						tileWidth = Integer.parseInt(fi.getString());
					
				} else {
					
					//	File processing
					
					if ( fi.getFieldName().equals("tilesimage") )
					{
						//	Write the file to disk; we can read it in via the prepared statement
						//	when ready.
						
						//tempTileFile = writeStreamContentToDisk(fi.getInputStream());
						rawBytes = fi.get();
						
						System.out.println("Raw bytes: " + rawBytes.length); 
						
						
					}
				}
			}
			
			System.out.println("All parameters read.");
			
			//System.out.println("Temp file: " + tempTileFile.getName());
			
			//	Is the image OK?
				
			imgDim = validateImageAndFindDimensions(rawBytes);
			
			//	Is everything we need available?
			
			if ( imgName == null 
			|| 	tileWidth == -1 
			||	tileCount == -1
			|| 	rawBytes == null )
				throw new Exception("Insufficient parameters were provided. (" + imgName + ", " + tileWidth + "," + tileCount + "," + tempTileFile + ")");
						
			System.out.println("Parameters needed were found.");
						
			//	We'll need a connection 
	
			conn = DBResourceManager.getConnection();
			
			prep = conn.prepareStatement(DB_IMG_UPLOAD);			
			
			//	Prepare the statement
			
			prep.setString( 1, 	imgName);
			prep.setBytes(2, 	rawBytes);
			prep.setInt( 3,		tileCount);
			prep.setInt( 4, 	tileWidth);
			prep.setString( 5, 	imgDescription);
			prep.setInt( 6, imgDim.width);
			prep.setInt( 7, imgDim.height);
			//	Execute
			
			prep.execute();
			
			
			System.out.println("Images uploaded!");
						
			
			//	Close any active streams
			
			//fileIn.close();
			
			response.getWriter().println("Image was uploaded successfully.");
			
		} catch ( FileUploadException fEx )
		{
			fEx.printStackTrace();
			response.sendError(200, "The image was not uploaded successfully: " + fEx.toString());
			
		} catch ( SQLException sqEx )
		{
			sqEx.printStackTrace();
			response.sendError(200, "The image was not uploaded successfully: " + sqEx.toString() );
			
		} catch ( Exception ex )
		{
			ex.printStackTrace();			
			response.sendError(200, "The image was not uploaded successfully: " + ex.toString());		
		}
		
		//
		//	Clean up resources safely; ignore any problems beyond this point.
		//
		
		try{
			if ( prep != null )
				prep.close();
		} catch ( SQLException sqex ) {};
		
		try{
			if ( conn != null )
				conn.close();
		} catch ( SQLException sqex ) {};
		
		try {
			if ( tempTileFile != null )
				tempTileFile.delete();
		} catch (Exception ex) {}			
	}

	
	
	
	/**
	 * Write-out a stream to disk.
	 * 
	 * @param is
	 * @return
	 */
	private static File writeStreamContentToDisk( InputStream is ) 
	{
		FileOutputStream	fOut;
		byte []				buffer;
		File				tempFile;
		int					size;
		
		
		

		tempFile = null;
		buffer = new byte[8096];
		
		try {
			
			//	Construct a new temp file
			
			tempFile = File.createTempFile("img", ".png");
			
			fOut = new FileOutputStream(tempFile);
			
			//	Write the content of the stream out
		
			System.out.println("Begin reading file...");
			
			while ( (size = is.read(buffer)) > 0 )
			{
				System.out.println(size);
				
				fOut.write(buffer, 0, size);
			}
			
			//	Flush and close the output buffer. Leave the original input stream untouched.
			
			fOut.flush();
			fOut.close();
			
			System.out.println("File written to disk!");
			
		} catch ( IOException ioex )
		{
			ioex.printStackTrace();
			
			//	Clean-up
			
			if ( tempFile != null )
				tempFile.delete();
			
			return null;
		}
		
		
		return tempFile;
	}
	
	
	/**
	 * Determines the dimensions of an image on disk.
	 * 
	 * @param imgLoc
	 * @return
	 * @throws IOException
	 */
	Dimension validateImageAndFindDimensions ( byte [] rawData ) throws IOException
	{
		Dimension		result;
		BufferedImage	bImg;
		ByteArrayInputStream	bArr;
		
		
		
		//	Create the byte buffer
		
		bArr = new ByteArrayInputStream(rawData);
		
		//	Read the image
		
		bImg = ImageIO.read(bArr);			
		
		//	TODO: Validate this is a PNG file.
		
		//	Calculate the dimensions
		
		result = new Dimension(bImg.getWidth(), bImg.getHeight());
		
		//	Cleanup and try to help the GC 
		
		bArr.close();
		bArr = null;
		bImg = null;
		
		//	Return the result
		
		return result;
	}
}

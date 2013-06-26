package org.rpl.infinimapper;

/**
 * Enumerates all known map file formats.
 * 
 * @author rplayfield
 *
 */
public enum MapDataType {
	
	/**
	 * TMX file format, CSV output
	 */
	TMX_PLAIN(".tmx"),
	
	/**
	 * TMX file format, Base64 output
	 */
	TMX_BASE64(".tmx"),
	
	/**
	 * XML File, with data written as chunks.
	 */
	XML_CHUNKED(".chxml"),
	
	/**
	 * XML File, with data as a contiguous map
	 */
	XML_MONOLITHIC(".mxml"),
	
	/**
	 * TXT File, with data as a contiguous map
	 */
	TXT_MONOLITHIC(".maptxt"),
	
	/**
	 * TXT File, with data as a series of chunks (basically one per line)
	 */
	TXT_CHUNKED(".chktxt"),
	
	/**
	 * Binary file, proprietary (but publicly published) format. Good for smaller
	 * sizes and faster reading. 
	 */
	RAW_BINARY(".mapbin"),
	
	
	/**
	 * Binary file, but much more compact and more complex to read. Better for
	 * embedded devices or other target platforms.
	 */
	RAW_EFFICIENT_BINARY(".mcbin");
	
	
	
	String		ext;
	
	
	MapDataType(String extension)
	{
		this.ext = extension;
	}
	
	
	
	/**
	 * Retrieve the file extension that should be used.
	 * @return
	 */
	public String getExt ()
	{
		return ext;
	}
	
}

package org.rpl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * Simplifies writing raw data in Base64 to the output stream.
 * 
 * @author rplayfield
 * 
 */
public class Base64IntWriter extends OutputStream {

	final static int LAST_BYTE = 0x000000FF;

	/**
	 * The stream we write our encoded data to.
	 */
	OutputStream targetStream;

	long intCount = 0;

	/**
	 * Where in our byte scratchpad we are.
	 */
	int scratchIndex = 0;

	/**
	 * The byte scratch pad we use to buffer data until we're ready to properly
	 * encode it via Base64.
	 */
	byte[] byteScratch = new byte[12000]; // Buffer size must be some multiple
											// of 12

	/**
	 * Record a byte into our hard-coded buffer.
	 * 
	 * @param b
	 * @return
	 */
	private boolean recordByte(byte b) {
		byteScratch[scratchIndex] = b;
		scratchIndex++;

		return (scratchIndex >= byteScratch.length);
	}

	/**
	 * Carefully flush out the buffer to the target stream.
	 * 
	 * @throws IOException
	 */
	private void flushBuffer() throws IOException {

		// com.sun.xml.internal.messaging.saaj.util.Base64.encode(byteScratch);
		// com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(byteScratch);
		// com.sun.org.apache.xml.internal.security.utils.Base64.en

		if (scratchIndex == byteScratch.length)
			targetStream.write(Base64.encodeBase64(byteScratch, false));
		else
			targetStream.write(Base64.encodeBase64(Arrays.copyOf(byteScratch, scratchIndex), false));

		// Reset the index

		scratchIndex = 0;
	}

	/**
	 * Construct a new Base64 integer writer, with or without compression of the
	 * stream.
	 * 
	 * @param rawOut
	 *            The stream to write our results to.
	 * @param useGZip
	 *            Whether or not to use GZip on the results.
	 * @throws IOException
	 *             Thrown when an error occurs during the writing process.
	 */
	public Base64IntWriter(final OutputStream rawOut) throws IOException {
		targetStream = rawOut;
	}

	/**
	 * Write a single integer to the output stream target specified, using the
	 * same technique as XMLMapWriter.java of the original Tiled source code.
	 * See:
	 * http://tiled.hg.sourceforge.net/hgweb/tiled/tiled/file/4259f26e18b7/src
	 * /tiled/io/xml/XMLMapWriter.java However, for memory purposes, we will
	 * rely on streaming to reduce memory requirements.
	 * 
	 * @param i
	 * @throws IOException
	 */
	public static void writeInt(final int i, OutputStream target) throws IOException {
		target.write(i & LAST_BYTE);
		target.write(i >> 8 & LAST_BYTE);
		target.write(i >> 16 & LAST_BYTE);
		target.write(i >> 24 & LAST_BYTE);
	}

	@Override
	public void close() throws IOException {

		finish();
	};

	@Override
	public void write(int b) throws IOException {
		if (recordByte((byte) b))
			flushBuffer();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		for (int i = off; i < (off + len); i++)
			write(b[i]);

	}

	@Override
	public void flush() throws IOException {

		flushBuffer();
	}

	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
	}

	/**
	 * Finish and close the stream.
	 * 
	 * @throws IOException
	 */
	public void finish() throws IOException {
		// System.out.println("Number of INTs written: " + intCount);

		flushBuffer();

		targetStream.flush();

	}
}

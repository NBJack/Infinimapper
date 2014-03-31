package org.rpl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtils {

	private static final String TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
	private static final File TMP_DIR = new File(TMP_DIR_PATH);

	/**
	 * Writes contents of a file to the stream using a simple buffer.
	 * 
	 * @param file
	 * @param stream
	 * @throws IOException
	 */
	public static void dumpFileToStream(File file, OutputStream stream) throws IOException {
		byte[] buffer = new byte[8096];
		int bytesRead;
		FileInputStream fileIn = new FileInputStream(file);
		while ((bytesRead = fileIn.read(buffer)) > 0) {
			stream.write(buffer, 0, bytesRead);
		}
		stream.flush();
	}

    /**
     * Return a new temporary directory location. Does not actually create the
     * directory. Uses the provided directory as the root.
     * @param dir
     * @return
     */
    public static File generateTempDir ( File dir ) {
        return new File(dir, System.currentTimeMillis() + "_" + Math.random());
    }

	/**
	 * Return a new temporary directory location. Does not actually create the
	 * directory. Uses the system default as the root.
	 * 
	 * @return
	 */
	public static File generateTempDirLocation() {
		return generateTempDir(TMP_DIR);
	}

}

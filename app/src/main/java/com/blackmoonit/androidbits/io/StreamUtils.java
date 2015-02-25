package com.blackmoonit.androidbits.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {

	/**
	 * Reads the contents of the InputStream into a string and returns it.
	 * @param aStream - the Input stream.
	 * @return Returns the string containing the contents of the input stream.
	 * @throws java.io.IOException
	 */
	public static String inputStreamToString(InputStream aStream) throws IOException {
		StringBuilder theBuilder = new StringBuilder();
		BufferedReader theReader = new BufferedReader(new InputStreamReader(aStream));
		try {
			String theLine = null;
			while ((theLine = theReader.readLine()) != null) {
				theBuilder.append(theLine).append("\n");
			}
		} finally {
			theReader.close();
		}
		return theBuilder.toString();
	}

}

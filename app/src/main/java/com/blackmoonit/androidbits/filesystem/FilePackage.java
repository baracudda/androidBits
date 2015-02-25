package com.blackmoonit.androidbits.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Base class for archiving files.
 *
 * @author Ryan Fischbach
 */
abstract public class FilePackage {
	public File mPackageFile = null;
	public String mBasePath = null;
	public ProgressBarHandler mMsgHandler = null;
	public Object mProgressID = null;

	public FilePackage(File aPackageFile) {
		mPackageFile = aPackageFile;
		mBasePath = aPackageFile.getParent();
		if (!mBasePath.endsWith(File.separator))
			mBasePath = mBasePath+File.separator;
	}

	public FilePackage(String aPackageFilePath) {
		this(new File(aPackageFilePath));
	}

	/**
	 * Pack up a list of files into an archive file.
	 *
	 * @param anIterator - Uri/File/String list iterator
	 * @throws java.io.IOException When working with files, this is always possible
	 */
	abstract public void pack(Iterator<?> anIterator) throws IOException;

	/**
	 * Unpack an archive file.
	 *
	 * @param aDestPath - complete path to where the unpacked files will go
	 * @throws java.io.IOException When working with files, this is always possible
	 */
	abstract public void unpack(String aDestPath) throws IOException;

}

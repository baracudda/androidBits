package com.blackmoonit.androidbits.filesystem;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Base class for archiving files.
 *
 * @author baracudda
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

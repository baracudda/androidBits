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
import java.io.Serializable;
import java.util.Locale;

/**
 * File adapters need some more meta file information to properly and quickly
 * sort and work with files on the local file system.
 *
 * @author baracudda
 */
public class FileListAdapterElement extends File implements Serializable {
	static private final long serialVersionUID = 5104958455676411709L;

	/**
	 * Is the file marked by the app?
	 */
	protected boolean bMarked = false;

	/**
	 * Does the folder containing this file contain marked files?
	 */
	protected boolean bFolderContainsMarked = false;

	/**
	 * Is the file represented here just a link to another, real, path?
	 */
	protected boolean bFileIsJumpPoint = false;

	//comparitor speed optimizations, pre-calc certain frequently accessed items
	public final String mCompName;
	public final String mParentPath;
	public final String mNameOnly;
	public final String mExtPart;
	//public final String mViewDesc; //content description for Accessibility services (not read in TextViews!)
	public final Long mLastModified;
	public final Long mSize;
	public final boolean bIsFile;

	//display adapter pre-calc optimizations
	public final String mExternalStorageRelativePath;

	/**
	 * Constructor taking a string directory path of which files to load.
	 * @param aPath - filesystem path to obtain file list.
	 */
	public FileListAdapterElement(String aPath) {
		super(aPath);
		mCompName = getName().toLowerCase(Locale.getDefault());
		mParentPath = getParent();
		mNameOnly = BitsFileUtils.replaceExtension(mCompName,"");
		mExtPart = BitsFileUtils.getExtension(this);
		mLastModified = this.lastModified();
		mSize = this.length();
		bIsFile = isFile();
		bFileIsJumpPoint = BitsFileUtils.isFileJumpPoint(this);
		mExternalStorageRelativePath = BitsFileUtils.getParentPathRelativeToExternalStorage(this);
		//String thePronouncableExt = (mExtPart!=null) ? mExtPart.toUpperCase().replace("", " ").trim() : "";
		//mViewDesc = BitsFileUtils.replaceExtension(getName(),thePronouncableExt);
	}

	/**
	 * May wish to tweak the filename for display purposes.
	 * @return Returns this.getName() except for special cases.
	 */
	public String getDisplayName() {
		return getName();
	}

	/**
	 * Gets the string displayed on the screen for a given File object.
	 *
	 * @param aFile - file object
	 * @return Return the string used for display purposes.
	 */
	static public String getDisplayName(File aFile) {
		if (aFile!=null) {
			//if (aFile.isDirectory())
			//	return "["+aFile.getName()+"]";
			//else
				return aFile.getName();
		} else {
			return null;
		}
	}

	public boolean isFileJumpPoint() {
		return bFileIsJumpPoint;
	}

	public boolean isMarked() {
		return bMarked;
	}

	public void setMark(boolean aValue) {
		bMarked = aValue;
	}

	public boolean containsMarked() {
		return (bFolderContainsMarked);
	}

	public void setMarkings(FileOrchard aMarkedFiles, File aFile) {
		bMarked = aMarkedFiles.contains(aFile);
		if (!bMarked)
			bFolderContainsMarked = aMarkedFiles.containsTreeOrBranch(aFile);
	}

	public void resetMarks() {
		bMarked = false;
		bFolderContainsMarked = false;
	}
}

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

import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * FileOrchard is a managed set of files usually arranged as a bunch of ordered trees.
 * Thread-safe. Map within a map, outer map is the parent path, inner map is (filename, file object).
 *
 * @author Ryan Fischbach
 */
public class FileOrchard {
	//bundle/unbundle keys
	private static final String SAVE_ORCHARD_SIZE = "orchard_size";
    private static final String SAVE_ORCHARD_TREE = "orchard_tree_";
    private static final String SAVE_ORCHARD_BRANCHES = "orchard_branches_";

	private HashMap<String, HashMap<String, File>> mFileOrchard = null;
	private boolean mSingleFolderLimit = false;
	private boolean mEnsureContentsExist = true;

	public FileOrchard() {
		mFileOrchard = new HashMap<String, HashMap<String, File>>();
	}

    public boolean isEmpty() {
    	return mFileOrchard.isEmpty();
    }

    public void clear() {
    	mFileOrchard.clear();
    }

    public int size() {
    	return mFileOrchard.size();
    }

	public boolean isSingleFolderLimit() {
		return mSingleFolderLimit;
	}

	public void setSingleFolderLimit(boolean singleFolderLimit) {
		mSingleFolderLimit = singleFolderLimit;
	}

	public boolean isEnsureContentsExist() {
		return mEnsureContentsExist;
	}

	public void setEnsureContentsExist(boolean ensureContentsExist) {
		mEnsureContentsExist = ensureContentsExist;
	}

	private String NameToMapKey(String aFolderName) {
		return (aFolderName!=null && !aFolderName.equals("")) ? aFolderName : File.separator;
	}

	/**
	 * Add file to orchard, return true if added.
	 * @param aFolderName - folder path (parent)
	 * @param aFileName - file name (or subfolder name)
	 * @return Returns true if added, else false. False can occur when file does not exist and
	 * we are checking for existance, or if the file is already in the orchard.
	 * @see #addFile(java.io.File)
	 */
	public boolean addFile(String aFolderName, String aFileName) {
		String theFolder = (aFolderName!=null && !aFolderName.equals(File.separator)) ? aFolderName : "";
		return addFile(new File(theFolder,aFileName));
	}

	/**
	 * Add file object to the orchard, return true if added.
	 * @param aFile - file/folder object
	 * @return Returns true if added, else false. False can occur when file does not exist and
	 * we are checking for existance, or if the file is already in the orchard.
	 */
	public boolean addFile(File aFile) {
		synchronized (mFileOrchard) {
			if (aFile!=null && (!isEnsureContentsExist() || aFile.exists())) {
				HashMap<String, File> theFileList = mFileOrchard.get(NameToMapKey(aFile.getParent()));
				if (theFileList!=null) {
					if (!theFileList.containsKey(aFile.getName())) {
						theFileList.put(aFile.getName(),aFile);
						return true;
					}
				} else {
					if (isSingleFolderLimit()) {
						//ensure we only have one folder key in Map
						mFileOrchard.clear();
					}
					theFileList = new HashMap<String, File>();
					theFileList.put(aFile.getName(),aFile);
					mFileOrchard.put(NameToMapKey(aFile.getParent()),theFileList);
					return true;
				}
			} else {
				removeFile(aFile);
			}
		}
		return false;
	}

	public void removeFile(File aFile) {
		if (aFile!=null) {
			String theFileTree = NameToMapKey(aFile.getParent());
			synchronized (mFileOrchard) {
				HashMap<String, File> theFileList = mFileOrchard.get(theFileTree);
				if (theFileList!=null) {
					theFileList.remove(aFile.getName());
					if (theFileList.size()==0) {
						mFileOrchard.remove(theFileTree);
					}
				}
			}
		}
	}

	/**
	 *
	 * @return Returns the size of folder map. i.e. # of trees in the orchard.
	 */
	public int sizeofOrchard() {
		return mFileOrchard.size();
	}

	/**
	 *
	 * @param aFolderName - folder tree to check
	 * @return Returns the number of files listed for the given folder.
	 */
	public int sizeof(String aFolderName) {
		HashMap<String, File> theFiles = mFileOrchard.get(NameToMapKey(aFolderName));
		if (theFiles!=null) {
			return theFiles.size();
		} else {
			return 0;
		}
	}

	/**
	 *
	 * @return Returns the number of files listed in the entire orchard.
	 */
	public int sizeofAll() {
		int theSize = 0;
		synchronized (mFileOrchard) {
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				String theFolderName = (String)folderNames.next();
				theSize += mFileOrchard.get(theFolderName).size();
			}
		}
		return theSize;
	}

	public void removeFolder(String aFolderName) {
		synchronized (mFileOrchard) {
			mFileOrchard.remove(NameToMapKey(aFolderName));
		}
	}

	public boolean contains(File aFile) {
		String aMapKey = NameToMapKey(aFile.getParent());
		HashMap<String, File> theFiles = mFileOrchard.get(NameToMapKey(aMapKey));
		if (theFiles!=null) {
			return theFiles.containsKey(aFile.getName());
		} else {
			return false;
		}
	}

	/**
	 * Check contents for exact folder path name.
	 *
	 * @param aFolderName - folder path
	 * @return Returns true if the exact folder path is found.
	 */
	public boolean containsTree(String aFolderName) {
		String aMapKey = NameToMapKey(aFolderName);
		return mFileOrchard.containsKey(aMapKey);
	}

	/**
	 * Check contents for file/folder and also for any subfolder.
	 *
	 * @param aFile - file or folder
	 * @return Returns true if the file is contained, if any subfile is contained, or parent folder
	 * that contains aFile is contained.
	 */
	public boolean containsTreeOrBranch(File aFile) {
		if (aFile!=null && size()>0) {
			String theSearchFilePath = aFile.getPath();
			synchronized (mFileOrchard) {
				Iterator<String> folderNames = mFileOrchard.keySet().iterator();
				while (folderNames.hasNext()) {
					String theFolderName = (String)folderNames.next();
					//if key starts with aFile, then subitem is contained
					if (aFile.isDirectory() && theFolderName.startsWith(theSearchFilePath))
						return true;
					//now check to see if a parent of aFile is contained in the key
					if (theSearchFilePath.startsWith(theFolderName)) {
						int idx = theSearchFilePath.indexOf(File.separator,theFolderName.length()+1);
						if (idx>-1) { //if idx>-1 then theSearchFilePath is in subfolder of current key
							if (mFileOrchard.get(theFolderName).containsKey(theSearchFilePath.substring(0,idx))) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public interface OnEachFile
	{
		/**
		 * Process the given file.
		 *
		 * @param aFile - file contained in the FileOrchard.
		 * @return Return FALSE if finished processing the FileOrchard, else return TRUE in
		 * order to keep processing more files from the mapping.
		 */
	    public boolean process(File aFile);
	}

	public void foreach(OnEachFile onEachFile) {
		if (onEachFile!=null) synchronized (mFileOrchard) {
			boolean bCancelled = false;
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				String theFolderName = (String)folderNames.next();
				HashMap<String, File> theFileList = mFileOrchard.get(theFolderName);
				Iterator<File> theFiles = theFileList.values().iterator();
				while (theFiles.hasNext()) {
					File theFile = (File)theFiles.next();
					if (!onEachFile.process(theFile)) {
						bCancelled = true;
						break;
					}
					if (!Thread.currentThread().getName().equals("main"))
						Thread.yield();
				}//while
				if (bCancelled) {
					break;
				}
			}
		}
	}

	public void foreachTree(OnEachFile onEachFile) {
		if (onEachFile!=null) synchronized (mFileOrchard) {
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				File theFolderFile = new File((String)folderNames.next());
				if (!onEachFile.process(theFolderFile)) {
					break;
				}
				if (!Thread.currentThread().getName().equals("main"))
					Thread.yield();
			}//while
		}
	}

	/**
	 * Generates the deepest common folder path.
	 *
	 * @return Returns the deepest common folder path for the list of contained paths.
	 */
	public String getSingleTree() {
		String theResult = null;
		if (mSingleFolderLimit) synchronized (mFileOrchard) {
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				theResult = (String)folderNames.next();
			}//while
		} else synchronized (mFileOrchard) {
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			String s;
			while (folderNames.hasNext()) {
				s = (String)folderNames.next();
				if (theResult!=null) {
					try {
						while (!s.startsWith(theResult)) {
							theResult = theResult.substring(0,theResult.lastIndexOf(File.separator));
						}
					} catch (Exception e) {
						return File.separator;
					}
				} else {
					theResult = s;
				}
			}//while
		}
		return theResult;
	}

    public Bundle toBundle() {
    	Bundle theBundle = new Bundle();
		if (!mFileOrchard.isEmpty()) synchronized (mFileOrchard) {
			theBundle.putInt(SAVE_ORCHARD_SIZE, mFileOrchard.size());
			int i = 0;
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				i += 1;
				String theFolderName = (String)folderNames.next();
				theBundle.putString(SAVE_ORCHARD_TREE+i,theFolderName);
				HashMap<String, File> theFileList = mFileOrchard.get(theFolderName);
				ArrayList<String> theFilenameList = new ArrayList<String>(theFileList.size());
				/*
				Iterator<File> theFiles = theFileList.values().iterator();
				while (theFiles.hasNext()) {
					File theFile = (File)theFiles.next();
					theFilenameList.add(theFile.getName());
				}//while
				*/
				theFileList.keySet().addAll(theFilenameList);
				theBundle.putStringArrayList(SAVE_ORCHARD_BRANCHES+i,theFilenameList);
			}//while
		}
		return theBundle;
    }

    public void fromBundle(Bundle aBundle) {
    	if (aBundle!=null) {
    		int theSize = aBundle.getInt(SAVE_ORCHARD_SIZE,0);
    		for (int i=1; i<=theSize; i++) {
    			String theFolderName = aBundle.getString(SAVE_ORCHARD_TREE+i);
    			ArrayList<String> theFilenameList = aBundle.getStringArrayList(SAVE_ORCHARD_BRANCHES+i);
    			ListIterator<String> theFilenames = theFilenameList.listIterator();
    			while (theFilenames.hasNext()) {
    				String theFilename = (String)theFilenames.next();
    				addFile(theFolderName,theFilename);
    			}//while
    		}
    	}
    }

	public ArrayList<Uri> toUriList(boolean bFilesOnly) {
		if (!isEmpty()) synchronized (mFileOrchard) {
			ArrayList<Uri> theUriList = new ArrayList<Uri>();
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				String theFolderName = (String)folderNames.next();
				HashMap<String, File> theFileList = mFileOrchard.get(theFolderName);
				Iterator<File> theFiles = theFileList.values().iterator();
				while (theFiles.hasNext()) {
					File theFile = (File)theFiles.next();
					if (!bFilesOnly || theFile.isFile())
						theUriList.add(Uri.fromFile(theFile));
				}//while filelist
			}//while folderlist
			return theUriList;
		} else {
			return null;
		}
	}

	/**
	 * Flatten structure down to first File found.  Used mainly when a single file is being picked.
	 * @return Returns first File found.
	 */
	public File getFirstFile() {
		if (!isEmpty()) synchronized (mFileOrchard) {
			Iterator<String> folderNames = mFileOrchard.keySet().iterator();
			while (folderNames.hasNext()) {
				String theFolderName = (String)folderNames.next();
				HashMap<String, File> theFileList = mFileOrchard.get(theFolderName);
				Iterator<File> theFiles = theFileList.values().iterator();
				while (theFiles.hasNext()) {
					return (File)theFiles.next();
				}//while filelist
			}//while folderlist
		}
		return null;
	}

	/**
	 * Gets a list of the files contained in this orchard.
	 * This list is then filtered through a FileFilter and matching files are returned
	 * as an array of files. If filter is null then all files match.
	 *
	 * @param aFilter - the filter to match names against, may be null
	 * @return Returns an array of files or null.
	 */
	public File[] listFiles(final FileFilter aFilter) {
		if (size()>0) {
			int theInitCapacity = (aFilter!=null)?10:sizeofAll();
			final ArrayList<File> theFiles = new ArrayList<File>(theInitCapacity);
			foreach(new OnEachFile() {
				@Override
				public boolean process(File aFile) {
					if (aFilter!=null) {
						if (aFilter.accept(aFile))
							theFiles.add(aFile);
					} else
						theFiles.add(aFile);
					return true;
				}
			});
			if (theFiles.size()>0) {
				File[] theResult = new File[theFiles.size()];
				return theFiles.toArray(theResult);
			} else
				return null;
		} else
			return null;
	}

	/**
	 * Gets a list of the files contained in this orchard.
	 *
	 * @return Returns an array of files or null.
	 */
	public File[] listFiles() {
		return listFiles(null);
	}

}

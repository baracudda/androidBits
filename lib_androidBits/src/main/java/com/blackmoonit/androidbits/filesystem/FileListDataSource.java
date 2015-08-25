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

import android.app.Activity;
import android.app.ListActivity;
import android.os.Environment;

import com.blackmoonit.androidbits.concurrent.ThreadDaemon;
import com.blackmoonit.androidbits.concurrent.ThreadInterruptable;
import com.blackmoonit.androidbits.concurrent.ThreadTask;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * DataSource supplying list of files on local filesystem.
 * Actions files can perform also defined here.
 *
 * @author Ryan Fischbach
 */
public class FileListDataSource extends ArrayList<FileListAdapterElement> {
	static private final long serialVersionUID = 4362538043449602887L;

	protected Activity mAct;
	protected final MIMEtypeMap mMimeMap;
	protected FileOrchard mMarkedFiles;
	protected String mCurrPath;
	public boolean mHideUnreadable = true;
	public boolean mHideHidden = true;
	protected String mPickFileFilterRegEx = null;
	protected String mPickFileFilterMIMEcategory = null;
	protected String mPickFileFilterMIMEinstance = null;
	public FileComparator mSorterInUse = null;
	protected FileMatcher mFileMatcher = null;
	protected String mSearchRoot = null;
	protected FileOrchard mSearchResults = null;
	public ThreadTask mProduceSearchResultsTask = null;
	public ThreadDaemon mConsumeSearchResultsTask = null;
	protected Runnable mOnFinishSearchTask = null;
	public FileFilter mFileFilter = null;

	/**
	 * Non UI constructor for background thread work like pruning recycle bin.
	 * @param aFileComparatorToUse - default sorter (can be null, in which no sorting is done)
	 * @param aFileFilter - default filter (can be null)
	 */
	public FileListDataSource(FileComparator aFileComparatorToUse, FileFilter aFileFilter) {
		mAct = null;
		mMimeMap = null;
		mCurrPath = null;
		mHideHidden = false;
		mHideUnreadable = false;
		mSorterInUse = aFileComparatorToUse;
		mFileFilter = aFileFilter;
	}

	/**
	 * Alternate Constructor used for UI facing activities.
	 * @param aAct - UI activity
	 */
	public FileListDataSource(ListActivity aAct) {
		this(aAct,null,null);
	}

	/**
	 * Constructor used for UI facing activities.
	 * @param aAct - UI activity
	 * @param aMIMEmap - mime map used to determine file types to possibly filter
	 * @param aMarkedFiles - selected files in case you need to know which in current list are selected
	 */
	public FileListDataSource(Activity aAct, MIMEtypeMap aMIMEmap, FileOrchard aMarkedFiles) {
		mAct = aAct;
		if (aMIMEmap!=null) {
			mMimeMap = aMIMEmap;
		} else {
			mMimeMap = new MIMEtypeMap().createMaps();
		}
		mMarkedFiles = aMarkedFiles;
		mCurrPath = Environment.getExternalStorageDirectory().getPath();

		mFileFilter = new FileFilter() {
			public boolean accept(File aFile) {
				boolean b = true;
				if (mHideUnreadable)
					b = aFile.canRead();
				if (b && mHideHidden)
					b = !aFile.isHidden();
				if (b && mPickFileFilterMIMEcategory!=null && aFile.isFile() && mMimeMap!=null) {
					b = mMimeMap.matchType(mPickFileFilterMIMEcategory,mPickFileFilterMIMEinstance,aFile);
				}
				if (b && mPickFileFilterRegEx!=null && aFile.isFile()) {
					b = aFile.getName().matches(mPickFileFilterRegEx);
				}
				return b;
			}
		};
	}

	/**
	 * Helper method to retrieve a FileListAdapterElements[] instead of merely a File[] from a folder.
	 * @param aFolder - the folder object
	 * @param aFileFilter - file filter to be used when retrieving the folder.listFiles();
	 * @return Returns an unsorted array.
	 */
	public static FileListAdapterElement[] listFiles(File aFolder, FileFilter aFileFilter) {
		FileListAdapterElement[] theResult = null;
		if (aFolder!=null) {
			File[] theFileList = null;
			theFileList = aFolder.listFiles(aFileFilter);
			if (theFileList!=null) {
				theResult = new FileListAdapterElement[theFileList.length];
				for (int i = 0; i<theFileList.length; i++) {
					theResult[i] = new FileListAdapterElement(theFileList[i].getPath());
					if (!ThreadInterruptable.isUiThread())
						Thread.yield();
				}
			}
		}
		return theResult;
	}

	protected Folder ensureFolderExists(File aFolder) {
		if (!aFolder.exists() || !aFolder.canRead()) {
			aFolder = Environment.getExternalStorageDirectory();
			//home folder may be sdcard and may not be mounted (not mounted = exists, not readable)
			if (!BitsFileUtils.isExternalStorageMounted() || !aFolder.exists() || !aFolder.canRead()) {
				return new Folder(File.separator);
			}
		}
		return new Folder(aFolder);
	}

	protected Folder parseFolderPath(String aPath) {
		if (aPath==null)
			aPath = mCurrPath;
		String theNewPath = mCurrPath;
		if (aPath.equals("..")) {
			// convert ".." into the proper parent folder name
			File tempFile = new File(theNewPath);
			theNewPath = tempFile.getParent();
			if (theNewPath==null || theNewPath.equals("")) {
				theNewPath = File.separator;
			}
		} else if (aPath.equals("")) {
			theNewPath = File.separator;
		} else {
			theNewPath = aPath;
		}
		return new Folder(theNewPath);
	}

	/**
	 * Resets the list to the contents of aPath.<br>
	 * ".." means parent of current folder.<br>
	 * "?" means show search results.<br>
	 * "" is the equivalent of root path of File.separator.
	 *
	 * @param aPath - path to display
	 * @return Returns the folder that was displayed (may have changed to ensure existance).
	 */
	public Folder fillList(String aPath) {
		clear();
		if (!aPath.equals("?")) {
			Folder theNewPath = ensureFolderExists(parseFolderPath(aPath));
			if (mSearchResults!=null) {
				if (mSearchRoot!=null) {
					if (mSearchRoot.equals(theNewPath.getPath())) {
						mSearchRoot = null;
						mCurrPath = "?";
						addAll(mSearchResults);
						sortList();
						return null;
					}
				} else {
					mSearchRoot = theNewPath.getParent();
				}
			}
			mCurrPath = theNewPath.getPath();
			addAll(theNewPath);
			sortList();
			return theNewPath;
		} else {
			mCurrPath = aPath;
			addAll(mSearchResults);
			sortList();
			return null;
		}
	}

	/**
	 * Convenience method to refill the list with the current path, reflecting any changes
	 * to the file system since the last time the list was filled.
	 * @return Returns the folder that was displayed (may have changed to ensure existance).
	 */
	public Folder refillList() {
		return fillList(mCurrPath);
	}

	/**
	 * Retrieve and sort folder contents.
	 * @param aFolder - folder contents desired
	 * @return Returns the folder that was displayed (may have changed to ensure existance).
	 */
	public Folder fillList(File aFolder) {
		clear();
		Folder theFolder = new Folder(ensureFolderExists(aFolder));
		addAll(theFolder);
		sortList();
		return theFolder;
	}

	/**
	 * Add a file to the data source.
	 * @param aFile - file to add
	 */
	public void add(File aFile) {
		FileListAdapterElement theItem = new FileListAdapterElement(aFile.getPath());
		if (mMarkedFiles!=null)
			theItem.setMarkings(mMarkedFiles,aFile);
		add(theItem);
	}

	/**
	 * Adds all the files in the array.
	 * @param aFileList - array of files to add
	 */
	public void addAll(File[] aFileList) {
		if (aFileList!=null) {
			ensureCapacity(aFileList.length);
			for (File theFile: aFileList) {
				add(theFile);
				if (!ThreadInterruptable.isUiThread())
					Thread.yield();
			}
		}
	}

	/**
	 * Add all files found in the folder.
	 * @param aFolder - folder file object
	 */
	public void addAll(File aFolder) {
		if (aFolder!=null) {
			File[] theFileList;
			if (mFileFilter!=null) {
				theFileList = aFolder.listFiles(mFileFilter);
			} else
				theFileList = aFolder.listFiles();
			addAll(theFileList);
		}
	}

	/**
	 * Add all files found in the FileOrchard.
	 * @param aFileOrchard - container with files to add.
	 */
	public void addAll(FileOrchard aFileOrchard) {
		if (aFileOrchard!=null && aFileOrchard.size()>0) {
			final FileFilter theFileFilter;
			if (mFileFilter!=null) {
				theFileFilter = mFileFilter;
				ensureCapacity(10);
			} else {
				theFileFilter = null;
				ensureCapacity(aFileOrchard.sizeofAll());
			}
			aFileOrchard.foreach(new FileOrchard.OnEachFile() {
				@Override
				public boolean process(File aFile) {
					if (theFileFilter==null || theFileFilter.accept(aFile)) {
						add(aFile);
					}
					return true;
				}
			});
		}
	}

	/**
	 * Sorts the existing list according to the {@link #mSorterInUse}.
	 */
	public void sortList() {
		if (mSorterInUse!=null) {
			try {
				Collections.sort(this,mSorterInUse);
			} catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException(iae.getMessage()+
						"; sorter="+mSorterInUse.toString(),iae.getCause());
			}
		}
	}

	/**
	 * Given the display name of the file, return the index of it.
	 *
	 * @param aDisplayName - display name of folder or file
	 * @return Returns the index if found, else -1.
	 */
	public int indexOfName(String aDisplayName) {
		if (aDisplayName==null)
			return -1;
		for (int i=0; i<size(); i++) {
			FileListAdapterElement theItem = get(i);
			if (theItem!=null && aDisplayName.equals(theItem.getDisplayName()))
				return i;
		}
		return -1;
	}

	/**
	 * Find the file and return the item associated with it.
	 *
	 * @param aFile - file to find
	 * @return Returns the found {@link FileListAdapterElement} from the list, else null.
	 */
	public FileListAdapterElement find(File aFile) {
		if (!"?".equals(mCurrPath) && mCurrPath!=null && aFile!=null && !mCurrPath.equals(aFile.getParent()))
			return null;
		for (int i=0; i<size(); i++) {
			FileListAdapterElement theItem = get(i);
			if (aFile==theItem || (theItem!=null && theItem.equals(aFile)))
				return theItem;
		}
		return null;
	}

	/**
	 * Find the file and remove the item associated with it.
	 *
	 * @param aFile - file to remove
	 * @return Returns TRUE if file is found and removed, else FALSE.
	 */
	public boolean removeFile(File aFile) {
		if (!"?".equals(mCurrPath) && mCurrPath!=null && aFile!=null && !mCurrPath.equals(aFile.getParent()))
			return false;
		for (int i=0; i<size(); i++) {
			FileListAdapterElement theItem = get(i);
			if (aFile==theItem || (theItem!=null && theItem.equals(aFile))) {
				remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Halts the current search tasks.
	 */
	public void stopSearchTask() {
		if (mProduceSearchResultsTask!=null) {
			mProduceSearchResultsTask.halt();
			mProduceSearchResultsTask = null;
		}
		if (mConsumeSearchResultsTask!=null) {
			mConsumeSearchResultsTask.halt();
			mConsumeSearchResultsTask = null;
		}
	}

	public FileListDataSource setFileFilterRegEx(String aRegExPattern) {
		mPickFileFilterRegEx = aRegExPattern;
		return this;
	}

	public FileListDataSource setFileFilterMIMEtype(String aMIMEtype) {
		if (aMIMEtype!=null && !aMIMEtype.equals("*/*")) {
			int idxMatchSeparator = aMIMEtype.lastIndexOf("/");
			mPickFileFilterMIMEcategory = aMIMEtype.substring(0,idxMatchSeparator);
			mPickFileFilterMIMEinstance = aMIMEtype.substring(idxMatchSeparator+1);
		} else {
			mPickFileFilterMIMEcategory = null;
			mPickFileFilterMIMEinstance = null;
		}
		return this;
	}

	/**
	 * Start searching on the query in background thread.
	 *
	 * @param aQuery - user query
	 */
	public void setSearchQuery(String aQuery, final File aSearchFolder, final Runnable aRefreshCallback) {
		mSearchResults = new FileOrchard();

		mFileMatcher = new FileMatcher(aQuery);
		mFileMatcher.mMimeMap = mMimeMap;

		//consumer thread that will display finds to the UI
		mConsumeSearchResultsTask = new ThreadDaemon(1000L);
		mConsumeSearchResultsTask.setProcessName("display search results");
		mConsumeSearchResultsTask.setTask(new Runnable() {
			@Override
			public void run() {
				if (mCurrPath.equals("?")) {
					int theSoftLimit = 0;
					while (!mFileMatcher.mSearchResults.isEmpty()) {
						File theFile = mFileMatcher.mSearchResults.remove();
						if (mSearchResults.addFile(theFile)) {
							add(theFile);
						}

						//faster devices may exceed this soft limit which will cause it to
						//  only display 100 items every second, but the
						//  human brain can only process information so fast and this seems
						//  a good amount to toss at the person to scan through
						if (theSoftLimit++>100)
							break;
					};
					if (aRefreshCallback!=null) {
						aRefreshCallback.run();
					}
					//if producer is done producing, stop consuming
					if (mFileMatcher.mSearchResults.isEmpty() && mFileMatcher.isSearchFinished()) {
						if (mCurrPath.equals("?")) {
							sortList();
							if (aRefreshCallback!=null) {
								aRefreshCallback.run();
							}
						}
						if (mAct!=null)
							mAct.setProgressBarIndeterminateVisibility(false);

						if (mConsumeSearchResultsTask!=null) {
							mConsumeSearchResultsTask.halt();
							mConsumeSearchResultsTask = null;
						}

						if (mOnFinishSearchTask!=null) {
							mOnFinishSearchTask.run();
							mOnFinishSearchTask = null;
						}
					}
				}
			}
		},(mAct!=null),mAct);

		mProduceSearchResultsTask = new ThreadTask(new Runnable() {
			@Override
			public void run() {
				/*
				if (aSearchFolder!=null)
					mFileMatcher.searchFolder(aSearchFolder);
				else
					mFileMatcher.searchFolders(FileMatcher.getStandardSearchFolders());
				*/
				//searches current folder first, followed by all standard public folders
				mFileMatcher.searchFolders(FileMatcher.getStandardSearchFolders(aSearchFolder));
				System.gc();
			}
		},"searching filesystem",Thread.NORM_PRIORITY-2);

	}

	/**
	 * Starts the search task previously defined with {@link #setSearchQuery(String, java.io.File, Runnable)}.
	 * @param aOnFinishSearch - Runnable to be executed upon finishing the search task.
	 */
	public void startSearchTask(Runnable aOnFinishSearch) {
		if (mProduceSearchResultsTask!=null && mConsumeSearchResultsTask!=null) {
			mOnFinishSearchTask = aOnFinishSearch;
			if (mAct!=null)
				mAct.setProgressBarIndeterminateVisibility(true);
			//start the threads
			mProduceSearchResultsTask.start();
			Thread.yield();
			mConsumeSearchResultsTask.start();
		}
	}

}

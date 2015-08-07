package com.blackmoonit.androidbits.filesystem;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static class for file system utility functions.
 * Fob, short for "file object" is going to be the standard term for "file or folder".
 * That way, naming conventions like "copyFile" will only be for files, "copyFolder" will only apply to
 * folders and "copyFob" can take either a file or a folder.
 *
 * @author Ryan Fischbach
 */
public final class BitsFileUtils {
	/**
	 * MIME type specific to the concept of a "folder" containing other files and/or more folders.
	 */
	static public final String MIMETYPE_FOLDER = "container/directory";
	/**
	 * MIME type meaning several different categories/types are present.
	 */
	static public final String MIMETYPE_MIXED = "multipart/mixed";
	/**
	 * MIME type for ._%filename.ext% files that contain the mac-specific desktop display settings.
	 * These files always start with "._" and the rest will be the name of another file in the same folder.
	 */
	static public final String MIMETYPE_APPLEDOUBLE_RES = "application/applefile";

	/**
	 * Match: filename-1.ext, filename_2.ext, filename(3).ext<br>
	 * Group on (filename)-1(.ext)<br>
	 * php-style regex: <code>/^(.+)(?:[\(_-])(\d+)(?:\)*)(\..*)?/</code>
	 */
	static public final String DUPLICATE_FILENAME_REGEX = "^(.+)(?:[\\(_-])(\\d+)(?:\\)*)(\\..*)?";

	static public String FOLDER_ALARMS = "media"+File.separator+"audio"+File.separator+"alarms"; //just "Alarms" in 2.0
	static public String FOLDER_DCIM = "DCIM";
	static public String FOLDER_DOWNLOADS = "Download";
	static public String FOLDER_MOVIES = "Movies";
	static public String FOLDER_MUSIC = "Music";
	static public String FOLDER_NOTIFICATIONS = "media"+File.separator+"audio"+File.separator+"notifications"; //just "Notifications" in 2.0
	static public String FOLDER_PICTURES = "Pictures";
	static public String FOLDER_PODCASTS = "Podcasts";
	static public String FOLDER_RINGTONES = "media"+File.separator+"audio"+File.separator+"ringtones"; //just "Ringtones" in 2.0

	/**
	 * Used by *File and *Fob functions when processing each file or subfile.
	 */
	static public interface OnEachFile {
		/**
		 * Pre-processing before a copy/move/delete operation should take place.
		 * @param srcFob - source file/folder to be operated on
		 * @param destFob - destination file/folder to be operated on (null in case of delete)
		 */
		public void beforeProcess(File srcFob, File destFob);
		/**
		 * Post-processing after a copy/move/delete operation has taken place.
		 * @param srcFile - source file/folder to be operated on
		 * @param destFile - destination file/folder to be operated on (null in case of delete)
		 */
	    public void afterProcess(File srcFile, File destFile);
	}

	static {
		initCompatibility();
	}

	static private Method mContext_getExternalCacheDir = null;
	static private Method mEnvironment_getExternalStoragePublicDirectory = null;

	static protected void initCompatibility() {
		try {
			FOLDER_ALARMS = (String)Environment.class.getField("DIRECTORY_ALARMS").get(null);
			FOLDER_DCIM = (String)Environment.class.getField("DIRECTORY_DCIM").get(null);
			FOLDER_DOWNLOADS = (String)Environment.class.getField("DIRECTORY_DOWNLOADS").get(null);
			FOLDER_MOVIES = (String)Environment.class.getField("DIRECTORY_MOVIES").get(null);
			FOLDER_MUSIC = (String)Environment.class.getField("DIRECTORY_MUSIC").get(null);
			FOLDER_NOTIFICATIONS = (String)Environment.class.getField("DIRECTORY_NOTIFICATIONS").get(null);
			FOLDER_PICTURES = (String)Environment.class.getField("DIRECTORY_PICTURES").get(null);
			FOLDER_PODCASTS = (String)Environment.class.getField("DIRECTORY_PODCASTS").get(null);
			FOLDER_RINGTONES = (String)Environment.class.getField("DIRECTORY_RINGTONES").get(null);
		} catch (Exception e) {
			//leave them at their default values (if dies on first one, will die on all in group)
		}
		try {
			mEnvironment_getExternalStoragePublicDirectory = Environment.class.getMethod(
					"getExternalStoragePublicDirectory", new Class[] {String.class} );
		} catch (NoSuchMethodException nsme) {
			mEnvironment_getExternalStoragePublicDirectory = null;
		}
	}

	/**
	 * Used by some functions like copy and move in case an IOError occurs as an attempt to recover.
	 */
	static public OnEachFile onOutOfSpaceEvent = null;

	/**
	 * Same as File.canRead() except that it will not throw an exception.
	 * @param aFile - file to query
	 * @return Returns TRUE if canRead() returns true, false otherwise.
	 */
	static public boolean isReadable(File aFile) {
		try {
			return aFile.canRead();
		} catch (NullPointerException npe) {
			return false;
		} catch (SecurityException se) {
			return false;
		}
	}

	/**
	 * Same as File.canWrite() except that it will not throw an exception.
	 * @param aFile - file to query
	 * @return Returns TRUE if canWrite() returns true, false otherwise.
	 */
	static public boolean isWriteable(File aFile) {
		try {
			return aFile.canWrite();
		} catch (NullPointerException npe) {
			return false;
		} catch (SecurityException se) {
			return false;
		}
	}

	/**
	 * Given aFolder, aPrefix, and aExtension, return a guaranteed non-existing file object
	 * that is safe to use as a non-existing destination file for an output file operation.
	 * Similar in function to {@link #getSafeNewFile(java.io.File, String)}, this differs from it in that
	 * all file objects returned will have a file in the form "%aFolder%/aPrefix#aExtension",
	 * meaning that all results will have a number included in the filename, starting with 1.<br>
	 * The result is not "reserved" and is subject to being taken by another process.
	 *
	 * @param aFolder - The directory where "aPrefix#aExtension" will be attempted.
	 * @param aPrefix - The filename portion to use before the number.
	 * @param aExtension - The filename portion to use after the number.
	 * @return Returns a File object representing the non-existant file.
	 * @see #getAutoGeneratedFileName(java.io.File, String, String)
	 * @see #getSafeNewFile(java.io.File, String)
	 */
	static public File getAutoGeneratedFile(File aFolder, String aPrefix, String aExtension) {
		//determine what the new auto-generated filename should be
		int i = 0;
		File nf = null;
		if (aFolder!=null && aFolder.exists()) {
			do {
				i+=1;
				nf = new File(aFolder,aPrefix+i+aExtension);
			} while (nf.exists() && i<Integer.MAX_VALUE);
		} else {
			nf = new File(aPrefix+i+aExtension);
		}
		if (i<Integer.MAX_VALUE) {
			return nf;
		} else {
			return null;
		}
	}

	/**
	 * Given aFolder, aPrefix, and aExtension, return a guaranteed non-existing file path string
	 * that is safe to use as a non-existing destination filepath for an output file operation.
	 * Similar in function to {@link #getSafeNewFile(java.io.File, String)}, this differs from it in that
	 * all filepaths returned will be of the form "%aFolder%/aPrefix#aExtension", meaning that
	 * all results will have a number included in them, starting with 1.<br>
	 * The result is not "reserved" and is subject to being taken by another process.
	 *
	 * @param aFolder - The directory where "aPrefix#aExtension" will be attempted.
	 * @param aPrefix - The filename portion to use before the number.
	 * @param aExtension - The filename portion to use after the number.
	 * @return Returns a string representing the filepath of the non-existant file.
	 * @see #getAutoGeneratedFile(java.io.File, String, String)
	 * @see #getSafeNewFile(java.io.File, String)
	 */
	static public String getAutoGeneratedFileName(File aFolder, String aPrefix, String aExtension) {
		File nf = getAutoGeneratedFile(aFolder,aPrefix,aExtension);
		if (nf!=null) {
			return nf.getName();
		} else {
			return null;
		}
	}

	/**
	 * Given aFolder and aFilename, return a guaranteed non-existing file object that is safe
	 * to use as a non-existing destination file object for an output file operation. The function
	 * will start with the filename and path given and only attempt to modify it if the result
	 * already exists.<br>
	 * The result is not "reserved" and is subject to being taken by another process.
	 *
	 * @param aFolder - directory where the filename needs to be placed
	 * @param aFilename - file or folder name desired
	 * @return Returns the desired file if it does not exist. If it already exists, then it will
	 * try to return variations of the filename until it finds a combination that does not exist yet.
	 * Filename pattern variations attempted append a "-#" to the end of the filename, but before the
	 * extension. Variations on the number appended include "_#" and "(#)" if already found in the
	 * filename.
	 * @see #getAutoGeneratedFile(java.io.File, String, String)
	 * @see #getAutoGeneratedFileName(java.io.File, String, String)
	 */
	static public File getSafeNewFile(File aFolder, String aFilename) {
		File nf = new File(aFolder,aFilename);
		if (nf.exists()) {
			int i = 0;
			//determine what the new auto-generated filename should be
			if (aFilename.indexOf("?")<0) {
				int startOffset = (aFilename.endsWith(".bak"))?aFilename.length()-5:-1;
				int idxExtension;
				if (startOffset<0)
					idxExtension = aFilename.lastIndexOf(".");
				else {
					idxExtension = aFilename.lastIndexOf(".",startOffset);
					if (idxExtension<0) {
						idxExtension = startOffset;
					}
				}
				if (idxExtension>0) {
					String fname = aFilename.substring(0,idxExtension);
					//php-style regex: /^(?:.+[\(_-])(\d+)(?:\)*)$/
					String regex = "^(?:.+[\\(_-])(\\d+)(?:\\)*)$";
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(fname);
					if (m.find()) {
						i = Integer.getInteger(m.group(1),i);
						aFilename = fname.substring(0,m.start(1))+"?"+fname.substring(m.end(1))+
								aFilename.substring(idxExtension);
					} else {
						aFilename = aFilename.substring(0,idxExtension)+"-?"+aFilename.substring(idxExtension);
					}
				} else
					aFilename = aFilename+"-?";
			}
			while (nf.exists() && i<Integer.MAX_VALUE) {
				i+=1;
				nf = new File(aFolder,aFilename.replaceFirst("\\?",Integer.toString(i)));
			}
		}
		return nf;
	}

	/**
	 * Given the file, return a name that will not conflict with any existing file, starting
	 * with the file given and modifying it only if it already exists.<br>
	 * The result is not "reserved" and is subject to being taken by another process.
	 *
	 * @param aFile - a file that may or may not exist yet.
	 * @return Returns a file path to a guaranteed non-existant file.
	 * @see #getSafeNewFile(java.io.File, String)
	 */
	static public File getSafeNewFile(File aFile) {
		return getSafeNewFile(aFile.getParentFile(),aFile.getName());
	}

	/**
	 * Return the category portion of a MIME type.<br>
	 * e.g. "image/*" is returned for "image/jpeg"
	 *
	 * @param aMIMEtype - a MIME type string in the form of "category/type"
	 * @return Returns "category/*" if aMIMEtype is not null and "&#42;/&#42;" otherwise.
	 */
	static public String getMIMEcategory(String aMIMEtype) {
		if (aMIMEtype!=null) {
			aMIMEtype = aMIMEtype.substring(0,aMIMEtype.lastIndexOf("/"))+"/*";
		} else {
			aMIMEtype = "*/*";
		}
		return aMIMEtype;
	}

	/**
	 * Test to see if the external storage device (SD card) is mounted and ready for action.
	 *
	 * @return Returns true if the OS determines the state of the external storage is mounted.
	 * @see android.os.Environment#getExternalStorageDirectory()
	 * @see android.os.Environment#MEDIA_MOUNTED
	 */
	static public boolean isExternalStorageMounted() {
		return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
	}

	/**
	 * Notify the OS to rescan the external storage device for changes made to its contents.
	 *
	 * @param aContext - Context used to send the broadcast event.
	 * @see android.content.Intent#ACTION_MEDIA_MOUNTED
	 */
	static public void refreshExternalStorage(Context aContext) {
		if (isExternalStorageMounted() && aContext!=null) {
			Uri mountPoint = Uri.fromFile(Environment.getExternalStorageDirectory());
			aContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,mountPoint));
			Thread.yield();
		}
	}

	/**
	 * After a file is saved to permanent storage, call this function to let the media scanner
	 * service know about it quickly. Image Gallery and Music Player cannot find new files unless
	 * the media service scans it or is notified.
	 *
	 * @param aContext - the context
	 * @param aFile - file to be scanned.
	 */
	static public void notifyMediaScanner(Context aContext, File aFile) {
		if (isExternalStorageMounted() && aContext!=null && aFile!=null && aFile.exists()) {
			Uri theUri = Uri.fromFile(aFile);
			aContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,theUri));
			Thread.yield();
		}
	}

	/**
	 * Wrapper for the API 8 (Android 2.2) {@link android.content.Context#getExternalCacheDir} function.
	 *
	 * @param aContext - context to use
	 * @return Returns the File object representing the external cache folder.  May be null.
	 */
	static public File getExternalCacheDir(Context aContext) {
		if (aContext==null)
			return null;
		if (mContext_getExternalCacheDir==null) {
			try {
				mContext_getExternalCacheDir = aContext.getClass().getMethod("getExternalCacheDir", new Class[] {} );
			} catch (NoSuchMethodException nsme) {
				//failure, must be older device
			}
		}
		File theResult = null;
		if (mContext_getExternalCacheDir!=null) {
			try {
				theResult = (File)mContext_getExternalCacheDir.invoke(aContext,(Object[])null);
			} catch (IllegalArgumentException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}
		} else {
			theResult = new File(Environment.getExternalStorageDirectory().getPath()+
					"/Android/data/"+aContext.getPackageName()+"/cache");
		}
		if (theResult!=null &&
			(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
			theResult.mkdirs();
			try {
				File noMediaMarker = new File(theResult,".nomedia");
				if (!noMediaMarker.exists())
					noMediaMarker.createNewFile();
			} catch (IOException e) {
				//just a nice thing for the user, don't care if this fails
			}
		}
		return theResult;
	}

	/**
	 * Get the cache folder we "should" use.  Start with external storage.
	 *
	 * @param aContext - context to use
	 * @return Returns the File object of the cache folder to use. Never null,
	 * however, path may not exist.
	 */
	static public File getCacheFolder(Context aContext) {
		File theFolder = getExternalCacheDir(aContext);
		if (theFolder==null) {
			theFolder = aContext.getCacheDir();
		}
		return theFolder;
	}

	/**
	 * Wrapper for the API 8 (Android 2.2)
	 * {@link android.os.Environment#getExternalStoragePublicDirectory(String)} function.
	 *
	 * @param aPublicFolder - string used for which public folder requested
	 * @return Returns the file representing the public folder desired.
	 */
	static public File getExternalPublicFolder(String aPublicFolder) {
		if (mEnvironment_getExternalStoragePublicDirectory!=null) {
			try {
				return (File)mEnvironment_getExternalStoragePublicDirectory.invoke(null, new Object[] {aPublicFolder});
			} catch (IllegalArgumentException e) {
				//leave default value
			} catch (IllegalAccessException e) {
				//leave default value
			} catch (InvocationTargetException e) {
				//leave default value
			}
		} else {
			return new File(Environment.getExternalStorageDirectory(),aPublicFolder);
		}
		return null;
	}

	/**
	 * Get the external system folder where cache files for apps reside (among other things).
	 *
	 * @return Returns the File representing the external system folder. Never null,
	 * however, path may not exist.
	 */
	static public File getExternalSystemFolder() {
		return new File(Environment.getExternalStorageDirectory(),"Android");
	}

	/**
	 * Get the external system folder where deleted files are moved into.
	 *
	 * @return Returns the File representing the external recycle bin. Never null,
	 * however, path may not exist.
	 */
	static public File getRecycleBin(Context aContext) {
		return new File(getCacheFolder(aContext),"recyclebin");
	}

	static public boolean isRecycleBinEmpty(Context aContext) {
		File theRecycleBin = getRecycleBin(aContext);
		if (theRecycleBin.exists()) {
			return (theRecycleBin.listFiles().length==0);
		} else {
			return true;
		}
	}

	/**
	 * An experimentally proven decent buffer size for file operations on Android phones.
	 */
	static public final int DEFAULT_FILE_BUFFER_SIZE = 1024*64;

	private static final HashMap<String,Integer> mFileBufferSizeCache = new HashMap<String,Integer>();

	private static int getFileBufferSize(File aFile) {
		Integer theBufferSize = null;
		if (aFile!=null) {
			String theExactPath = getCanonicalPath(aFile);
			String theExtStoragePath = getCanonicalPath(Environment.getExternalStorageDirectory());
			if (theExactPath.startsWith(theExtStoragePath)) {
				theBufferSize = mFileBufferSizeCache.get(theExtStoragePath);
			} else {
				theBufferSize = mFileBufferSizeCache.get(theExactPath);
			}
		}
		if (theBufferSize!=null)
			return theBufferSize;
		else
			return 0;
	}

	/**
	 * Determines a decent buffer size to use for file operations on aFile based on the
	 * file system used in the file path and the size of the file itself.
	 *
	 * @param aFile - is the file object to analyze. Only it's parent path needs to exist.
	 * @return Returns the computed buffer size to use for this file object.
	 * @see #computeFileBufferSize(android.os.StatFs)
	 */
	static public int computeFileBufferSize(File aFile) {
		int theBufferSize = getFileBufferSize(aFile);
		if (theBufferSize==0) {
			theBufferSize = DEFAULT_FILE_BUFFER_SIZE; //if fs stats not avail, use the default buffer size
			//buffer = 1 file block size, whatever that happens to be, compute the block size
			File theFolder = (aFile!=null)?aFile.getParentFile():null;
			if (aFile!=null && theFolder!=null && theFolder.exists() && theFolder.canWrite()) {
				try {
					theBufferSize = computeFileBufferSize(new StatFs(theFolder.getPath()));
				} catch (IllegalArgumentException iae) { //StatFs doesn't like some paths
					theBufferSize = DEFAULT_FILE_BUFFER_SIZE;
				}
			}
			mFileBufferSizeCache.put(getCanonicalPath(aFile), Integer.valueOf(theBufferSize));
		}
		return theBufferSize;
	}

	/**
	 * Determines a decent buffer size to use for file operations.
	 *
	 * @param aStatFs - optional file system information
	 * @return Returns a minimum buffer size to use for file operations given the statistics
	 * passed into the function.  Default buffer size is {@link #DEFAULT_FILE_BUFFER_SIZE}.
	 */
	static public int computeFileBufferSize(StatFs aStatFs) {
		if (aStatFs!=null) {
			return Math.max(aStatFs.getBlockSize()*2,DEFAULT_FILE_BUFFER_SIZE);
		} else {
			return DEFAULT_FILE_BUFFER_SIZE;
		}
	}

	/**
	 * Copies srcFile to destFile.  If destFile exists, it will be deleted first. If srcFile has
	 * a 0 length, the destFile will still be deleted if it exists and created with 0 length.
	 *
	 * @param srcFile - file to be copied
	 * @param destFile - file to receive the copied data
	 * @throws java.io.IOException if the copy process encounters one.
	 * @throws java.io.FileNotFoundException if the srcFile does not exist.
	 */
	static public void copyFile(File srcFile, File destFile) throws FileNotFoundException, IOException {
		copyFile(srcFile,destFile,null,null);
	}

	/**
	 * Copies srcFile to destFile.  If destFile exists, it will be deleted first. If srcFile has
	 * a 0 length, the destFile will still be deleted if it exists and created with 0 length.
	 * The destFile will have the same Last Modified datetime as the srcFile.
	 *
	 * @param srcFile - file to be copied
	 * @param destFile - file to receive the copied data
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar
	 * @throws java.io.IOException if the copy process encounters one.
	 * @throws java.io.FileNotFoundException if the srcFile does not exist.
	 */
	static public void copyFile(File srcFile, File destFile, ProgressBarHandler aMsgHandler,
			final Object aProgressID) throws FileNotFoundException, IOException {
		if (srcFile.isDirectory())
			return;
		//delete the destination file, if it exists
		if (destFile.exists())
			destFile.delete();
		destFile.createNewFile();
		if (srcFile.length()>0) {
			copyStream(new FileInputStream(srcFile),new FileOutputStream(destFile),
					computeFileBufferSize(destFile),
					srcFile.length(),aMsgHandler,aProgressID);
			if (destFile.exists())
				destFile.setLastModified(srcFile.lastModified());
		}
	}

	/**
	 * Workhorse function that will open buffered streams with default size and then copy the data.
	 *
	 * @param inStream - input stream to be copied
	 * @param outStream - output stream to receive the copied data
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar
	 * @throws java.io.IOException if the copy process encounters one.
	 */
	static public void copyStream(InputStream inStream, OutputStream outStream,
			ProgressBarHandler aMsgHandler, final Object aProgressID) throws IOException {
		copyStream(inStream,outStream,DEFAULT_FILE_BUFFER_SIZE,-1,aMsgHandler,aProgressID);
	}

	/**
	 * Workhorse function that will open buffered streams and then copy the data.
	 *
	 * @param inStream - input stream to be copied
	 * @param outStream - output stream to receive the copied data
	 * @param aBufferSize - buffer size to use. a minimum size will be enforced so passing 0 is ok.
	 * @param aInputSize - size of the input stream, if known, -1 otherwise. Only useful if
	 * aMsgHandler is not null.
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar
	 * @throws java.io.IOException if the copy process encounters one.
	 */
	static public void copyStream(final InputStream inStream, final OutputStream outStream, int aBufferSize, long aInputSize,
			final ProgressBarHandler aMsgHandler, final Object aProgressID) throws IOException {
		int theBufferSize = Math.max(aBufferSize,DEFAULT_FILE_BUFFER_SIZE);
		byte[] theBuffer = new byte[theBufferSize];
		BufferedInputStream src = new BufferedInputStream(inStream,theBufferSize);
		BufferedOutputStream dst = new BufferedOutputStream(outStream,theBufferSize);
		copyStreamData(src,dst,theBuffer,aInputSize,aMsgHandler,aProgressID);
		dst.close();
		src.close();
		theBuffer = null;
	}

	/**
	 * Workhorse function that actually moves the stream data from inStream to outStream.
	 * Both streams must already be open and ready for action. Opening/closing of the streams is
	 * not a part of this function and must be handled by the caller.
	 *
	 * @param inStream - opened input stream
	 * @param outStream - opened output stream
	 * @param aBuffer - buffer to use for the operation, IOException thrown if null.
	 * @param aInputSize - size of the input stream, if known, -1 otherwise. Only useful if
	 * aMsgHandler is not null.
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar
	 * @throws java.io.IOException if aBuffer is null or the copy process encounters one.
	 */
	static public void copyStreamData(final InputStream inStream, final OutputStream outStream,
			byte[] aBuffer, long aInputSize,
			final ProgressBarHandler aMsgHandler, final Object aProgressID) throws IOException {
		if (aBuffer==null)
			throw new IOException("Buffer is null.");
		int theBufferSize = aBuffer.length;
		int numBytesRead = 0;
		if (aMsgHandler!=null) synchronized (aMsgHandler) {
			aMsgHandler.getMsgProgressItemStart(aProgressID,aInputSize).sendToTarget();
		}
		int r = 0;
		long totalBytesRead = 0L;
		while ( (numBytesRead = inStream.read(aBuffer,0,theBufferSize)) > 0 ) {
		    outStream.write(aBuffer,0,numBytesRead);
		    totalBytesRead += numBytesRead;
			r += 1;
			if ((r%10)==0) {
				if (aMsgHandler!=null) synchronized (aMsgHandler) {
					aMsgHandler.getMsgProgressItemUpdate(aProgressID,totalBytesRead).sendToTarget();
				}
				r = 0; //reset so it will never accidentally wrap
				Thread.yield();
			}
		}//while
		if (aMsgHandler!=null) synchronized (aMsgHandler) {
			aMsgHandler.getMsgProgressItemFinish(aProgressID).sendToTarget();
		}
	}

	/**
	 * Handle all the special cases when renaming a file.
	 *
	 * @param aFob - is the file object to be renamed.
	 * @param newName - is the new filename.
	 * @return Returns a file object with the new filename if successful, null otherwise.
	 * @throws NullPointerException if aFile or newName is null.

	 */
	static public File renameFob(File aFob, String newName) {
		File origFile = aFob;
		File newFile = new File(origFile.getParent(),newName);
		File tempFile = null;
		// exists() ignores case, so we need to handle renames with just a case change
		boolean bAllowChange = true;
		if (newFile.exists()) {
			if (origFile.getName().equalsIgnoreCase(newName) && !origFile.getName().equals(newName) ) {
				tempFile = getAutoGeneratedFile(origFile.getParentFile(), "temp-", newName);
				bAllowChange = aFob.renameTo(tempFile); //now the 2nd renameTo will set it to the correctly cased new name
				aFob = tempFile; //aFile curiously does not take on the new name after renameTo()
			} else {
				bAllowChange = false;
			}
		}
		if (bAllowChange && aFob.renameTo(newFile)) {
			return newFile;
		} else {
			if (tempFile!=null) {
				//restore orig name
				tempFile.renameTo(origFile);
			}
			return null;
		}
	}

	/**
	 * Copies srcFob to destFob.  If destFob exists, it will be overwritten/merged.
	 * @param srcFob - file to be copied
	 * @param destFob - file to receive the copied data.
	 * @param aProcessEvent - defines the before/after processing.
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar.
	 * @return Returns NULL if Fob has been copied to the destination, else the exception that was raised.
	 */
	static public Exception copyFob(final File srcFob, final File destFob, OnEachFile aProcessEvent,
			ProgressBarHandler aMsgHandler, final Object aProgressID) {
		Exception theResult = null;
		try {
			if (aMsgHandler!=null) synchronized (aMsgHandler) {
				aMsgHandler.getMsgProgressTotalUpdate(aProgressID,srcFob.getName(),1L).sendToTarget();
			}
			if (theResult==null && aProcessEvent!=null)
				aProcessEvent.beforeProcess(srcFob,destFob);
			if (srcFob.equals(destFob))
				return theResult;
			if (srcFob.isDirectory()) {
				//make sure we do not get into infinite copy loops if destination is a subfolder of source
				FileFilter theFileFilter = new FileFilter() {
					@Override
					public boolean accept(File aFob) {
						if (destFob.equals(aFob) || destFob.getPath().startsWith(aFob.getPath())
								|| BitsFileUtils.getCanonicalPath(destFob).startsWith(aFob.getPath()))
							return false;
						else
							return true;
					}
				};
				//get folder contents that need to be copied
				File[] theFolderContents = srcFob.listFiles(theFileFilter);
				if (aMsgHandler!=null) synchronized (aMsgHandler) {
					aMsgHandler.getMsgProgressIncreaseTotal(aProgressID,theFolderContents.length*1L).sendToTarget();
				}
				//make sure destFolder exists
				if (!destFob.mkdir()) {
					throw new IOException("Failed to create folder.");
				}
				//copy folder contents
				for (File theSrcFob:theFolderContents) {
					File theDestFob = new File(BitsFileUtils.getCanonicalPath(destFob),theSrcFob.getName());
					try {
						theResult = copyFob(theSrcFob,theDestFob,aProcessEvent,aMsgHandler,aProgressID);
						Thread.yield();
					} catch (StackOverflowError soe) {
						//too many subfolders or an infinite loop occured, ignore deeper folders
						continue;
					}
					if (theResult!=null)
						break;
				}
			} else {
				try {
					copyFile(srcFob,destFob,aMsgHandler,aProgressID);
				} catch (IOException ioe) {
					if (onOutOfSpaceEvent!=null) {
						onOutOfSpaceEvent.beforeProcess(srcFob, destFob);
						Thread.yield();
						copyFile(srcFob,destFob,aMsgHandler,aProgressID);
						onOutOfSpaceEvent.afterProcess(srcFob, destFob);
					} else {
						theResult = ioe;
					}
				}
			}
			if (theResult==null && aProcessEvent!=null)
				aProcessEvent.afterProcess(srcFob,destFob);
		} catch (Exception e) {
			return e;
		}
		return theResult;
	}

	/**
	 * Copies srcFob to destFob and then deletes srcFob. If destFob exists, it will be overwritten/merged.
	 * @param srcFob - file to be copied
	 * @param destFob - file to receive the copied data
	 * @param aProcessEvent - defines the before/after processing.
	 * @param aMsgHandler - a message Handler for copy progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar.
	 * @return Returns NULL if the file/folder has been moved to the destination, else the exception that was raised.
	 */
	static public Exception moveFob(final File srcFob, final File destFob, final OnEachFile aProcessEvent,
			ProgressBarHandler aMsgHandler, final Object aProgressID) {
		OnEachFile theMoveEvent = new OnEachFile() {
			@Override
			public void beforeProcess(File srcFob, File destFob) {
				if (aProcessEvent!=null)
					aProcessEvent.beforeProcess(srcFob,destFob);
			}
			@Override
			public void afterProcess(File srcFob, File destFob) {
				Exception theDelErr = null;
				if (!destFob.getPath().startsWith(srcFob.getPath()))
					theDelErr = deleteFob(srcFob,null,null,null);
				if (theDelErr==null && aProcessEvent!=null)
					aProcessEvent.afterProcess(srcFob,destFob);
			}
		};
		return copyFob(srcFob,destFob,theMoveEvent,aMsgHandler,aProgressID);
	}

	/**
	 * Delete a file.
	 * @param aFile - the file
	 * @return Return true if the file is successfully deleted, false otherwise.
	 */
	static public boolean deleteFile(File aFile) {
		try {
			return aFile.delete();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Delete a file, if it is a folder, then also delete it's contents.
	 * @param aFile - the file or folder
	 * @param aProcessEvent - defines the before/after processing.
	 * @param aMsgHandler - a message Handler for progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar.
	 * @return Return NULL if the file/folder is successfully deleted, else the exception that was raised.
	 */
	static public Exception deleteFob(File aFile, OnEachFile aProcessEvent,
			final ProgressBarHandler aMsgHandler, final Object aProgressID) {
		Exception theResult = null;
		try {
			if (aMsgHandler!=null) synchronized (aMsgHandler) {
				aMsgHandler.getMsgProgressTotalUpdate(aProgressID,aFile.getName(),1L).sendToTarget();
			}
			if (theResult==null && aProcessEvent!=null)
				aProcessEvent.beforeProcess(aFile,null);
			if (aFile.isDirectory())
				theResult = deleteFolderContents(aFile,aProcessEvent,aMsgHandler,aProgressID);
			if (theResult==null && !aFile.delete())
				return new IOException("delete returned false");
			if (theResult==null && aProcessEvent!=null)
				aProcessEvent.afterProcess(aFile,null);
		} catch (StackOverflowError soe) {
			return new IOException("folder contains nested circular symlinks, unable to auto-delete");
		} catch (Exception e) {
			return e;
		}
		return theResult;
	}

	/**
	 * Delete the contents of a folder without deleting the folder itself.
	 * @param aFolder - the folder
	 * @param aProcessEvent - defines the before/after processing.
	 * @param aMsgHandler - a message Handler for progress, if this parameter is used, you
	 * must include aProgressID.
	 * @param aProgressID - unique identifier for the progress bar.
	 * @return Return NULL if the contents are successfully deleted, else the exception that was raised.
	 */
	static public Exception deleteFolderContents(File aFolder, OnEachFile aProcessEvent,
			final ProgressBarHandler aMsgHandler, final Object aProgressID) {
		Exception theResult = null;
		try {
			if (aFolder.isDirectory()) {
				File[] subfiles = aFolder.listFiles();
				if (aMsgHandler!=null && subfiles!=null) synchronized (aMsgHandler) {
					aMsgHandler.getMsgProgressIncreaseTotal(aProgressID, subfiles.length*1L).sendToTarget();
				}
				for (File subFile:subfiles) {
					theResult = deleteFob(subFile,aProcessEvent,aMsgHandler,aProgressID);
					Thread.yield();
					if (theResult!=null)
						break;
				}
			}
		} catch (StackOverflowError soe) {
			return new IOException("folder contains nested circular symlinks, unable to auto-delete");
		} catch (Exception e) {
			return e;
		}
		return theResult;
	}

	/**
	 * Query the appropriate provider to determine the ID for the given file.
	 *
	 * @param aContext - Context used for the query
	 * @param aFile - Media file which is being searched for
	 * @param aProviderUri - Content provider which the query will run against
	 * @param aIDcol - name of ID column
	 * @param aFilePathCol - name of column containing the file path
	 * @return The ID the content provider uses for aFile or null if not found.
	 */
	static public Long getVisualMediaFileContentId(Context aContext, File aFile,
			Uri aProviderUri, String aIDcol, String aFilePathCol) {
		if (aContext==null || aFile==null || aProviderUri==null)
			return null;
		Long theId = null;
		ContentResolver cr = aContext.getContentResolver();
		// Columns to return
		//String[] theProjection = { aIDcol, aFilePathCol };
		String[] theProjection = { aIDcol };
		// Look for a picture which matches with the requested path
		// (MediaStore stores the path in column Images.Media.DATA)
		String theSelection = aFilePathCol + " = ?";
		String[] selArgs = { getCanonicalPath(aFile) };
		Cursor theCursor;
		try {
			theCursor = cr.query(aProviderUri,theProjection,theSelection,selArgs,null);
		} catch (Exception e) {
			/*
			 * Error encountered with an SDK "device"
			 * android.database.sqlite.SQLiteException: unable to open database file
			 * Device =rockchip/sdkDemo/sdkDemo/:1.5/CUPCAKE/eng.root.20100630.145439:eng/test-keys
			 */
			theCursor = null;
		}
		//if the cursor is null, that means the image file requested has not been cached yet
		//  do not auto call sdcard refresh at this point because it may just be in the process
		//  of creating the thumbnail and we're just asking for it too soon
		if (theCursor!=null) {
			if (theCursor.moveToFirst()) {
				//int idColumn = theCursor.getColumnIndex(aIDcol);
				//theId = theCursor.getLong(idColumn);
				theId = theCursor.getLong(0);
			}
			theCursor.close();
		}
		return theId;
	}

	/**
	 * Get the Uri used to reference the image file as the contents of the Image Provider.
	 *
	 * @param aContext - Context used to query the provider
	 * @param aImageFile - image file
	 * @return Returns the equivalent "content" scheme Uri if found in the Image Provider.
	 * If not found, the "file" scheme Uri is returned instead.
	 */
	static public Uri getContentUriFromImageFile(Context aContext, File aImageFile) {
		Long theId = getVisualMediaFileContentId(aContext,aImageFile,
				Images.Media.EXTERNAL_CONTENT_URI,Images.Media._ID,Images.Media.DATA);
		if (theId!=null)
			return Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI,Long.toString(theId));
		else
			return Uri.fromFile(aImageFile);
	}

	/**
	 * Get the Uri used to reference the thumbnail of the image file as the contents of the
	 * Image Provider.
	 *
	 * @param aContext - Context used to query the provider
	 * @param aImageFile - image file
	 * @return Returns the equivalent "content" scheme Uri if found in the Image Provider.
	 * If not found, NULL is returned instead.
	 */
	static public Uri getThumbnailUriFromImageFile(Context aContext, File aImageFile) {
		//first, get the ID of the orig image file
		Long theId = getVisualMediaFileContentId(aContext,aImageFile,
				Images.Media.EXTERNAL_CONTENT_URI,Images.Media._ID,Images.Media.DATA);
		//now query the thumbnail image provider using the orig image ID
		if (theId!=null) {
			ContentResolver cr = aContext.getContentResolver();
			String[] theProjection = { Images.Thumbnails._ID };
			String theSelection = Images.Thumbnails.IMAGE_ID + " = ?";
			String[] selArgs = { Long.toString(theId) };
			Cursor theCursor;
			try {
				theCursor = cr.query(Images.Thumbnails.EXTERNAL_CONTENT_URI,theProjection,theSelection,selArgs,null);
				if (theCursor.moveToFirst()) {
					theId = theCursor.getLong(0);
				}
				theCursor.close();
			} catch (Exception e) {
				theId = null;
			}
		}

		if (theId!=null)
			return Uri.withAppendedPath(Images.Thumbnails.EXTERNAL_CONTENT_URI,Long.toString(theId));
		else
			return null;
	}

	/**
	 * Get the Uri used to reference the video file as the contents of the Video Provider.
	 *
	 * @param aContext - Context used to query the provider
	 * @param aVideoFile - video file
	 * @return Returns the equivalent "content" scheme Uri if found in the Video Provider.
	 * If not found, the "file" scheme Uri is returned instead.
	 */
	static public Uri getContentUriFromVideoFile(Context aContext, File aVideoFile) {
		Long theId = getVisualMediaFileContentId(aContext,aVideoFile,
				Video.Media.EXTERNAL_CONTENT_URI,Video.Media._ID,Video.Media.DATA);
		if (theId!=null)
			return Uri.withAppendedPath(Video.Media.EXTERNAL_CONTENT_URI,Long.toString(theId));
		else
			return Uri.fromFile(aVideoFile);
	}

	/**
	 * Given the content://path/# Uri for a file, return the actual file path.
	 * Originally created for content://downloads/all_downloads/####,
	 * which is the 4.x web file download mechanism.
	 *
	 * @param aContext - Context used to query the provider
	 * @param aUri - Uri desired
	 * @return Returns the File object the Uri describes.
	 */
	static public File getFileFromContentUri(Context aContext, Uri aUri) {
		File theResult = null;
		ContentResolver cr = aContext.getContentResolver();
		// Equivalent to MediaStore.Files.FileColumns.DATA which is v11+
		String[] theProjection = { MediaStore.MediaColumns.DATA };
		Cursor theCursor;
		try {
			theCursor = cr.query(aUri,theProjection,null,null,null);
		} catch (Exception e) {
			theCursor = null;
		}
		if (theCursor!=null) {
			if (theCursor.moveToFirst()) {
				theResult = new File(theCursor.getString(0));
			}
			theCursor.close();
		}
		return theResult;
	}

	/**
	 * Given the file, return the portion of the name that the OS will use for determining the
	 * MIME type of the file.  e.g. "image.txt.jpg" will return ".jpg"
	 *
	 * @param aFile - a file object that may or may not exist.
	 * @return Returns the extension of the file with the leading ".", empty string if there is
	 * no extension, or null if aFile is null.
	 */
	static public String getExtension(File aFile) {
		if (aFile!=null) {
			String theFilename = aFile.getName();
			int idxExtension = theFilename.lastIndexOf(".");
			if (idxExtension>0)
				return theFilename.substring(theFilename.lastIndexOf("."));
			else
				return "";
		} else {
			return null;
		}
	}

	/**
	 * Replaces the portion of aFilename that the OS uses to determine the MIME type with the
	 * new extension. The new extension must be null, "", or start with "." or it will
	 * result in an IllegalArgumentException.<br>
	 * e.g. replaceExtension("/mnt/sdcard/somefile.txt.xml", ".jpg") will return
	 * "/mnt/sdcard/somefile.txt.jpg".<br>
	 * In the special case of a hidden folder name (filename consists solely of ".somefilename")
	 * then aNewExtension is added to the end of the filename.<br>
	 * e.g. replaceExtension("/mnt/sdcard/.myhiddenfolder",".jpg") will return
	 * "/mnt/sdcard/.myhiddenfolder.jpg".
	 *
	 * @param aFilename - the original filename (full path is optional)
	 * @param aNewExtension - the new extension (leading "." is required)
	 * @return Returns aFilename with its extension replaced with aNewExtension or null if
	 * aFilename is null.
	 * @throws IllegalArgumentException if aNewExtension is not null, not "", or fails to start
	 * with "."
	 */
	static public String replaceExtension(String aFilename, String aNewExtension) {
		if (aNewExtension==null)
			return aFilename;
		if (!aNewExtension.equals("") && !aNewExtension.startsWith("."))
			throw new IllegalArgumentException("aNewExtension must be null, \"\", or start with \".\"");
		if (aFilename!=null) {
			int idxExtension = aFilename.lastIndexOf(".");
			if (idxExtension>0)
				return aFilename.substring(0,aFilename.lastIndexOf("."))+aNewExtension;
			else
				return aFilename+aNewExtension;
		} else {
			return null;
		}
	}

	/**
	 * Removes the leading path segments from aFilename that equal aBasePath. If isDirectory is
	 * true, then the result will also have a trailing path seperator.
	 *
	 * @param aFilePath - is the full file path
	 * @param aBasePath - is the leading path segments to remove
	 * @return Returns the file path with aBasePath's leading segements removed. Returns null if
	 * aFilePath is null and if isDirectory is true, forces the result to end with a path seperator.
	 */
	static public String getRelativePath(String aFilePath, String aBasePath) {
		String s = null;
		if (aFilePath!=null) {
			s = aFilePath;
			if (aBasePath!=null) {
				if (!aBasePath.endsWith(File.separator))
					aBasePath = aBasePath+File.separator;
				if (s.startsWith(aBasePath))
					s = s.substring(aBasePath.length());
			}
		}
		return s;
	}

	/**
	 * Removes the leading path segments of aFile that match aBasePath.
	 *
	 * @param aFile - is the file object
	 * @param aBasePath - is the starting path segments to remove
	 * @return Returns the path string with aBasePath starting segments removed
	 * or null if aFile is null.
	 * @see #getRelativePath(String, String)
	 */
	static public String getRelativePath(File aFile, String aBasePath) {
		if (aFile!=null)
			return BitsFileUtils.getRelativePath(aFile.getPath(),aBasePath);
		else
			return null;
	}

	/**
	 * Returns the result of getRelativePath(aFilePath, ExternalStorageMountPath).
	 *
	 * @param aFilePath - is the full file path
	 * @return - Returns the file path with leading ExternalStorageMountPath removed.
	 * @see #getRelativePath(String, String)
	 */
	static public String getPathRelativeToExternalStorage(String aFilePath) {
		if (aFilePath!=null)
			return BitsFileUtils.getRelativePath(aFilePath,
					Environment.getExternalStorageDirectory().getPath());
		else
			return null;
	}

	/**
	 * Return a parent path shortened so that it is relative to ExternalStorageMountPath.
	 * @param aFile - file whose parent path will be shortened
	 * @return Returns the relative path of the file's parent or "" if parent is null.
	 */
	static public String getParentPathRelativeToExternalStorage(File aFile) {
		String s = getRelativePath(aFile.getParentFile(),
				Environment.getExternalStorageDirectory().getPath());
		if (s!=null)
			return s;
		else
			return "";
		/* while the below code is functionally equivalent, I received this error report:
		 *
		 * Blackmoon File Browser generated the following exception:
		 * java.lang.StringIndexOutOfBoundsException: start=0 end=-1 length=14
		 *
		 * --------- Activity Stack Trace ---------
		 * 1, ViewFileAsJumpPoint (Spring nach …)
		 * ----------------------------------------
		 *
		 * -------- Instruction Stack Trace -------
		 *
		 * 1, java.lang.String.startEndAndLength(String.java:598)
		 * 2, java.lang.String.substring(String.java:1561)
		 * 3, com.blackmoonit.filesystem.BitsFileUtils.getParentPathRelativeToExternalStorage(BitsFileUtils.java:880)
		 *
		 * which corresponds to the .substring line.  It makes NO sense to me why it should have occurred.
		 * the canonical path of the file, stripped of the first part of the string corresponding to the
		 * external SDcard path, enforced by always checking for "%scdard path%/" so that it always falls
		 * on a folder boundary should mean that whatever remains is either the filename itself or the
		 * relative path after %sdcard path% and then the filename.  The fact that it cannot find the filename
		 * is disturbing and I cannot account for any case where that should happen.
		 * Regardless, changing the code so that the point is moot.
		 *
		//if (!s.endsWith(File.separator))
		//	s += File.separator;
		String s = BitsFileUtils.getCanonicalPath(aFile);
		s = BitsFileUtils.getPathRelativeToExternalStorage(s);
		if (s!=null) {
			int i = s.lastIndexOf(aFile.getName());
			return (i>=0)?s.substring(0,i):;
		} else {
			return "";
		}
		*/
	}

	/**
	 * Similar to {@link java.io.File#getCanonicalPath()}, this will return it's result, but
	 * if an IOException occurs, {@link java.io.File#getPath()} will be returned instead.
	 *
	 * @param aFile - file to retrieve the cononical path
	 * @return Returns the path of the file with all references and symlinks resolved.
	 */
	static public String getCanonicalPath(File aFile) {
		try {
			return aFile.getCanonicalPath();
		} catch (NoSuchElementException nsee) {
			//Android bug: getCanonicalPath can generate this kind of exception as well
			return aFile.getAbsolutePath();
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			//Android bug: getCanonicalPath can generate this kind of exception as well
			return aFile.getAbsolutePath();
		} catch (NullPointerException npe) {
			return null;
		} catch (IOException e) {
			return aFile.getAbsolutePath();
		}
	}

	/**
	 * Android's custom ringtone folder.<br>
	 * %sdcard%/media/audio/ringtones
	 */
	static public final File customRingtoneFolder = new File(Environment.getExternalStorageDirectory(),
			"/media/audio/ringtones");
	/**
	 * Android's custom notification ringtone folder.<br>
	 * %sdcard%/media/audio/notifications
	 */
	static public final File customNotificationFolder = new File(Environment.getExternalStorageDirectory(),
			"/media/audio/notifications");
	/**
	 * Android's custom alarm ringtone folder.<br>
	 * %sdcard%/media/audio/alarms
	 */
	static public final File customAlarmFolder = new File(Environment.getExternalStorageDirectory(),
			"/media/audio/alarms");

	/**
	 * Tests the existance of anAudioFile in the given audioFolder.
	 *
	 * @param audioFolder - one of the following folders:
	 * <li>{@link com.blackmoonit.androidbits.filesystem.BitsFileUtils#customRingtoneFolder}</li>
	 * <li>{@link com.blackmoonit.androidbits.filesystem.BitsFileUtils#customNotificationFolder}</li>
	 * <li>{@link com.blackmoonit.androidbits.filesystem.BitsFileUtils#customAlarmFolder}</li>
	 * @param anAudioFile - audio file object
	 * @return Returns true if the audio file exists in the audioFolder, false otherwise.
	 */
	static public boolean customAudioExists(File audioFolder,File anAudioFile) {
		File f = new File(audioFolder,anAudioFile.getName());
		return (f!=null && f.exists());
	}

	/**
	 * Checks the file to see if it is just symbolic and pointing somewhere else.
	 * False positive may occur if the path includes a ".." as part of it.
	 *
	 * @param aFile - file to test
	 * @return Returns true if the file actually references a different path location.
	 */
	static public boolean isFileJumpPoint(File aFile) {
		try {
			return !aFile.getAbsolutePath().equals(BitsFileUtils.getCanonicalPath(aFile));
		} catch (NullPointerException npe) {
			return false;
		}
	}

	/**
	 * Return true if the folder is empty or if the file object is a file.
	 *
	 * @param aFolder - folder to test
	 * @return Returns true for files and empty folders, else false.
	 */
	static public boolean isFolderEmpty(File aFolder) {
		String[] folderContents = (aFolder!=null)?aFolder.list():null;
		if (folderContents!=null)
			return folderContents.length==0;
		else
			return true;
	}

	/**
	 * Go through the file list and figure out the deepest common folder between them.
	 *
	 * @param aFileList - list of either Uri, File, or String elements
	 * @return Returns a string consisting of the deepest common folder path. May be "" but never null.
	 */
	static public String deepestCommonFolder(List<?> aFileList) {
		String theResult = null;
		String theParent;
		if (aFileList!=null) {
			for (int i=0; i<aFileList.size(); i++) {
				Object theNextItem = aFileList.get(i);
				File theFile;
				if (theNextItem instanceof Uri) {
					theFile = new File(((Uri)theNextItem).getPath());
				} else if (theNextItem instanceof File) {
					theFile = (File)theNextItem;
				} else if (theNextItem instanceof String) {
					theFile = new File((String)theNextItem);
				} else {
					theFile = null; //list of unknown type, cannot process it
				}
				if (theFile!=null) {
					theParent = theFile.getParent();
					if (theResult!=null) {
						try {
							while (!theParent.startsWith(theResult)) {
								theResult = theResult.substring(0,theResult.lastIndexOf(File.separator,theResult.length()-1));
							}
						} catch (Exception e) {
							theResult = "";
						}
					} else
						theResult = theParent;
				}
			}
		}
		if (theResult==null)
			theResult = "";
		return theResult;
	}

	/**
	 * Update the file's last modified timestamp with current time.
	 * @param aFile - file to touch
	 * @param aTouchIfOlderThanThisTime - only update the time to now if older than this param,
	 * 0 means always update.
	 */
	static public boolean touchFile(File aFile, long aTouchIfOlderThanThisTime) {
		try {
			long theNow = System.currentTimeMillis();
			if ((theNow - aFile.lastModified())>aTouchIfOlderThanThisTime) {
				return aFile.setLastModified(theNow);
			} else {
				return true;
			}
		} catch (IllegalArgumentException iae) {
			return false;
		} catch (SecurityException se) {
			return false;
		} catch (NullPointerException npe) {
			return false;
		}
	}

	/**
	 * Given a file/folder, determine how much free space is left on that path/device.
	 * @param aFile - file/folder where free space info is wanted
	 * @return Returns amount of free space or NULL if there was a problem.
	 */
	static public Long freeSpace(File aFile) {
		if (aFile==null)
			return null;
		if (!aFile.isDirectory())
			aFile = aFile.getParentFile();
		if (aFile.isDirectory() && !aFile.canWrite())
			return null;
		try {
			StatFs theStats = new StatFs(aFile.getPath());
			if (theStats!=null) {
				long theFreeSpace = theStats.getAvailableBlocks();
				int theBlockSize = theStats.getBlockSize();
				//having the * on same line above gave terrible results when > max_int
				return theFreeSpace*theBlockSize;
			}
		} catch (IllegalArgumentException e) {
			//let default return value be returned
		}
		return null;
	}

	/**
	 * 2Gb limit, return the file as a byte[].
	 * @param aFile - file to read in.
	 * @return Returns the entire file as a loaded byte[].
	 * @throws IOException
	 */
	static public byte[] getFobAsIntByteArray(File aFile) throws IOException {
		RandomAccessFile theFob = new RandomAccessFile(aFile.getPath(), "r");
		byte[] theResult = new byte[(int)theFob.length()];
		theFob.read(theResult);
		return theResult;
	}

}

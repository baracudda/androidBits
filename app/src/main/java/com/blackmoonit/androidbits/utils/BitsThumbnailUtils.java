package com.blackmoonit.androidbits.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.StatFs;

import com.blackmoonit.androidbits.content.BitsIntent;
import com.blackmoonit.androidbits.filesystem.MIMEtypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Static class for thumbnail utility functions.
 *
 * @author Ryan Fischbach
 */
@TargetApi(10)
public final class BitsThumbnailUtils {
	static private Method mVideo_GetFrameAtTime = null;
	static public File mThumbnailCacheFolder = null;
	static public final String DEFAULT_THUMBNAIL_FILE_PREFIX = "thumb";
	static public String mThumbnailFilePrefix = DEFAULT_THUMBNAIL_FILE_PREFIX;
	static public final long THUMBNAIL_TOUCH_FREQUENCY = 1000*60*60*24; //miliseconds in 1 day


	/**
	 * Get a representative thumbnail of a video file.
	 * @param aFile - file to determine what icon to use
	 * @return Returns the Drawable thumbnail.
	 */
	static public Bitmap getVideoThumbnail(File aFile) {
		return getVideoThumbnail(aFile, 0);
	}

	/**
	 * Get a representative thumbnail of a video file.
	 * @param aFile - file to determine what icon to use
	 * @param aIconSize - scale the thumbnail to this square size
	 * @return Returns the Drawable thumbnail.
	 */
	static public Bitmap getVideoThumbnail(File aFile, int aIconSize) {
		Bitmap theResult = null;
		//available for all Android versions, but different methods as of API 10 (2.3.3)
		MediaMetadataRetriever theRetriever = new MediaMetadataRetriever();
		try {
			if (mVideo_GetFrameAtTime==null) {
				try { //API 10+
					mVideo_GetFrameAtTime = theRetriever.getClass().getMethod("getFrameAtTime");
				} catch (NoSuchMethodException nsme) {
					//failure, must be older device
					try { //API >4, <10
						mVideo_GetFrameAtTime = theRetriever.getClass().getMethod("captureFrame");
					} catch (NoSuchMethodException e) {
						//leave method as null
					}
				}
			}
			if (mVideo_GetFrameAtTime!=null) {
				theRetriever.setDataSource(aFile.getPath());
				try {
					theResult = (Bitmap)mVideo_GetFrameAtTime.invoke(theRetriever);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			try {
				theRetriever.release();
				theRetriever = null;
			} catch (RuntimeException ex) {
				return null;
			}
		}
		if (theResult!=null && aIconSize>0 && theResult.getHeight()>aIconSize) {
			theResult = Bitmap.createScaledBitmap(theResult,aIconSize,aIconSize,true);
		}
		return theResult;
	}

	/**
	 * Get the most appropriate icon for the given file.
	 *
	 * @param aContext - Context required to retrieve package info
	 * @param aMimeMap - MIME map used to determine MIME types
	 * @param aFile - file to determine what icon to use
	 * @param aFileRes - resource for default file icon, 0 if not used
	 * @param aFolderRes - resource for default directory icon, 0 if not used
	 * @param aZipRes - resource for default zip icon, 0 if not used
	 * @param bLoadImageIcon - determines whether to create image icons or not (this setting is
	 * useful if this function is used for non-standard icons, with standard ones using URI
	 * method of reference instead of image data directly, in that case Res params = 0 and this
	 * would be false)
	 * @return Returns the Drawable icon.
	 */
	static public Drawable getFileIcon(Context aContext, MIMEtypeMap aMimeMap, File aFile,
			int aFileRes, int aFolderRes, int aZipRes, boolean bLoadImageIcon) {
		return getFileIcon(aContext, aMimeMap, aFile, aFileRes, aFolderRes, aZipRes, bLoadImageIcon, 0, 1);
	}

	/**
	 * Get the most appropriate icon for the given file.
	 *
	 * @param aContext - Context required to retrieve package info
	 * @param aMimeMap - MIME map used to determine MIME types
	 * @param aFile - file to determine what icon to use
	 * @param aFileRes - resource for default file icon, 0 if not used
	 * @param aFolderRes - resource for default directory icon, 0 if not used
	 * @param aZipRes - resource for default zip icon, 0 if not used
	 * @param bLoadImageIcon - determines whether to create image icons or not (this setting is
	 * useful if this function is used for non-standard icons, with standard ones using URI
	 * method of reference instead of image data directly, in that case Res params = 0 and this
	 * would be false)
	 * @param aIconSize - size of the icon desired, 0 means default size.
	 * @param aScale - thumbnails come in different sizes, 1 means default size.
	 * @return Returns the Drawable icon.
	 */
	static public Drawable getFileIcon(Context aContext, MIMEtypeMap aMimeMap, File aFile,
			int aFileRes, int aFolderRes, int aZipRes, boolean bLoadImageIcon, int aIconSize, int aScale) {
		Drawable theResult = null;
		if (aContext!=null && aMimeMap!=null && aFile!=null) {
			String theMIMEtype = aMimeMap.guessMIMEtype(aFile);
			String theMIMEcategory = aMimeMap.getMIMEcategory(theMIMEtype);
			try {
				if (theMIMEtype!=null) {
					int theIconSize = (aIconSize==0) ? BitsGraphicsUtils.getIconSize(aContext,48) : aIconSize;
					if (theMIMEtype.equals("application/vnd.android.package-archive")) {
						PackageInfo pi = aContext.getPackageManager()
							.getPackageArchiveInfo(aFile.getPath(),PackageManager.GET_ACTIVITIES);
						pi.applicationInfo.sourceDir = aFile.getPath();
						pi.applicationInfo.publicSourceDir = aFile.getPath();
						theResult =  aContext.getPackageManager().getApplicationIcon(pi.applicationInfo);
						if (theResult!=null && (theResult.getIntrinsicHeight()>theIconSize) &&
								(theResult instanceof BitmapDrawable)) {
							theResult = new BitmapDrawable(Bitmap.createScaledBitmap(((BitmapDrawable)theResult).
									getBitmap(),theIconSize,theIconSize,true));
						}
						pi = null;
					} else if (theMIMEtype.equals("application/zip")) {
						if (aZipRes!=0)
							theResult = aContext.getResources().getDrawable(aZipRes);
					} else if (theMIMEcategory.equals("image/*") && aFile.isFile() && bLoadImageIcon) {
						theResult = loadThumbnailFromCache(aFile,aScale);
						if (theResult==null || (theResult.getIntrinsicHeight()>theIconSize) ) {
							theResult = new BitmapDrawable(BitsGraphicsUtils.getImage(aContext,aFile,-1,
									theIconSize,theIconSize,true));
						}
					} else if (theMIMEcategory.equals("video/*") && aFile.isFile() && bLoadImageIcon) {
						theResult = loadThumbnailFromCache(aFile,aScale);
						if (theResult==null || (theResult.getIntrinsicHeight()>theIconSize) ) {
							theResult = new BitmapDrawable(getVideoThumbnail(aFile,theIconSize));
						}
					} else {
						Intent theIntent = new Intent(Intent.ACTION_VIEW);
						theIntent.setDataAndType(BitsIntent.getViewFileUri(aFile), theMIMEtype);
						try {
							theResult =  aContext.getPackageManager().getActivityIcon(theIntent);
						} catch (Exception e) {
							theResult = null;
						}
						theIntent = null;
					}
				}
			} catch (Exception e) {
				//getting the icon failed, no reason we need to cry about it.
				theResult = null;
			} catch (OutOfMemoryError oom) {
				//oops, the file is too big to load in this process
				try {
					theResult = ((BitmapDrawable)aContext.getResources().getDrawable(
							BitsGraphicsUtils.getDrawableResId(aContext, "icon_image_too_big")));
				} catch (Exception e) {
					theResult = null;
				}
			}
			if (theResult==null) {
				if (aFile.isDirectory() && aFolderRes!=0)
					theResult = aContext.getResources().getDrawable(aFolderRes);
				else if (aFileRes!=0)
					theResult = aContext.getResources().getDrawable(aFileRes);
			}
		}
		return theResult;
	}

	/**
	 * Returns the File object used to access the cache file.
	 * @param aFile - Image file to determine cache file object
	 * @param aScale - thumbnails come in different sizes
	 * @return Returns File object representing the cached thumbnail. If aFile is in cache folder already,
	 * then aFile is returned.
	 */
	static public File getThumbnailCacheFile(File aFile, int aScale) {
		if (mThumbnailCacheFolder!=null && aFile!=null && !mThumbnailCacheFolder.equals(aFile.getParentFile()))
			return new File(mThumbnailCacheFolder,aScale+mThumbnailFilePrefix+Long.toHexString(aFile.hashCode())+".png");
		else
			return (mThumbnailCacheFolder!=null)?aFile:null;
	}

	/**
	 * Given a BitmapDrawable thumbnail of aFile, create the thumbnail file in aCacheFolder.
	 * Thumbnails are stored as PNG.
	 * @param aCacheFile - file object where the thumbnail will be stored.
	 * @param aThumbnail - BitmapDrawable of the thumbnail.
	 * @return Returns TRUE if succeeded, otherwise FALSE.
	 */
	static public boolean createThumbnailCacheFile(File aCacheFile, BitmapDrawable aThumbnail) {
		if (aThumbnail!=null && aCacheFile!=null) {
			FileOutputStream out = null;
			try {
				aCacheFile.deleteOnExit();
				out = new FileOutputStream(aCacheFile);
				if (!aThumbnail.getBitmap().compress(CompressFormat.PNG,100,out))
					throw new IOException();
				out.flush();
				out.close();
				return true;
			} catch (Exception e) {
				out = null;
				if (aCacheFile!=null)
					aCacheFile.delete();
				return false;
			}
		}
		return false;
	}

	/**
	 * Returns the File object used to access the cache file.
	 * @param aFile - Image file to determine cache file object
	 * @param aScale - thumbnails come in different sizes
	 */
	static public void removeThumbnailCacheFile(File aFile, int aScale) {
		File tempFile = getThumbnailCacheFile(aFile,aScale);
		if (tempFile!=null && tempFile.exists()) {
			tempFile.delete();
		}
	}

	/**
	 * Returns the drawable saved in thumbnail cache, if it exists, for the file passed in.
	 * @param aFile - file that may have a cached thumbnail
	 * @return Returns the drawable containing the thumbnail.
	 */
	static public Drawable loadThumbnailFromCache(File aFile, int aScale) {
		File theThumbnailFile = getThumbnailCacheFile(aFile,aScale);
		if (theThumbnailFile!=null && theThumbnailFile.exists() && theThumbnailFile.canRead()) {
			try {
				if (theThumbnailFile.canWrite()) {
					//touch the file so compactFolder will avoid deleting from staleness
					Long theNow = System.currentTimeMillis();
					if ((theNow - theThumbnailFile.lastModified())>THUMBNAIL_TOUCH_FREQUENCY) {
						theThumbnailFile.setLastModified(theNow);
					}
				}
				BitmapDrawable theThumbnail = new BitmapDrawable(theThumbnailFile.getPath());
				if (theThumbnail==null || theThumbnail.getBitmap()==null)
					throw new IOException();
				return theThumbnail;
			} catch (Exception e) {
				theThumbnailFile.delete(); //a problem with the thumbnail file? remove it
				return null;
			}
		}
		return null;
	}

	/**
	 * Save off the thumbnail for aFile in thumbnail cache, if defined.
	 * @param aFile - original file object the thumbnail represents.
	 * @param aThumbnail - thumbnail to be saved.
	 */
	static public void saveThumbnailToCache(File aFile, BitmapDrawable aThumbnail, int aScale) {
		try {
			File theCacheFile = getThumbnailCacheFile(aFile,aScale);
			if (theCacheFile!=null && aThumbnail!=null && isEnoughRoomForThumbnail(theCacheFile,
					aThumbnail.getBounds().width()*aThumbnail.getBounds().height()*4)) { //only need a maximally approx size
				createThumbnailCacheFile(theCacheFile,aThumbnail);
			}
		} catch (Exception e) {
			//don't care if fail
		}
	}

	/**
	 * Determine if there's enough room in the cache to store a thumbnail.
	 * @param aFile - file of thumbnail about to be saved
	 * @param aSize - size of the thumbnail to be saved
	 * @return Returns TRUE if a thumbnail can be saved, else FALSE
	 */
	static public boolean isEnoughRoomForThumbnail(File aFile, int aSize) {
		if (aFile==null)
			return false;
		if (!aFile.isDirectory())
			aFile = aFile.getParentFile();
		if (aFile.isDirectory() && !aFile.canWrite())
			return false;
		try {
			StatFs theStats = new StatFs(aFile.getPath());
			if (theStats!=null) {
				long theFreeSpace = theStats.getAvailableBlocks();
				int theBlockSize = theStats.getBlockSize();
				//having the * on same line above gave terrible results when > max_int
				return (theFreeSpace*theBlockSize>aSize*4); //only TRUE if we have a bit of elbow room
			}
		} catch (IllegalArgumentException e) {
			//return the default value
		}
		return true; //unless I know for sure I can't, assume we can
	}


}

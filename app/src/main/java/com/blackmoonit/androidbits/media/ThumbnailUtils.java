package com.blackmoonit.androidbits.media;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Static class for thumbnail utility functions.
 *
 * @author Ryan Fischbach<br>Blackmoon Info Tech Services - &copy;2010
 */
public final class ThumbnailUtils {
    static private Method mVideo_GetFrameAtTime = null;
    static public File mThumbnailCacheFolder = null;
    static public final String DEFAULT_THUMBNAIL_FILE_PREFIX = "thumb";
    static public String mThumbnailFilePrefix = DEFAULT_THUMBNAIL_FILE_PREFIX;
    static public final long THUMBNAIL_TOUCH_FREQUENCY = 1000*60*60*24; //milliseconds in 1 day


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
                if (Build.VERSION.SDK_INT<10)
                    theRetriever.setMode(MediaMetadataRetriever.MODE_CAPTURE_FRAME_ONLY);
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
                if (!aThumbnail.getBitmap().compress(Bitmap.CompressFormat.PNG,100,out))
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
    static public boolean isEnoughRoomForThumbnail(File aFile, long aSize) {
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
                long theBlockSize = theStats.getBlockSize();
                //having the * on same line above gave terrible results when > max_int
                return (theFreeSpace*theBlockSize>aSize*4); //only TRUE if we have a bit of elbow room
            }
        } catch (IllegalArgumentException e) {
            //return the default value
        }
        return true; //unless I know for sure I can't, assume we can
    }


}

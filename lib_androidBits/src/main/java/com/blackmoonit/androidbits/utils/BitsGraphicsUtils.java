package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2014 Blackmoon Info Tech Services
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.io.File;

/**
 * Graphic utility class of static functions.
 * @author baracudda
 */
public final class BitsGraphicsUtils {

	/**
	 * Converts the given DIPs into current device/resolution pixels.
	 * @param aContext - context required to retrieve device metrics
	 * @param aDip - DIP value to convert to pixels.
	 * @return Rerturns the pixel count for the given DIP.
	 */
	static public int dipsToPixels(Context aContext, int aDip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip,
				aContext.getResources().getDisplayMetrics());
		return Math.round(px);
	}

    static public Bitmap.Config getBitmapConfig(Bitmap aBitmap) {
        if (aBitmap!=null) {
            Bitmap.Config bmpCfg = aBitmap.getConfig();
            if (bmpCfg==null) {
                //Android 1.6 will run this code, but 2.2 won't on same png graphic… odd.
                bmpCfg = Bitmap.Config.ARGB_8888;
            }
            return bmpCfg;
        } else
            return null;
    }

    static public Bitmap tintBitmap(Context aContext, int aResId, int aTintColor) {
        Bitmap theSrc;
        try {
            theSrc = BitmapFactory.decodeResource(aContext.getResources(), aResId);
        } catch (NullPointerException npe) {
            return null;
        } catch (Resources.NotFoundException rnfe) {
            return null;
        }
        Bitmap theDst = Bitmap.createBitmap(theSrc.getWidth(),theSrc.getHeight(),getBitmapConfig(theSrc));
        Canvas theCanvas = new Canvas(theDst);
        Paint thePaint = new Paint();
        thePaint.setColorFilter(new PorterDuffColorFilter(0x7F000000+aTintColor, PorterDuff.Mode.SRC_ATOP));
        theCanvas.drawBitmap(theSrc,0f,0f,thePaint);
        return theDst;
    }

    static public Drawable scaleDrawable(BitmapDrawable aDrawable, int aNewWidth, int aNewHeight) {
        if (aDrawable==null)
            return null;
        if (aNewWidth<=0)
            aNewWidth = aDrawable.getIntrinsicWidth();
        if (aNewHeight<=0)
            aNewHeight = aDrawable.getIntrinsicHeight();
        if (aNewWidth<=0 || aNewHeight<=0)
            return aDrawable; //if we still don't have a proper width or height, nothing to do
        return new BitmapDrawable(Bitmap.createScaledBitmap(aDrawable.getBitmap(),aNewWidth,aNewHeight,true));
		/*
		Bitmap theDst = Bitmap.createBitmap(aNewWidth,aNewHeight,getBitmapConfig(theSrc));
		Canvas theCanvas = new Canvas(theDst);
		theCanvas.drawBitmap(theSrc,null,new Rect(0,0,aNewWidth,aNewHeight),null);
		return new BitmapDrawable(theDst);
		*/
    }

    /**
     * Icons seem to use a slightly different formula to figure out their scaled DIP size.
     * First, we round the density factor and then multiply that by the dip size.
     *
     * @param aContext - context for retrieving the density
     * @param aDIP - Device Independent Pixel size
     * @return Returns the icon size based on the current display density.
     */
    static public int getIconSize(Context aContext, int aDIP) {
        return aDIP*Math.max(1,Math.round(aContext.getResources().getDisplayMetrics().density));
    }

    /**
     * Round int to the nearest power of 2, 1 being the minimum.
     * @param i - integer to round
     * @return Returns 1, 2, 4, 8, 16, 32, 64… based on whichever i is nearest.
     */
    static public int round_power2(int i) {
        i--;
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        i++;
        return i;
    }

    /**
     * Read just enough of the image file to figure out the width and height.
     * @param aImageFile - image file
     * @return Returns the {@link android.graphics.BitmapFactory.Options} with the out params set. If aImageFile is null,
     * null is returned.
     */
    static public BitmapFactory.Options getImageFileResolution(File aImageFile) {
        if (aImageFile!=null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(aImageFile.getPath(),options);
            return options;
        } else
            return null;
    }

    /**
     * Compute the optimal bitmap reading sample size based on the thumbsize desired and the bitmap's
     * resolution.
     *
     * @param aImgSize - {@link android.graphics.BitmapFactory.Options} with the out params set
     * @param aOutputX - resulting thumbsize width
     * @param aOutputY - resulting thumbsize height
     * @param bSpeedOverQuality - round up the sample size to the nearest power of 2
     * @return Returns the computed sample size.
     */
    static public int computeSampleSize(BitmapFactory.Options aImgSize, int aOutputX, int aOutputY,
                                        boolean bSpeedOverQuality) {
        if (aImgSize!=null && aImgSize.outWidth>0 && aImgSize.outHeight>0 && aOutputX>0 && aOutputY>0) {
            //determine which dimension is reduced the most and use that as our sampling basis
            int theResult = Math.max(aImgSize.outWidth/aOutputX,aImgSize.outHeight/aOutputY);
            if (bSpeedOverQuality) {
                //round the result to the nearest power of 2
                theResult = round_power2(theResult);
            }
            return theResult;
        } else
            throw new IllegalArgumentException();
    }

    /**
     * Android bug where trying to load a really large image will either hang the phone or kill the app.
     * @param aContext - context needed to determine available memory
     * @param aImgSize - image dimensions as gotten from {@link #getImageFileResolution(java.io.File)}
     * @return Returns TRUE if the file is too large to load.
     */
	static public boolean isImageFileTooBig(Context aContext, BitmapFactory.Options aImgSize) {
		if (aContext!=null && aImgSize!=null) {
			int theMaxSize = BitsAppUtils.getMemoryClass(aContext);
			int theImageSize = aImgSize.outWidth*aImgSize.outHeight/1048576; //div 1meg
			return (theImageSize>=theMaxSize);
		} else
			return false;
	}

    static public int getDrawableResId(Context aContext, String aResName) {
        return aContext.getApplicationContext().getResources().getIdentifier(aResName,"drawable",
                aContext.getPackageName());
    }
    private static final byte[] tempStorageForImageLoad = new byte[1024*64];

    /**
     * Load a scaled image from the file system (handles large photos).
     *
     * @param aContext - context required to check for avail mem (protect against very large images)
     * @param aImageFile - File to load
     * @param aScaling - -1 for down only, 1 for up only, 0 for any
     * @param aOutputW - width of returned image, must be > 0
     * @param aOutputH - height of returned image, must be > 0
     * @param bRespectAspectRatioOfImage - TRUE will adjust W or H to match original image aspect ratio
     * @return Returns the image retrieved.
     */
    static public Bitmap getImage(Context aContext, File aImageFile, int aScaling,
                                  int aOutputW, int aOutputH, boolean bRespectAspectRatioOfImage) {
        if (aImageFile!=null && aImageFile.exists() && aImageFile.isFile() && aOutputW>0 && aOutputH>0) {
            BitmapFactory.Options theImgSize = getImageFileResolution(aImageFile);
            if (theImgSize!=null && !isImageFileTooBig(aContext,theImgSize)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = computeSampleSize(theImgSize,aOutputW,aOutputH,!bRespectAspectRatioOfImage);
                options.inTempStorage = tempStorageForImageLoad;
                //options.inDither = true;  //images look blurry if I set this to TRUE
                boolean bSkipScaling = false;
                if ((aScaling<0 && options.inSampleSize<1) || (aScaling>0 && options.inSampleSize>1)) {
                    options.inSampleSize = 1;
                    bSkipScaling = true;
                }
                Thread.yield();
                Bitmap bmOrig = BitmapFactory.decodeFile(aImageFile.getPath(),options);
                Thread.yield();
                if (!bSkipScaling && bmOrig!=null) {
                    //inSampleSize will bring us close to our thumbnail size, but we will need to
                    //  create a scaled bitmap to be accurate
                    int newW = aOutputW;
                    int newH = aOutputH;
                    if (bRespectAspectRatioOfImage) {
                        int origW = options.outWidth;
                        int origH = options.outHeight;
                        float p = Math.min((float)newW/origW,(float)newH/origH);
                        newW = Math.min(Math.round(origW*p),newW);
                        newH = Math.min(Math.round(origH*p),newH);
                    }
                    bmOrig = Bitmap.createScaledBitmap(bmOrig,newW,newH,true);
                    Thread.yield();
                }
                return bmOrig;
            } else if (theImgSize!=null) {
                try {
                    return ((BitmapDrawable)aContext.getResources().getDrawable(
                            getDrawableResId(aContext,"icon_image_too_big"))).getBitmap();
                } catch (Exception e) {
                    return null;
                }
            } else
                return null;
        } else
            return null;
    }

}

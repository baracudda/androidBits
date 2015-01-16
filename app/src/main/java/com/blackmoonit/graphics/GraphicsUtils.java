package com.blackmoonit.graphics;

import android.content.Context;
import android.util.TypedValue;

/**
 * Graphic utility class of static functions.
 * 
 */
public final class GraphicsUtils {

	/**
	 * Converts the given DIPs into current device/resolution pixels.
	 * @param aContext - context required to retrieve device metrics
	 * @param aDip - DIP value to convert to pixels.
	 * @return Rerturns the pixel count for the given DIP.
	 * @author Ryan Fischbach
	 */
	static public int dipsToPixels(Context aContext, int aDip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip, 
				aContext.getResources().getDisplayMetrics());
		return Math.round(px);
	}
	

}

package com.blackmoonit.androidbits.utils;

import android.graphics.Color;
import android.graphics.ColorMatrix;

/**
 * Color utility functions housed in a static class.
 *
 * @author Ryan Fischbach
 */
public final class BitsColorUtils {

	private BitsColorUtils() {} //do not instanciate this class

	static public int floatToColor(float aColorValue) {
        int theColorInt = Math.round(aColorValue);
        return intToColor(theColorInt);
    }

    static public int intToColor(int aColorValue) {
        if (aColorValue < 0) {
        	aColorValue = 0;
        } else if (aColorValue > 255) {
        	aColorValue = 255;
        }
        return aColorValue;
    }

    static public int interpolateColorPiece(int aStartValue, int aEndValue, float aPercent) {
    	//aStartvalue and aEndValue are alpha/r/g or b values.
    	//aPercent is a value from [0..1) used to determine the exact value desired
        return aStartValue + Math.round((aEndValue-aStartValue)*aPercent);
    }

    static public int interpolateColor(int aColorSet[], float aUnit) {
        if (aUnit <= 0) {
            return aColorSet[0];
        } else if (aUnit >= 1) {
            return aColorSet[aColorSet.length-1];
        }

        float p = aUnit*(aColorSet.length-1);
        int i = (int)p;	//functional equivalent to DIV
        p -= i;			//functional equivalent to MOD

        // now p is just the fractional part [0…1) and i is the index
        int c0 = aColorSet[i];
        int c1 = aColorSet[i+1];
        int a = interpolateColorPiece(Color.alpha(c0),	Color.alpha(c1),p);
        int r = interpolateColorPiece(Color.red(c0),	Color.red(c1),	p);
        int g = interpolateColorPiece(Color.green(c0),	Color.green(c1),p);
        int b = interpolateColorPiece(Color.blue(c0),	Color.blue(c1),	p);

        return Color.argb(a,r,g,b);
    }

	static public int rotateColor(int aColor, float rad) {
        float deg = rad*180/(float)Math.PI;
        int or = Color.red(aColor);
        int og = Color.green(aColor);
        int ob = Color.blue(aColor);

        ColorMatrix cm1 = new ColorMatrix();
        ColorMatrix cm2 = new ColorMatrix();
        cm1.setRGB2YUV();
        cm2.setRotate(0, deg);
        cm1.postConcat(cm2);
        cm2.setYUV2RGB();
        cm1.postConcat(cm2);

        final float[] ca = cm1.getArray();
        int nr = BitsColorUtils.floatToColor((ca[ 0]*or)+(ca[ 1]*og)+(ca[ 2]*ob));
        int ng = BitsColorUtils.floatToColor((ca[ 5]*or)+(ca[ 6]*og)+(ca[ 7]*ob));
        int nb = BitsColorUtils.floatToColor((ca[10]*or)+(ca[11]*og)+(ca[12]*ob));

        return Color.argb(Color.alpha(aColor),nr,ng,nb);
    }

}

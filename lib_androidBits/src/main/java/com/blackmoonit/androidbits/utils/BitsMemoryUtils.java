package com.blackmoonit.androidbits.utils;

import com.vladium.utils.IObjectProfileNode;
import com.vladium.utils.ObjectProfiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This class provides utility methods for reporting and converting memory
 * measurements. As a static class,
 * {@code BitsMemoryUtils} cannot be instantiated.
 */
public final class BitsMemoryUtils {

    private BitsMemoryUtils() {} //do not instantiate this class

    /**
     * Determines size of object, in bytes, via serialization. Utilizes
     * Android ObjectOutputStream, see
     * {@code https://developer.android.com/reference/java/io/ObjectOutputStream.html}.
     * There's some caveats, here; serialization won't keep track of transient
     * variables, and the default serialization method writes strings in UTF-8,
     * so any ANSI characters will only take one byte. However, despite these
     * stipulations this approach still serves as a valid estimation of object
     * size in most use cases.
     * @param object Object to get size of.
     * @return Returns approximate size of object, in bytes. Returns 0 if object
     * is null or problems are encountered.
     */
    public static int getSize( final Object object )
    {
        if( object == null )
            return 0 ;
        ByteArrayOutputStream ba_OStream = new ByteArrayOutputStream() ;
        ObjectOutputStream obj_OStream = null ;
        try
        {
            obj_OStream = new ObjectOutputStream( ba_OStream ) ;
            obj_OStream.writeObject( object ) ;
            obj_OStream.flush() ;
            obj_OStream.close() ;
        }
        catch ( IOException e ) { e.printStackTrace() ; }

        return ba_OStream.size() ;
    }

    /**
     * ObjectProfiler's approach is not perfect. Besides the already explained
     * ignorance of memory alignment, another obvious problem with it is that
     * Java object instances can share nonstatic data, such as when instance
     * fields point to global singletons and other shared content.
     *
     * Please see
     * {@code http://www.javaworld.com/article/2077408/core-java/sizeof-for-java.html}
     * for more information.
     *
     * @param object Object to get size of.
     * @return Returns approximate size of object, in bytes.
     */
    public static int getDeepSize( final Object object )
    {
//        Field[] objectFields = object.getClass().getFields();
//        Field[] declaredObjectFields = object.getClass().getDeclaredFields();

        IObjectProfileNode profile = ObjectProfiler.profile( object ) ;
        return profile.size() ;
    }

    /**
     * Converts bytes to kilobytes. Rounds to two decimal points, and considers
     * the magnitudes involved to be in terms memory. Ergo, 1 Kb = 1024B.
     * @param bytes Byte value to convert to kilobytes.
     * @return The kilobyte equivalent of bytes.
     */
    public static double convertToKb( final int bytes )
    {
        /** conversionFactor
         * is dependent on context of Decimal or Binary, ergo SI units or
         * in terms of memory. 1024 would be for memory considerations,
         * 1000 for SI units.
         * https://en.wikipedia.org/wiki/Template:Quantities_of_bytes
         */
        final double conversionFactor = 1024d ;

        /** numDecimalsToRound
         * represents the number of decimals to round the answer to.
         */
        final double numDecimalsToRound = 2d;

        /** roundingMagnitude
         * value should not be changed.
         */
        final double roundingMagnitude = Math.pow(10d, numDecimalsToRound) ;

        return (double)Math.round( ( bytes / conversionFactor ) *
                roundingMagnitude ) / roundingMagnitude ;
    }

    public static String getReadableSize ( final Object object ) {
        StringBuilder sb = new StringBuilder();
        final int objectSizeB = getSize(object);
        final double objectSizeKb = convertToKb(objectSizeB);
        sb.append(objectSizeKb);
        sb.append(" Kb, (");
        sb.append(objectSizeB);
        sb.append(" bytes)");
        return sb.toString();
    }

    public static String getReadableDeepSize ( final Object object ) {
        StringBuilder sb = new StringBuilder();
        final int objectSizeB = getDeepSize(object);
        final double objectSizeKb = convertToKb(objectSizeB);
        sb.append(objectSizeKb);
        sb.append(" Kb, (");
        sb.append(objectSizeB);
        sb.append(" bytes)");
        return sb.toString();
    }
}

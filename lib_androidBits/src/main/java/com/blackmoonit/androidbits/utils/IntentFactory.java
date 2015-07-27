package com.blackmoonit.androidbits.utils;

import android.content.Context;
import android.content.Intent;

/**
 * For any Android class that expects to receive intents, implement this class
 * to provide tailor-made {@link Intent}s to consumers, which can trigger
 * specific actions in the enclosing class.
 */
public abstract class IntentFactory
{
    /** Compatibility: Provides intent category for apps prior to API 15. */
    public static final String CATEGORY_APP_MESSAGING =
        "android.intent.category.APP_MESSAGING" ;

    /** Should not be instantiated. */
    protected IntentFactory() {}

    /**
     * If a context is provided, then the intent can be bound directly to the
     * class that intends to catch it. Otherwise, you still get an intent back,
     * but it doesn't have the convenience of pre-binding.
     * @param ctx the context of the consumer
     * @param clsProcessor the class that intends to catch the intent; the
     *                     intent will be bound automatically to that class
     * @return an intent that might or might not be bound to the enclosing class
     */
    protected static Intent getBoundIntent( Context ctx, Class<?> clsProcessor )
    {
        if( ctx != null )
            return new Intent( ctx, clsProcessor ) ;
        else
            return new Intent() ;
    }
}

package com.blackmoonit.androidbits.utils;
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

import android.content.Context;
import android.content.Intent;

/**
 * For any Android class that expects to receive intents, implement this class
 * to provide tailor-made {@link Intent}s to consumers, which can trigger
 * specific actions in the enclosing class. The methods in the implementation
 * classes can use the {@link #getBoundIntent} method provided by this class to
 * create an intent that is automatically bound to the enclosing class. Any
 * descendant implementation of this class <b>should</b> contain a method
 * {@code getBoundIntent(Context)} which simply makes reference to this two-arg
 * method but specifically passes in the enclosing class as the binding class.
 *
 * <h2>Example with an Android Service</h2>
 *
 * <p>Consider a {@code FooService} which expects to receive specific
 * actions corresponding to constants {@code ACTION_FOO_DO_THIS} and
 * {@code ACTION_FOO_DO_THAT}. You would create those action constants publicly,
 * and then implement an inner class extension of {@code IntentFactory} with
 * public static functions {@code doThis(Context)} and {@code doThat(Context)}.
 * where the incoming context is relevant to the caller.</p>
 *
 * <pre>
 * public class FooService extends Service
 * {
 *     public static final String ACTION_FOO_DO_THIS =
 *         "com.mydomain.services.foo.FOO_DO_THIS" ;
 *     public static final String ACTION_FOO_DO_THAT =
 *         "com.mydomain.services.foo.FOO_DO_THAT" ;
 *
 *     // Define your factory as an inner class.
 *     public static class IntentFactory
 *     extends com.blackmoonit.androidbits.utils.IntentFactory
 *     {
 *         // This could be protected, or public to provide a nice shorthand.
 *         public static Intent getBoundIntent( Context ctx )
 *         { return getBoundIntent( ctx, FooService.class ) ; }
 *
 *         // Define methods that provide intents for your service's actions.
 *         public static Intent doThis( Context ctx )
 *         { return getBoundIntent( ctx ).setAction( ACTION_FOO_DO_THIS ) ; }
 *         public static Intent doThat( Context ctx )
 *         { return getBoundIntent( ctx ).setAction( ACTION_FOO_DO_THAT ) ; }
 *     }
 *
 *     public void handleCommand( Intent in )
 *     {
 *         // Responds to actions ACTION_FOO_DO_THIS and ACTION_FOO_DO_THAT...
 *     }
 *
 *     // etc.
 * }
 * </pre>
 *
 * <p>You can now trigger specific actions in the service by using these
 * custom-designed intents.</p>
 *
 * <pre>
 * // Call the "do this" function...
 * Context ctx = this.getContext() ;
 * ctx.startService( FooService.IntentFactory.doThis(ctx) ) ;
 * // Call the "do that" function...
 * ctx.startService( FooService.IntentFactory.doThat(ctx) ) ;
 * </pre>
 *
 * <h2>Example with a Broadcast Receiver</h2>
 *
 * <p>A {@link android.content.BroadcastReceiver} can similarly provide a
 * factory for all broadcasts that it expects to receive.</p>
 *
 * <pre>
 * public class FooReceiver extends BroadcastReceiver
 * {
 *     public static final String ACTION_FOO_DO_STUFF =
 *         "com.mydomain.foo.DO_STUFF" ;
 *     public static final String EXTRA_THINGS =
 *         "com.mydomain.foo.DO_STUFF.WITH_THINGS" ;
 *
 *     public static class IntentFactory
 *     extends com.blackmoonit.androidbits.utils.IntentFactory
 *     {
 *         public static Intent getBoundIntent( Context ctx )
 *         { return getBoundIntent( ctx, FooReceiver.class ) ; }
 *
 *         public static Intent doStuff( Context ctx, Parcelable pclThings )
 *         {
 *             return getBoundIntent( ctx )
 *                 .setAction( ACTION_FOO_DO_STUFF )
 *                 .putExtra( EXTRA_THINGS, pclThings )
 *                 ;
 *         }
 *     }
 *
 *     public void onReceive( Context ctx, Intent in )
 *     {
 *         switch( in.getAction() )
 *         {
 *             case ACTION_FOO_DO_STUFF:
 *                 this.processThings( in.getParcelableExtra( EXTRA_THINGS ) ) ;
 *                 break ;
 *             // etc.
 *         }
 *         // etc.
 *     }
 * }
 * </pre>
 *
 * <p>The consumer then notifies the service</p>
 *
 * <h2>Custom-Designed Intents</h2>
 *
 * <p>The methods that provide the custom {@code Intent} instances can also
 * assign any additional properties that are relevant to the intent, such as an
 * intent category, extras from the enclosing class, etc. This further takes the
 * burden away from the class that is generating the intent. Since these are
 * custom functions, you are free to design them to take additional parameters
 * as well &mdash; that is, if the consumer needs to provide some data to the
 * intent in addition to what the factory's enclosing class could provide, then
 * you can have that data come in as additional parameters of your factory
 * method. This greatly simplifies the programming task of generating intents
 * for your service or receiver, and presents a more intuitive interface for
 * other developers to interact with your intent-receiving class.</p>
 *
 * <h2>Providing Intents for Standard Actions</h2>
 *
 * <p>While intended primarily as a way to provide custom-designed
 * {@code Intent} instances, this class could also provide intents that use
 * "standard" action constants, but are still bound to the enclosing class.
 * For example, if you have a service that wants to do something special with an
 * {@code ACTION_GET_CONTENT} intent, then your factory implementation can
 * present a public method {@code doSomethingSpecial()} that returns an intent
 * with that standard action but which is still bound to your specific service.
 * The {@code Intent} instances returned by your factory methods need not be
 * restricted to use of custom-defined actions; they can, and should, cover the
 * whole range of actions that the enclosing class wants to intercept.</p>
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

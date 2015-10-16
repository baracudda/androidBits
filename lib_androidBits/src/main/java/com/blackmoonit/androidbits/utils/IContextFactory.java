package com.blackmoonit.androidbits.utils;

import android.content.Context;

/**
 * Generic factory which requires a context to instantiate.
 */
public interface IContextFactory<T> {

	/**
	 * Factory method that creates instance of class T.
	 * @param aContext - the context to use.
	 * @return Returns a new instance of class T.
	 */
	T newInstance(Context aContext);

}

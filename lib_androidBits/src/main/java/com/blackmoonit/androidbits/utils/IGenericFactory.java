package com.blackmoonit.androidbits.utils;

/**
 * Generic factory which does not require any parameters.
 */
public interface IGenericFactory<T> {

	/**
	 * Factory method that creates instance of class T.
	 * @return Returns a new instance of class T.
	 */
	T newInstance();

}

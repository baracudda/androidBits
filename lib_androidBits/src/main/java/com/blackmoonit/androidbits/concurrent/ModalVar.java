package com.blackmoonit.androidbits.concurrent;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sometimes interaction is required in the middle of a task. Automate the
 * process of interrupting the task and waiting for a result to be
 * obtained from the user (and/or another thread).
 * Once obtained, it can be remembered for future occurances (default)
 * or it can be reset after the first read so that any future
 * read will cause the value to be obtained again.<br>
 * NULL is used as the wait condition, so it is the only value that cannot be
 * represented by this variable.
 *
 * @param <V> - class of the value desired
 *
 * @author Ryan Fischbach
 */
public class ModalVar<V> {
	/**
	 * Context used to run the UI events on the UI thread.
	 */
	protected final WeakReference<Activity> wAct;
	/**
	 * Critical section gateway used to enforce the asynchronous read behavior.
	 */
	protected final ReentrantLock mCheckDecision = new ReentrantLock();
	/**
	 * Blocking condition that will block any read attempt until a {@link #setValue(V)}
	 * has been executed.
	 */
	protected final Condition mObtainDecision  = mCheckDecision.newCondition();
	/**
	 * Variable used to store the actual value of the ModalVar.
	 */
	protected V mDecisionValue = null;
	/**
	 * When TRUE, will cause {@link #mDecisionValue to become NULL again after
	 * the first {@link #getValue()} finishes.
	 */
	protected boolean bResetOnRead = false;
	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * The event will be run on the same thread as getValue().
	 */
	protected Runnable mOnObtainDecision = null;
	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * The event will be run on the UI thread.
	 */
	protected Runnable mOnObtainDecisionUI = null;
	/**
	 * Event that fires on {@link #setValue(V aValue)}, after any waiting thread is signaled.
	 * The event will only fire once the value changes from NULL to a non-null value
	 * The event will be run on the same thread as setValue().
	 */
	protected Runnable mOnSetValue = null;
	/**
	 * Event that fires on {@link #setValue(V aValue)}, after any waiting thread is signaled.
	 * The event will be run on the UI thread.
	 */
	protected Runnable mOnSetValueUI = null;
	/**
	 * If a timeout is desired, set the TimeOut.UNITS.
	 */
	public TimeUnit mTimeOutUnits = null;
	/**
	 * If a timeout is desired, set the amount of time to wait.
	 */
	public Long mTimeOutAmount = null;
	/**
	 * If a timeout is desired, optionally provide a runnable to fire on timeout.
	 */
	public Runnable mTimeOutEvent = null;
	/**
	 * If a timeout is desired, optionally set the desired ModalVar value.
	 */
	public V mTimeOutValue = null;

	/**
	 * Default constructor, isResetOnRead is false.
	 */
	public ModalVar() {
		this(null);
	}

	/**
	 * Default constructor if the OnX UI events are going to be used, isResetOnRead is false.
	 * @param aAct - Activity used to run the UI events on the UI thread. May be NULL.
	 */
	public ModalVar(Activity aAct) {
		wAct = (aAct!=null)?new WeakReference<Activity>(aAct):null;
	}

	/**
	 * Constructor to use if you want to also specify isResetOnRead to TRUE.
	 * @param aAct - Activity used to run the UI events on the UI thread. May be NULL.
	 * @param aResetOnRead - if TRUE, resets value to NULL after a successful {@link #getValue()}.
	 */
	public ModalVar(Activity aAct, boolean aResetOnRead) {
		this(aAct);
		setResetOnRead(aResetOnRead);
	}

	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * Useful if aValue needs to obtain its value from a background thread and maintain
	 * that value over the course of a loop mechanism.
	 * The event will be run on the same thread as getValue().
	 * @return Returns the event handler Runnable that is being used.
	 */
	public Runnable getOnObtainValue() {
		return mOnObtainDecision;
	}

	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * Useful if aValue needs to obtain its value from a background thread and maintain
	 * that value over the course of a loop mechanism.
	 * The event will be run on the same thread as getValue().
	 */
	public void setOnObtainValue(Runnable aOnObtainValue) {
		mOnObtainDecision = aOnObtainValue;
	}

	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * Useful if aValue needs to obtain its value from the user via a dialog and maintain
	 * that value over the course of a loop mechanism.
	 * The event will be run on the UI thread.
	 * @return Returns the event handler Runnable that is being used.
	 */
	public Runnable getOnObtainValueUI() {
		return mOnObtainDecisionUI;
	}

	/**
	 * Event that fires on {@link #getValue()} if {@link #mDecisionValue} is NULL.
	 * Useful if aValue needs to obtain its value from the user via a dialog and maintain
	 * that value over the course of a loop mechanism.
	 * The event will be run on the UI thread.
	 */
	public void setOnObtainValueUI(Runnable aOnObtainValueUI) {
		mOnObtainDecisionUI = aOnObtainValueUI;
	}

	/**
	 * Event that fires on {@link #setValue(V aValue)} if {@link #mDecisionValue} is NULL.
	 * Useful if setting a value needs to fire off something to be done in the background.
	 * The event will be run on the same thread as setValue().
	 * @return Returns the event handler Runnable that is being used.
	 */
	public Runnable getOnSetValue() {
		return mOnSetValue;
	}

	/**
	 * Event that fires on {@link #setValue(V aValue)} if {@link #mDecisionValue} is NULL.
	 * Useful if setting a value needs to fire off something to be done in the background.
	 * The event will be run on the same thread as setValue().
	 */
	public void setOnSetValue(Runnable aOnSetValue) {
		mOnSetValue = aOnSetValue;
	}

	/**
	 * Event that fires on {@link #setValue(V aValue)} if {@link #mDecisionValue} is NULL.
	 * Useful if setting a value needs to fire off something to be done in the main UI.
	 * The event will be run on the UI thread.
	 * @return Returns the event handler Runnable that is being used.
	 */
	public Runnable getOnSetValueUI() {
		return mOnSetValueUI;
	}

	/**
	 * Event that fires on {@link #setValue(V aValue)} if {@link #mDecisionValue} is NULL.
	 * Useful if setting a value needs to fire off something to be done in the main UI.
	 * The event will be run on the UI thread.
	 */
	public void setOnSetValueUI(Runnable aOnSetValueUI) {
		mOnSetValueUI = aOnSetValueUI;
	}

	/**
	 * @return Returns TRUE if {@link #getValue()} will reset the internal value back to NULL after
	 * the first successful read. Useful if you want to wait for an answer from a dialog and then reset
	 * the value immediately after so a later event will force another dialog to be shown.
	 */
	public boolean isResetOnRead() {
		return bResetOnRead;
	}

	/**
	 * Sets the value for {@link #isResetOnRead()}.
	 * @param aResetOnRead - boolean value
	 * @see #isResetOnRead()
	 */
	public void setResetOnRead(boolean aResetOnRead) {
		bResetOnRead = aResetOnRead;
	}

	/**
	 * Resets internal mechanisms to behave as if newly created again, causing it to wait
	 * for a new value to be set.
	 */
	public void reset() {
		setValue(null);
	}

	/**
	 * Checks to see if a getValue() would force a wait or not.
	 * @return Returns TRUE if getValue() would return immediately.
	 */
	public boolean isValueSet() {
		return (mDecisionValue!=null);
	}

	/**
	 * Forces calling thread to wait until a value is obtained. If a value is already set,
	 * no waiting occurs. NULL value only returned if thread was interrupted.
	 * @return Returns the value if set, otherwise it blocks the thread until it is set.
	 * NULL is returned if the thread has been interrupted.
	 */
	public V getValue() {
		mCheckDecision.lock();
		try {
			//if we do not have a value yet, obtain one if event is defined
			if (mDecisionValue==null) {
				if (mOnObtainDecision!=null)
					mOnObtainDecision.run();
				if (mOnObtainDecisionUI!=null && wAct!=null) {
					Activity theAct = wAct.get();
					if (theAct!=null)
						theAct.runOnUiThread(mOnObtainDecisionUI);
				}
			}

			//await() will release the lock on mCheckDecision and sleep the current thread
			//wait for value to be obtained
			while (mDecisionValue==null) {
				//false positive signalling may occur, so a while loop is used to be absolutely sure
				if (mTimeOutUnits==null) {
					mObtainDecision.await();
				} else {
					if (!mObtainDecision.await(mTimeOutAmount, mTimeOutUnits)) {
						if (mTimeOutEvent!=null)
							mTimeOutEvent.run();
						if (mTimeOutValue!=null)
							mDecisionValue = mTimeOutValue;
					}
				}
			}
			//we have a value, return it
			V theResult = mDecisionValue;
			if (isResetOnRead())
				reset();
			return theResult;
		} catch (InterruptedException e) {
			return null;
		} finally {
			mCheckDecision.unlock();
		}
	}

	/**
	 * Sets the value to this parameter and signals those waiting to read it.
	 * @param aValue - value to set. Note that if the value is NULL, it will
	 * reset the ModalVar and not signal any waiting objects. Also, if the param
	 * is equal to what is already set, nothing will be signaled and no events fired.
	 */
	public void setValue(V aValue) {
		mCheckDecision.lock();
		try {
			if (aValue!=null && !aValue.equals(mDecisionValue)) {
				mDecisionValue = aValue;
				//fire the events if any were defined
				if (mOnSetValue!=null)
					mOnSetValue.run();
				if (mOnSetValueUI!=null && wAct!=null) {
					Activity theAct = wAct.get();
					if (theAct!=null)
						theAct.runOnUiThread(mOnSetValueUI);
				}
				//signal all waiting reader threads, if any
				mObtainDecision.signalAll();
			} else if (aValue==null) {
				mDecisionValue = null;
			}
		} finally {
			mCheckDecision.unlock();
		}
	}

	/**
	 * Determines if aTestObject is a ModalVar and that its interval value type is aValueClass.
	 * @param aTestObject - object to test
	 * @param aValueClass - class of ModalVar to test against
	 * @return Returns TRUE iff aTestObject is an instanceof ModalVar&lt;aValueClass&gt;
	 */
	static public boolean isModalVar(Object aTestObject, Class<?> aValueClass) {
		if (aTestObject instanceof ModalVar<?>) {
			if (((ModalVar<?>)aTestObject).mDecisionValue!=null)
				return ((ModalVar<?>)aTestObject).mDecisionValue.getClass().isAssignableFrom(aValueClass);
			else
				return true;
		} else
			return false;
	}

	/**
	 * Test and cast an object into the desired type if it is the correct type.
	 * @param aTestObject - object to test
	 * @param aValueClass - class of ModalVar to test against
	 * @return Returns aTestObject cast as ModalVar&lt;V&gt; if they are the same type, otherwise NULL.
	 */
	@SuppressWarnings("unchecked")
	static public <V> ModalVar<V> castModalVar(Object aTestObject, Class<V> aValueClass) {
		return (isModalVar(aTestObject,aValueClass)) ? (ModalVar<V>)aTestObject : null;
	}

}

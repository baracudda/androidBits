package com.blackmoonit.androidbits.filesystem;
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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;
import java.util.UUID;

/**
 * Handler containing specific values and functions related to generic progress bar UI handling.
 * Supported Progress Event: ActionText (Event title), Overall Progress Bar, Overall Text,
 * Item Progress Bar, Item Text, Cancel Progress mechanism. The UI implementation of these elements
 * is left for decendant classes.
 *
 * @author Ryan Fischbach
 */
public abstract class ProgressBarHandler extends Handler {
	public static final int MSG_PROGRESS_START = 1;
	public static final int MSG_PROGRESS_FINISH = 2;
	public static final int MSG_PROGRESS_CANCEL = 3;
	public static final int MSG_PROGRESS_INC_TOTAL_VALUE = 4;
	public static final int MSG_PROGRESS_TOTAL_UPDATE = 5;
	public static final int MSG_PROGRESS_ITEM_START = 6;
	public static final int MSG_PROGRESS_ITEM_UPDATE = 7;
	public static final int MSG_PROGRESS_ITEM_FINISH = 8;
	/**
	 * Start additional messages starting with this value and increasing.
	 */
	public static final int MSG_USER_FIRST = 9;

	/**
	 * Data bundle key for passing text via a progress message.
	 */
	public static final String KEY_PROGRESS_TEXT = "progress_text";
	/**
	 * Data bundle key for passing a long value via a progress message.
	 */
	public static final String KEY_PROGRESS_AMOUNT = "progress_amount";
	/**
	 * Data bundle key for passing a throwable object via a progress message.
	 */
	public static final String KEY_PROGRESS_CANCEL_MSG = "progress_cancel_msg";

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_PROGRESS_ITEM_UPDATE:
				onProgressItemUpdate(msg);
				break;
			case MSG_PROGRESS_ITEM_START:
				onProgressItemStart(msg);
				break;
			case MSG_PROGRESS_ITEM_FINISH:
				onProgressItemFinish(msg);
				break;
			case MSG_PROGRESS_TOTAL_UPDATE:
				onProgressTotalUpdate(msg);
				break;
			case MSG_PROGRESS_INC_TOTAL_VALUE:
				onProgressTotalIncMax(msg);
				break;
			case MSG_PROGRESS_START:
				onProgressStart(msg);
				break;
			case MSG_PROGRESS_CANCEL:
				onProgressCancel(msg);
				//don't break here, want it to fall into the MSG_PROGRESS_FINISH code
			case MSG_PROGRESS_FINISH:
				onProgressFinish(msg);
				break;
		}
	}

	public abstract void onProgressStart(Message msg);
	public abstract void onProgressTotalIncMax(Message msg);
	public abstract void onProgressTotalUpdate(Message msg);
	public abstract void onProgressCancel(Message msg);
	public abstract void onProgressFinish(Message msg);
	public abstract void onProgressItemStart(Message msg);
	public abstract void onProgressItemUpdate(Message msg);
	public abstract void onProgressItemFinish(Message msg);

	/**
	 * Get the UUID stored within the message. (msg.obj)
	 * @param aMsg
	 * @return
	 */
	public static UUID getProgressID(Message aMsg) {
		return (aMsg!=null)?(UUID)aMsg.obj:null;
	}

	/**
	 * Get the value stored within the message encoded as a long value using the key
	 * {@link #KEY_PROGRESS_AMOUNT} or arg2.
	 *
	 * @param aMsg - the message
	 * @return Returns the value stored with {@link #KEY_PROGRESS_AMOUNT} or arg2 if no data exists.
	 */
	public static long getProgressAmount(Message aMsg) {
		if (aMsg!=null && aMsg.peekData()!=null) {
			return aMsg.getData().getLong(KEY_PROGRESS_AMOUNT,aMsg.arg2);
		} else {
			return aMsg.arg2;
		}
	}

	/**
	 * Get the text stored within the message encoded as a string using the key
	 * {@link #KEY_PROGRESS_TEXT}.
	 *
	 * @param aMsg - the message
	 * @return Returns the text stored with {@link #KEY_PROGRESS_TEXT} or null if no data exists.
	 */
	public static String getProgressText(Message aMsg) {
		if (aMsg!=null && aMsg.peekData()!=null) {
			return aMsg.getData().getString(KEY_PROGRESS_TEXT);
		} else {
			return null;
		}
	}

	public static Serializable getProgressCancelMsg(Message aMsg) {
		if (aMsg!=null && aMsg.peekData()!=null) {
			return aMsg.getData().getSerializable(KEY_PROGRESS_CANCEL_MSG);
		} else {
			return null;
		}
	}

	/**
	 * Constructs the metadata needed to start/update/finish|cancel a progress event.
	 * An event consists of a title, an overall progress bar, a specific item progress bar, and a means to
	 * cancel the event. This base class avoids any specific UI so that tasks need not worry about it and
	 * the specifics of a UI can be customized.
	 * @param aActionTextResID - string resource for the action being tracked
	 * @return Returns an Object which identifies this particular progress event.
	 */
	public Object createNewProgressEvent(int aActionTextResID) {
		return UUID.randomUUID();
	}

	/**
	 * Sends the message immediately and yields the thread to give the UI a chance to respond.
	 * @param aMsg - message to send.
	 */
	public static void sendProgressMsg(Message aMsg) {
		if (aMsg!=null) {
			aMsg.sendToTarget();
			Thread.yield();
		}
	}

	/**
	 * Initialize a newly Created progress event with specifics like text and size.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aTotalText - string displayed for the overall progress event
	 * @param aTotalSize - size of the event to track (can be increased at a later time)
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressStart(Object aProgressID, String aTotalText, Long aTotalSize) {
		Message theMsg = obtainMessage(MSG_PROGRESS_START,aProgressID);
		Bundle theBundle = new Bundle();
		if (aTotalText!=null)
			theBundle.putString(KEY_PROGRESS_TEXT,aTotalText);
		if (aTotalSize!=null)
			theBundle.putLong(ProgressBarHandler.KEY_PROGRESS_AMOUNT,aTotalSize);
		theMsg.setData(theBundle);
		return theMsg;
	}

	/**
	 * Returns the message used to cancel a progress event before it completes.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressCancel(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_CANCEL,aProgressID);
	}

	/**
	 * Returns the message used to cancel a progress event before it completes.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aExtraData - custom data the handler may wish to know.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressCancel(Object aProgressID, Serializable aExtraData) {
		Message theMsg = getMsgProgressCancel(aProgressID);
		Bundle theBundle = new Bundle();
		theBundle.putSerializable(KEY_PROGRESS_CANCEL_MSG,aExtraData);
		theMsg.setData(theBundle);
		return theMsg;
	}

	/**
	 * Returns the message used to end a progress event.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressFinish(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_FINISH,aProgressID);
	}

	/**
	 * Returns the message used to update the overall progress by aIncrement and set new item text.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aProgressItemText - text to show as the new item being processed.
	 * @param aIncrement - amount to increment total completed, null means no increment.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressTotalUpdate(Object aProgressID, String aProgressItemText, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_TOTAL_UPDATE,aProgressID);
		Bundle theBundle = new Bundle();
		theBundle.putString(ProgressBarHandler.KEY_PROGRESS_TEXT,aProgressItemText);
		if (aIncrement!=null)
			theBundle.putLong(ProgressBarHandler.KEY_PROGRESS_AMOUNT,aIncrement);
		theMsg.setData(theBundle);
		return theMsg;
	}

	/**
	 * Send a message to increase the overall progress max size without affecting progress
	 * towards that max amount.
	 * Example usage: When it is discovered that a subfolder needs to be processed,
	 * use this message to increase to total size of work that needs to be done.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aIncrement - amount to increment total size
	 */
	public Message getMsgProgressIncreaseTotal(Object aProgressID, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_INC_TOTAL_VALUE,aProgressID);
		if (aIncrement!=null) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(ProgressBarHandler.KEY_PROGRESS_AMOUNT,aIncrement);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to reset item progress with a new max value.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aItemSize - new max value.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemStart(Object aProgressID, Long aItemSize) {
		Message theMsg = obtainMessage(MSG_PROGRESS_ITEM_START,aProgressID);
		if (aItemSize!=null && aItemSize!=-1L) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(ProgressBarHandler.KEY_PROGRESS_AMOUNT,aItemSize);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to update the item progress amount.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @param aIncrement - amount to move the item progress towards item max value.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemUpdate(Object aProgressID, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_ITEM_UPDATE,aProgressID);
		if (aIncrement!=null) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(ProgressBarHandler.KEY_PROGRESS_AMOUNT,aIncrement);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to complete the current item progress, but not the overall event.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent(int)}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemFinish(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_ITEM_FINISH,aProgressID);
	}

}

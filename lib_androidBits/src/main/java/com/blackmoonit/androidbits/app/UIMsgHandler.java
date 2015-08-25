package com.blackmoonit.androidbits.app;
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

import java.io.Serializable;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Handler containing specific values and functions related to generic toast & progress bar UI handling.
 * Supported Progress Events: ActionText (Event title), Overall Progress Bar, Overall Text,
 * Item Progress Bar, Item Text, Cancel Progress mechanism. The UI implementation of these elements
 * is left for decendant classes that wish to implement the nested interfaces.
 *
 * @author Ryan Fischbach
 */
public abstract class UIMsgHandler extends Handler {
	public static final int MSG_TOAST = 1;
	public static final int MSG_PROGRESS_START = 2;
	public static final int MSG_PROGRESS_FINISH = 3;
	public static final int MSG_PROGRESS_CANCEL = 4;
	public static final int MSG_PROGRESS_INC_TOTAL_VALUE = 5;
	public static final int MSG_PROGRESS_TOTAL_UPDATE = 6;
	public static final int MSG_PROGRESS_ITEM_START = 7;
	public static final int MSG_PROGRESS_ITEM_UPDATE = 8;
	public static final int MSG_PROGRESS_ITEM_FINISH = 9;

	/**
	 * Start additional messages starting with this value and increasing.
	 */
	public static final int MSG_USER_FIRST = 10;

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

	/**
	 * A Handler implementing this interface can handle receiving toast messages.
	 */
	public interface ToastMsgs {
		//get msgs to send
		public Message getMsgToast(String aStr);
		public Message getMsgToast(Integer aResourceID);

		//handle received msgs
		public void onMsgToast(Message aMsg);
	}

	/**
	 * A Handler implementing this interface can handle receiving progress bar messages.
	 */
	public interface ProgressBarMsgs {
		//get msgs to send
		public Message getMsgProgressStart(Object aProgressID, String aTotalText, Long aTotalSize);
		public Message getMsgProgressCancel(Object aProgressID);
		public Message getMsgProgressCancel(Object aProgressID, Serializable aExtraData);
		public Message getMsgProgressFinish(Object aProgressID);
		public Message getMsgProgressTotalUpdate(Object aProgressID, String aProgressItemText, Long aIncrement);
		public Message getMsgProgressIncreaseTotal(Object aProgressID, Long aIncrement);
		public Message getMsgProgressItemStart(Object aProgressID, Long aItemSize);
		public Message getMsgProgressItemUpdate(Object aProgressID, Long aIncrement);
		public Message getMsgProgressItemFinish(Object aProgressID);

		//handle received msgs
		public void onMsgProgressStart(Message aMsg);
		public void onMsgProgressTotalIncMax(Message aMsg);
		public void onMsgProgressTotalUpdate(Message aMsg);
		public void onMsgProgressCancel(Message aMsg);
		public void onMsgProgressFinish(Message aMsg);
		public void onMsgProgressItemStart(Message aMsg);
		public void onMsgProgressItemUpdate(Message aMsg);
		public void onMsgProgressItemFinish(Message aMsg);
	}

	@Override
	public void handleMessage(Message aMsg) {
		ToastMsgs theToastHandler = (ToastMsgs)((this instanceof ToastMsgs)?this:null);
		ProgressBarMsgs thePbHandler = (ProgressBarMsgs)((this instanceof ProgressBarMsgs)?this:null);

		if (aMsg.what==MSG_TOAST && theToastHandler!=null) {
			theToastHandler.onMsgToast(aMsg);
			return;
		} else if (thePbHandler!=null) {
			switch (aMsg.what) {
				case MSG_PROGRESS_ITEM_UPDATE:
					thePbHandler.onMsgProgressItemUpdate(aMsg);
					return;
				case MSG_PROGRESS_ITEM_START:
					thePbHandler.onMsgProgressItemStart(aMsg);
					return;
				case MSG_PROGRESS_ITEM_FINISH:
					thePbHandler.onMsgProgressItemFinish(aMsg);
					return;
				case MSG_PROGRESS_TOTAL_UPDATE:
					thePbHandler.onMsgProgressTotalUpdate(aMsg);
					return;
				case MSG_PROGRESS_INC_TOTAL_VALUE:
					thePbHandler.onMsgProgressTotalIncMax(aMsg);
					return;
				case MSG_PROGRESS_START:
					thePbHandler.onMsgProgressStart(aMsg);
					return;
				case MSG_PROGRESS_CANCEL:
					thePbHandler.onMsgProgressCancel(aMsg);
					//don't break here, want it to fall into the MSG_PROGRESS_FINISH code
				case MSG_PROGRESS_FINISH:
					thePbHandler.onMsgProgressFinish(aMsg);
					return;
			}//end switch
		}
		super.handleMessage(aMsg);
	}

	/*========================================================================
	 * Toast related methods
	 *========================================================================*/

	/**
	 * Returns the message used to display a toast string.
	 * @param aStr - string to display
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgToast(String aStr) {
		return obtainMessage(MSG_TOAST,aStr);
	}

	/**
	 * Returns the message used to display a toast string.
	 * @param aResourceID - resource ID of the string to display.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgToast(Integer aResourceID) {
		return obtainMessage(MSG_TOAST,aResourceID,0);
	}

	/**
	 * Standard toast message handling. Two ways to display a message:
	 * 1: msg.obj is a string with msg.arg2 an optional Toast.LENGTH_* constant.
	 * 2: msg.arg1 is a Resource ID with msg.arg2 an optional Toast.LENGTH_* constant.
	 * @param aContext - the UI context.
	 * @param aMsg - the message being handled.
	 */
	static public void onMsgToast(Context aContext, Message aMsg) {
		String theMsg = null;
		int theDuration = Toast.LENGTH_LONG;
		try {
			if (aMsg.arg2!=0) {
				theDuration = aMsg.arg2;
			}
			if (aMsg.obj instanceof String) {
				theMsg = (String)aMsg.obj;
			} else {
				theMsg = aContext.getString(aMsg.arg1);
			}
		} catch (Exception e) {
			return; //no msg to display, just exit now
		}
		if (theMsg!=null) {
			Toast.makeText(aContext,theMsg,theDuration).show();
		}
	}

	/*========================================================================
	 * Progress Bar related methods (can only really implement the send msg half)
	 *========================================================================*/

	/**
	 * Get the UUID stored within the message. (msg.obj)
	 * @param aMsg
	 * @return
	 */
	static public UUID getProgressID(Message aMsg) {
		return (aMsg!=null)?(UUID)aMsg.obj:null;
	}

	/**
	 * Get the value stored within the message encoded as a long value using the key
	 * {@link #KEY_PROGRESS_AMOUNT} or arg2.
	 *
	 * @param aMsg - the message
	 * @return Returns the value stored with {@link #KEY_PROGRESS_AMOUNT} or arg2 if no data exists.
	 */
	static public long getProgressAmount(Message aMsg) {
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
	static public String getProgressText(Message aMsg) {
		if (aMsg!=null && aMsg.peekData()!=null) {
			return aMsg.getData().getString(KEY_PROGRESS_TEXT);
		} else {
			return null;
		}
	}

	/**
	 * Initialize a newly Created progress event with specifics like text and size.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
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
			theBundle.putLong(KEY_PROGRESS_AMOUNT,aTotalSize);
		theMsg.setData(theBundle);
		return theMsg;
	}

	/**
	 * Returns the message used to cancel a progress event before it completes.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressCancel(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_CANCEL,aProgressID);
	}

	/**
	 * Returns the message used to cancel a progress event before it completes.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
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
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressFinish(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_FINISH,aProgressID);
	}

	/**
	 * Returns the message used to update the overall progress by aIncrement and set new item text.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @param aProgressItemText - text to show as the new item being processed.
	 * @param aIncrement - amount to increment total completed, null means no increment.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressTotalUpdate(Object aProgressID, String aProgressItemText, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_TOTAL_UPDATE,aProgressID);
		Bundle theBundle = new Bundle();
		theBundle.putString(KEY_PROGRESS_TEXT,aProgressItemText);
		if (aIncrement!=null)
			theBundle.putLong(KEY_PROGRESS_AMOUNT,aIncrement);
		theMsg.setData(theBundle);
		return theMsg;
	}

	/**
	 * Send a message to increase the overall progress max size without affecting progress
	 * towards that max amount.
	 * Example usage: When it is discovered that a subfolder needs to be processed,
	 * use this message to increase to total size of work that needs to be done.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @param aIncrement - amount to increment total size
	 */
	public Message getMsgProgressIncreaseTotal(Object aProgressID, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_INC_TOTAL_VALUE,aProgressID);
		if (aIncrement!=null) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(KEY_PROGRESS_AMOUNT,aIncrement);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to reset item progress with a new max value.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @param aItemSize - new max value.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemStart(Object aProgressID, Long aItemSize) {
		Message theMsg = obtainMessage(MSG_PROGRESS_ITEM_START,aProgressID);
		if (aItemSize!=null && aItemSize!=-1L) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(KEY_PROGRESS_AMOUNT,aItemSize);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to update the item progress amount.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @param aIncrement - amount to move the item progress towards item max value.
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemUpdate(Object aProgressID, Long aIncrement) {
		Message theMsg = obtainMessage(MSG_PROGRESS_ITEM_UPDATE,aProgressID);
		if (aIncrement!=null) {
			Bundle theBundle = new Bundle();
			theBundle.putLong(KEY_PROGRESS_AMOUNT,aIncrement);
			theMsg.setData(theBundle);
		}
		return theMsg;
	}

	/**
	 * Returns the message used to complete the current item progress, but not the overall event.
	 * @param aProgressID - progress event ID (result of {@link #createNewProgressEvent()}).
	 * @return Returns the Message to be sent.
	 */
	public Message getMsgProgressItemFinish(Object aProgressID) {
		return obtainMessage(MSG_PROGRESS_ITEM_FINISH,aProgressID);
	}

}

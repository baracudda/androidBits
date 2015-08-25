package com.blackmoonit.androidbits.dialog;
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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.blackmoonit.androidbits.app.AppPreferenceBase;

/**
 * Standard confirmation dialog.
 */
public class DialogConfirm extends Dialog implements View.OnClickListener {
	private static final String TAG = "BITS.lib.dialog.DialogConfirm";

	protected static int getResId(Context aContext, String aResType, String aResName) {
		return aContext.getApplicationContext().getResources().getIdentifier(aResName,aResType,
				aContext.getPackageName());
	}

	protected int getResId(String aResType, String aResName) {
		return getResId(getContext(),aResType,aResName);
	}

	protected int mDialogLayoutResId = 0;
	protected int mMsgTextViewResId = 0;
	protected int mPositiveButtonResId = 0;
	protected int mNegativeButtonResId = 0;

	private void getDefaultResIds() {
		String thePackageName = getContext().getPackageName();
		Resources theRes = getContext().getResources();
		mDialogLayoutResId = theRes.getIdentifier("dialog_confirm","layout",thePackageName);
		mMsgTextViewResId = theRes.getIdentifier("dialog_confirm_message","id",thePackageName);
		mPositiveButtonResId = theRes.getIdentifier("ButtonPositive","id",thePackageName);
		mNegativeButtonResId = theRes.getIdentifier("ButtonNegative","id",thePackageName);
	}

	//fields
	protected int mFontSize = 0;
	protected boolean bSetupCalled = false;
	protected View.OnClickListener mPositiveListener = null;
	protected View.OnClickListener mNegativeListener = null;

	/**
	 * Quick confirmation dialog using standard layouts, text and graphics.
	 * Please note that the dialog will call dismiss() on itself before the onClick is handled.
	 * This prevents the user from clicking multiple times which would cause multiple onClicks.
	 * <br><br>Usage:<pre>
	 * final DialogConfirm dConfirm = new DialogConfirm(getContext());
	 * View.OnClickListener onHandleClick = new View.OnClickListener() {
	 * 	&#64;Override
	 * 	public void onClick(View v) {
	 * 		//do something
	 * 	};
	 * };
	 * dConfirm.setupOkCancel(R.string.dialog_confirm_my_title,
	 * 		getContext().getString(R.string.dialog_confirm_my_message),
	 * 		onHandleClick).show();
	 * </pre>
	 * @param aContext - context
	 */
	public DialogConfirm(Context aContext) {
		super(aContext);
		getDefaultResIds();
		getPrefs(AppPreferenceBase.getPrefs(getContext()));
	}

	protected void getPrefs(SharedPreferences aSettings) {
		if (aSettings!=null) {
			//app wide UI prefs would go here, like FontSize.
        }
	}

	/**
	 * Override the default layout and identify the pertinent parts.
	 * @param aLayoutResId - layout to use.
	 * @param aMsgResId - TextView for the text message.
	 * @param aPosButtonId - Ok button.
	 * @param aNegButtonId - Cancel button.
	 * @return
	 */
	public DialogConfirm setLayout(int aLayoutResId, int aMsgResId, int aPosButtonId, int aNegButtonId) {
		if (bSetupCalled)
			throw new IllegalStateException(TAG+": setup*() has already been called.");
		mDialogLayoutResId = aLayoutResId;
		mMsgTextViewResId = aMsgResId;
		mPositiveButtonResId = aPosButtonId;
		mNegativeButtonResId = aNegButtonId;
		return this;
	}

	/**
	 * Convenience funtion for calling {@link #setup()} with some standard inputs.
	 * Android dialog Alert icon, and R.layout.dialog_confirm are used with this method.
	 * @param aTitleResId - dialog title resource ID
	 * @param aMsg - dialog message
	 * @param aPositiveListener - onClick handler
	 */
	public DialogConfirm setup(int aTitleResId, String aMsg, View.OnClickListener aPositiveListener) {
		return setup(android.R.drawable.ic_dialog_alert,aTitleResId,aMsg,aPositiveListener,null);
	}

	/**
	 * Required method before it can be shown.
	 * @param aIconResId - dialog icon resource ID (optional)
	 * @param aTitleResId - title resource ID (optional)
	 * @param aMsg - message to display
	 * @param aPositiveListener - positive button onClick handler
	 * @param aNegativeListener - negative button onClick handler (optional)
	 */
	public DialogConfirm setup(int aIconResId, int aTitleResId, String aMsg,
			View.OnClickListener aPositiveListener, View.OnClickListener aNegativeListener) {

		if (aTitleResId!=0) {
			setTitle(aTitleResId);
			if (aIconResId!=0)
				requestWindowFeature(Window.FEATURE_LEFT_ICON);
		} else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		setContentView(mDialogLayoutResId);
		if (aIconResId!=0)
			setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,aIconResId);

		mPositiveListener = aPositiveListener;
		mNegativeListener = aNegativeListener;

		TextView tv = (TextView)findViewById(mMsgTextViewResId);
		tv.setText(aMsg);
		if (mFontSize>0)
			tv.setTextSize(mFontSize);

		Button bv = (Button)findViewById(mPositiveButtonResId);
		if (mFontSize>0)
			bv.setTextSize(mFontSize);
		bv.setOnClickListener(this);

		bv = (Button)findViewById(mNegativeButtonResId);
		if (mFontSize>0)
			bv.setTextSize(mFontSize);
		bv.setOnClickListener(this);

		bSetupCalled = true;
		return this;
	}

	/**
	 * OnClick will dismiss the dialog right away and then call the appropriate button handler.
	 */
	public void onClick(View v) {
		dismiss();
		Thread.yield(); //make sure the jump dialog goes away
		int theViewId = v.getId();
		if (theViewId==mPositiveButtonResId) {
			if (mPositiveListener!=null)
				mPositiveListener.onClick(v);
		} else if (theViewId==mNegativeButtonResId) {
			if (mNegativeListener!=null)
				mNegativeListener.onClick(v);
		}
	}

	@Override
	public void show() {
		if (bSetupCalled)
			super.show();
		else
			throw new IllegalStateException(TAG+": setup*() required before show() can be called.");
	}


}

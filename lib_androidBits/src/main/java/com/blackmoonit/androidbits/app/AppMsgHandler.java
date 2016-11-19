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

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * Implement ToastMsgs interface and provide easy mechanism to handle progress msgs too.
 *
 * @author baracudda
 */
@SuppressWarnings("unused")
public class AppMsgHandler extends UIMsgHandler implements UIMsgHandler.ToastMsgs {
	protected WeakReference<Context> wrContext;

	/**
	 * Message Handler for Activities to handle toast msgs & progress bar msgs in a standard way.
	 * @param aContext - UI context of the activity.
	 */
	public AppMsgHandler(Context aContext) {
		super();
		setContext(aContext);
	}

	/**
	 * Build-chain friendly method to set the context reference.
	 * @param aContext - the context to use.
	 * @return Returns THIS for chaining.
	 */
	public AppMsgHandler setContext(Context aContext) {
		wrContext = new WeakReference<Context>(aContext);
		return this;
	}

	public Context getContext() {
		return wrContext.get();
	}

	/**
	 * Standard toast message handling. Two ways to display a message:
	 * 1: msg.obj is a string with msg.arg2 an optional Toast.LENGTH_* constant.
	 * 2: msg.arg1 is a Resource ID with msg.arg2 an optional Toast.LENGTH_* constant.
	 * @param aMsg - the message being handled.
	 */
	@Override
	public void onMsgToast(Message aMsg) {
		onMsgToast(getContext(),aMsg);
	}

	static public void sendToastMsg(Context aContext, String aStr) {
		if (aContext instanceof ContextCourier) {
			Handler theHandler = ((ContextCourier)aContext).getHandler();
			if (theHandler instanceof ToastMsgs) {
				((ToastMsgs)theHandler).getMsgToast(aStr).sendToTarget();
			}
		}
	}

	static public void sendToastMsg(Context aContext, int aResourceId) {
		sendToastMsg(aContext,aContext.getString(aResourceId));
	}

}

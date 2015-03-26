package com.blackmoonit.androidbits.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class AppIntro extends Dialog {
	protected int mLayoutRes = 0;
	protected int mAnimRes = 0;
	protected Animation mIntroAnim = null;
	protected View mLayout = null;

	public AppIntro(Context aContext, int aLayoutRes, int aAnimRes) {
		super(aContext);
		mLayoutRes = aLayoutRes;
		mAnimRes = aAnimRes;
	}

	@Override
	protected void onCreate(Bundle aSavedState) {
		super.onCreate(aSavedState);
		mLayout = LayoutInflater.from(getContext()).inflate(mLayoutRes,null);
		mLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppIntro.this.dismiss();
			}
		});
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(mLayout);
		mIntroAnim = AnimationUtils.loadAnimation(getContext(),mAnimRes);
		mIntroAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				//nothing to do
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				//nothing to do
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				AppIntro.this.dismiss();
			}
		});
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		dismiss();
		return true;
	}

	@Override
	public void show() {
		super.show();
		mLayout.startAnimation(mIntroAnim);
	}


}

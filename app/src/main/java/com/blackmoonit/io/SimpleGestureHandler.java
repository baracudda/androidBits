package com.blackmoonit.io;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.blackmoonit.graphics.GraphicsUtils;

/**
 * Simple swipe & scroll handler containing a detector with listener.
 * 
 * Example usage in an Activity:
 * 
 * SimpleGestureHandler sgHandler = new SimpleGestureHandler(this, new OnSimpleGesture() {
         
          // add all required override interface methods here
          
		});
 * 
 */

public class SimpleGestureHandler implements OnTouchListener {

	public interface OnSimpleGesture
	{
		/**
		 * Handle the OnSwipe event.
		 * 
		 * @param aMotionEvent - beginning of swipe.
		 * @param aDirection - vector of the swipe, aDirection>0 is a swipe to the right.
		 * @return Return true if you wish to stop further processing of the touch event.
		 */
	    public boolean onSwipe(MotionEvent aMotionEvent, float aDirection);

	    /**
	     * Handle the OnScroll event.
	     * 
	     * @param aMotionEvent - beginning of scroll.
	     * @param aDirection - vector of the scroll, aDirection>0 is a scroll up.
	     * @return Return true if you wish to stop further processing of the touch event.
	     */
	    public boolean onScroll(MotionEvent aMotionEvent, float aDirection);
	    
	    /**
	     * Handle Mouse Buttons in event.
	     * 
	     * @param aMotionEvent - a button event occured.
	     * @param aButtonState - the current button state that prompted this event.
	     * @return Return true if you wish to stop further processing of the mouse event.
	     */
	    public boolean onMouseButton(MotionEvent aMotionEvent, int aButtonState);
	    
	    /**
	     * Handle Long Press in event.
	     * 
	     * @param aMotionEvent - a long press event occured.
	     * @return Return true if you wish to stop further processing of the mouse event.
	     */
	    public boolean onLongPress(MotionEvent aMotionEvent);
	}

	/**
	 * Gesture listener that recognizes horizontal swipes and vertical scrolls.
	 */
	 public class SimpleGestureListener extends SimpleOnGestureListener {
		private int mSwipeMinDistanceRequired = 150;
		private int mSwipeMaxRiseAllowed = 100;
		private int mSwipeThresholdVelocity = 80;
		private int mScrollMinDistanceRequired = 80;
		private int mScrollMaxRunAllowed = 20;
		private int mScrollThresholdVelocity = 40;
		protected OnSimpleGesture mGestureHandler = null;
		protected boolean bOnGestureResult = false;
		protected MotionEvent mLastOnDownEvent = null;

		public SimpleGestureListener(Context aContext, OnSimpleGesture aGestureHandler) {
			// commented out due to removal of Blackmoon imports
			
			mSwipeMinDistanceRequired = GraphicsUtils.dipsToPixels(aContext,mSwipeMinDistanceRequired);
			mSwipeMaxRiseAllowed = GraphicsUtils.dipsToPixels(aContext,mSwipeMaxRiseAllowed);
			mScrollMinDistanceRequired = GraphicsUtils.dipsToPixels(aContext,mScrollMinDistanceRequired);
			mScrollMaxRunAllowed = GraphicsUtils.dipsToPixels(aContext,mScrollMaxRunAllowed);
			if (aGestureHandler!=null)
				mGestureHandler = aGestureHandler;
			else
				throw new IllegalArgumentException("aGestureHandler cannot be null.");
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			mLastOnDownEvent = e; //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1==null)
				e1 = mLastOnDownEvent;
			if (e1==null || e2==null)
				return false;
			float dX = e2.getX()-e1.getX();
			float dY = e2.getY()-e1.getY();
			if (		Math.abs(dY)<mSwipeMaxRiseAllowed && 
						Math.abs(velocityX)>=mSwipeThresholdVelocity &&
						Math.abs(dX)>=mSwipeMinDistanceRequired ) {
				bOnGestureResult = mGestureHandler.onSwipe(e1,dX);
				return true;
			} else if (	Math.abs(dX)<mScrollMaxRunAllowed && 
						Math.abs(velocityY)>=mScrollThresholdVelocity &&
						Math.abs(dY)>=mScrollMinDistanceRequired ) {
				bOnGestureResult = mGestureHandler.onScroll(e1,dY);
				return true;
			}
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {			
			final int theButtonState = e.getButtonState();
			if (theButtonState!=0 && mGestureHandler.onMouseButton(e,theButtonState)) {
				return true;
			}
			return super.onSingleTapConfirmed(e);
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			if (!mGestureHandler.onLongPress(e))
				super.onLongPress(e);
		}
		
	}
	
	public static final int LIMIT_UNCHANGED = 0;
	protected final SimpleGestureListener mSimpleGestureListener;
	protected final GestureDetector mSimpleGestureDetector;
	
	public SimpleGestureHandler(Context aContext, OnSimpleGesture aGestureHandler) {
		mSimpleGestureListener = new SimpleGestureListener(aContext,aGestureHandler);
		mSimpleGestureDetector = new GestureDetector(aContext,mSimpleGestureListener);
	}

	/**
	 * 
	 * @see View.OnTouchListener
	 */
	public boolean onTouch(View v, MotionEvent aEvent) {
		if (mSimpleGestureDetector.onTouchEvent(aEvent))
			return mSimpleGestureListener.bOnGestureResult;
		else
			return false;
	}
	
	/**
	 * 
	 * @return Returns the GestureDetector being used.
	 */
	public GestureDetector getGestureDetector() {
		return mSimpleGestureDetector;
	}
	
	public void setSwipeLimits(int aMinDistance, int aMaxRise, int aMinVelocity) {
		if (aMinDistance>LIMIT_UNCHANGED)
			mSimpleGestureListener.mSwipeMinDistanceRequired = aMinDistance;
		if (aMaxRise>LIMIT_UNCHANGED)
			mSimpleGestureListener.mSwipeMaxRiseAllowed = aMaxRise;
		if (aMinVelocity>LIMIT_UNCHANGED)
			mSimpleGestureListener.mSwipeThresholdVelocity = aMinVelocity;		
	}
	
	public void setScrollLimits(int aMinDistance, int aMaxRun, int aMinVelocity) {
		if (aMinDistance>LIMIT_UNCHANGED)
			mSimpleGestureListener.mScrollMinDistanceRequired = aMinDistance;
		if (aMaxRun>LIMIT_UNCHANGED)
			mSimpleGestureListener.mScrollMaxRunAllowed = aMaxRun;
		if (aMinVelocity>LIMIT_UNCHANGED)
			mSimpleGestureListener.mScrollThresholdVelocity = aMinVelocity;				
	}
}
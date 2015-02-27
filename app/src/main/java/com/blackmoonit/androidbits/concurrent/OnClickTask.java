package com.blackmoonit.androidbits.concurrent;

import android.os.AsyncTask;
import android.view.View;

/**
 * OnClick drop-in replacement handler that utilizes a background task for its heavy work.
 * Non-trivial tasks should use this class instead of a typical inline OnClickListener so that
 * UI response time is not disrupted on potentially slow jobs - which would cause multiple click
 * events while the user keeps pressing the button/widget trying to make the UI respond.
 * Useful if you want cancellable slow tasks performed in the background.
 * @author Ryan Fischbach
 */
public class OnClickTask implements View.OnClickListener, Runnable {

	/**
	 * Simplifies use of OnClickTask by defining the interface of strictly necessary methods an
	 * inline definition would need to define in order for the task to be completed fairly easily.
	 * The complexities of how to define the AsyncTask appropriately can be avoided so that one
	 * can concentrate on defining "what button X should do" rather than how to get it done.
	 * beforeTask() equates to what would be run during onClick(), followed by the background
	 * thread being created and doTask() run inside that thread.  Once doTask() is finished,
	 * afterTask() is run on the UI thread again.
	 */
	static abstract public class TaskDef<TResultOfBeforeTask, TProgress, TResultOfDoTask> extends
			AsyncTask<View, TProgress, TResultOfDoTask> {
		OnClickTask myClickHandler = null;
		TResultOfBeforeTask mResultOfBeforeTask = null;

		public TaskDef() {
		}

		public TaskDef setClickHandler(OnClickTask aClickHandler) {
			myClickHandler = aClickHandler;
			return this;
		}

		public TaskDef(OnClickTask aClickHandler) {
			this();
			setClickHandler(aClickHandler);
		}

		@Override
		protected void onPreExecute() {
			mResultOfBeforeTask = beforeTask(myClickHandler.mViewClicked);
		}

		@Override
		protected TResultOfDoTask doInBackground(View... params) {
			View theView = null;
			if (params!=null && params.length>0)
				theView = params[0];
			return doTask(theView, mResultOfBeforeTask);
		}

		@Override
		protected void onProgressUpdate(TProgress... aProgress) {
			TProgress theObj = null;
			if (aProgress!=null && aProgress.length>0)
				theObj = aProgress[0];
			onTaskProgressUpdate(theObj);
		}

		@Override
		protected void onPostExecute(TResultOfDoTask aTaskResult) {
			afterTask(aTaskResult);
		}

		/**
		 * Portion of task that needs to be done on UI thread prior to doInBackground().
		 * @param v - the View that is firing the onClick event.
		 */
		abstract public TResultOfBeforeTask beforeTask(View v);

		/**
		 * Actual work to be done is performed off the UI thread.
		 * @param v - the View that is firing the onClick event.
		 * @param aBeforeTaskResult - the result of the beforeTask() call.
		 * @return Returns a TTaskResult which will be passed as a param into afterTask().
		 */
		abstract public TResultOfDoTask doTask(View v, TResultOfBeforeTask aBeforeTaskResult);

		/**
		 * After work is done, this part is run on the UI thread.
		 * @param aTaskResult - result of doTask().
		 */
		abstract public void afterTask(TResultOfDoTask aTaskResult);

		/**
		 * doTask() can call publishProgress(TProgress) which will run this method on UI thread.
		 * @param o - some TProgress object defining the progress with which to update the UI.
		 */
		abstract public void onTaskProgressUpdate(TProgress o);

	}

	protected Class<TaskDef> mDefinedTask;
	protected TaskDef mRunningTask = null;
	protected View mViewClicked = null;

	public OnClickTask(Class<TaskDef> aTaskDef) {
		mDefinedTask = aTaskDef;
	}

	@Override
	public void onClick(View v) {
		//once task starts running, subsequent clicks will do nothing until it finishes
		if (mRunningTask==null) {
			mViewClicked = v;
			run();
		}
	}

	@Override
	public void run() {
		if (mDefinedTask!=null && mRunningTask==null) {
			try {
				mRunningTask = mDefinedTask.newInstance().setClickHandler(this);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			mRunningTask.execute(mViewClicked);
		}
	}

	public OnClickTask go() {
		run();
		return this;
	}

}

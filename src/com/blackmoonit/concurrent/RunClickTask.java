package com.blackmoonit.concurrent;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Runnable/OnClick drop-in replacement handler that utilizes a background task for its heavy work.
 * Non-trivial tasks should use this class instead of a typical inline Runnable/OnClickListener so that 
 * UI response time is not disrupted on potentially slow jobs - which would cause multiple click
 * events while the user keeps pressing the button/widget trying to make the UI respond. 
 * Usage:<pre>
 * new RunClickTask(new RunClickTask.TaskDef() {
 * 	&#64;Override 
 * 	public void beforeTask(View v) {
 * 		myDialog.dismiss(); //to help prevent multi-clicks 
 * 	}
 * 
 * 	&#64;Override
 * 	public Object doTask(View v, Handler aProgressHandler) {
 * 		//my task stuff!
 * 		return myReturnValue; //used as param for afterTask();
 * 	}
 * 
 * 	&#64;Override
 * 	public void onProgressUpdate(Object o) {
 * 		//the default progress handler will call this method for simple UI updates.
 * 		//custom handlers can do as they please, of course.
 * 	}
 * 
 * 	&#64;Override
 * 	public void afterTask(Object aTaskResult) {
 * 		//UI task cleanup like maybe hiding any progress bars used.
 * 	}
 * 
 * 	//allows doTask() to use "publishProgress(aProgressHandler,myProgressObj);" more easily.
 * 	public void publishProgress(Handler aProgressHandler, Object o) {
 * 		if (aProgressHandler instanceof OnClickTask.DefaultHandler)
 * 			aProgressHandler.dispatchMessage(aProgressHandler.obtainMessage(0,o));
 * 	}
 * })
 * </pre> or if you need a custom progress handler:<pre>
 * new RunClickTask(myProgressHandler,new RunClickTask.TaskDef() { 
 *   ... implement interface methods here ...
 * })
 * </pre>
 * @author Ryan Fischbach
 */
public class RunClickTask extends AsyncTask<View, Object, Object> implements OnClickListener, Runnable {

	/**
	 * The default handler used for simple progress update callbacks. Usage in doTask():<pre>
	 * ((OnClickTask.DefaultHandler)aProgressHandler).publishProgress(yourProgressUpdateObject);
	 * </pre>or by<pre>
	 * aProgressHandler.dispatchMessage(aProgressHandler.obtainMessage(0,yourProgressUpdateObject));
	 * </pre>
	 */
	static public class DefaultHandler extends Handler {
		protected final RunClickTask mTask;
		
		public DefaultHandler(RunClickTask aTask) {
			mTask = aTask;
		}
		
		public void publishProgress(Object o) {
			mTask.publishProgress(o);
		}

		@Override
		public void dispatchMessage(Message msg) {
			mTask.publishProgress(msg.obj);
		}
	}
	
	/**
	 * Simplifies use of RunClickTask by defining the interface of strictly necessary methods an
	 * inline definition would need to define in order for the task to be completed fairly easily.
	 * The complexities of how to define the AsyncTask appropriately can be avoided so that one
	 * can concentrate on defining "what button X should do" rather than how to get it done.
	 * beforeTask() equates to what would be run during onClick(), followed by the background
	 * thread being created and doTask() run inside that thread.  Once doTask() is finished, 
	 * afterTask() is run on the UI thread again.
	 */
	public interface TaskDef {
		/**
		 * Portion of task that needs to be done on UI thread prior to doInBackground().
		 * @param v - the View that is firing the onClick event.
		 */
		public void beforeTask(View v);
		
		/**
		 * Actual work to be done is performed off the UI thread.
		 * Progress can be handled by custom Handle class or by calling default handler.
		 * @see RunClickTask.DefaultHandler
		 * @param v - the View that is firing the onClick event.
		 * @param aProgressHandler - custom handler passed into constructor or default one, or NULL
		 * @return Returns an Object which will be passed as a param into afterTask(). 
		 */
	    public Object doTask(View v, Handler aProgressHandler);
	    
	    /**
	     * doTask() can call publishProgress(anObj) which will run this method on UI thread.
	     * @param o - some object defining the progress with which to update the UI.
	     */
	    public void onProgressUpdate(Object o);
	    
	    /**
	     * After work is done, this part is run on the UI thread.
	     * @param aTaskResult - result of doTask().
	     */
	    public void afterTask(Object aTaskResult);
	}
	
	protected RunClickTask.TaskDef mTaskDef = null;
	protected Handler mProgressHandler = null;
	protected View mViewClicked = null; //needed because onPreExecute has no params
	
	public RunClickTask(Handler aProgressHandler, RunClickTask.TaskDef aTaskDef) {
		setTask(aTaskDef);
		setProgressHandler(aProgressHandler);
	}
	
	public RunClickTask(RunClickTask.TaskDef aTaskDef) {
		this(null,aTaskDef);
	}
	
	public RunClickTask setTask(RunClickTask.TaskDef aTaskDef) {
		mTaskDef = aTaskDef;
		if (mTaskDef==null)
			throw new IllegalArgumentException();
		return this;
	}
	
	public RunClickTask setProgressHandler(Handler aProgressHandler) {
		mProgressHandler = aProgressHandler;
		if (aProgressHandler==null) try {
			mProgressHandler = new DefaultHandler(this);
		} catch (RuntimeException rte) {
			//background threads cannot create a Handler, so ignore this error if we get it.
			//  must pass in a Handler to use at that point.
		}
		return this;
	}
	
	@Override
	public void onClick(View v) {
		mViewClicked = v;
		this.execute(v);
	}

	@Override
	protected void onPreExecute() {
		mTaskDef.beforeTask(mViewClicked);
	}

	@Override
	protected Object doInBackground(View... params) {
		View theView = null;
		if (params!=null && params.length>0)
			theView = params[0];
   		return mTaskDef.doTask(theView,mProgressHandler);
    }

	@Override
	protected void onProgressUpdate(Object... progress) {
		Object theObj = null;
		if (progress!=null && progress.length>0)
			theObj = progress[0];
        mTaskDef.onProgressUpdate(theObj);
    }

	@Override
	protected void onPostExecute(Object aTaskResult) {
		mTaskDef.afterTask(aTaskResult);
	}

	@Override
	public void run() {
		execute();
	}
	
	public RunClickTask go() {
		execute();
		return this;
	}

}

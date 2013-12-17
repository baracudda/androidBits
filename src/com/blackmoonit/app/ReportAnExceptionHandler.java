package com.blackmoonit.app;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 * Exception report delivery via email and user interaction.  Avoids giving an app the
 * permission to access the Internet. Weak reference used to access the Activity so that in
 * case the handler stays around long after the activity that created it, only a tiny memory leak
 * will result.
 * 
 * @author Ryan Fischbach
 * 
 *  Source has been released to the public as is and without any warranty.
 */
public class ReportAnExceptionHandler implements Thread.UncaughtExceptionHandler, Runnable {
	/**
	 * Debug report filename.
	 */
	public static final String ExceptionReportFilename = "istrStack.trace";

	/**
	 * Email address of where to send the debug report.
	 */
	private static final String MSG_SENDTO = "istr.bugsighting@blackmoonit.com";
	/**
	 * Note to user in the email.
	 */
	private static final String MSG_BODY = "Please help by sending this email. "+
		"No personal information is being sent (you can check by reading the rest of the email).";
	
	private final Thread.UncaughtExceptionHandler mDefaultUEH;
	private WeakReference<Context> mContext;
	private final int mMsgBodyResID;
	private boolean bEnabled = true;
 
	public ReportAnExceptionHandler(Context aContext) {
		this(aContext,0);
	}

	public ReportAnExceptionHandler(Context aContext, int aMsgResID) {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		mContext = new WeakReference<Context>(aContext);
		mMsgBodyResID = aMsgResID;
	}
	
	public Context getContext() {
		return mContext.get();
	}
	
	public Activity getActivity() {
		if (mContext.get() instanceof Activity)
			return (Activity)mContext.get();
		else
			return null;
	}
	
	/**
	 * Call this method at the end of the protected code, usually in {@link finalize()}.
	 */
	private void restoreOriginalHandler() {
		if (Thread.getDefaultUncaughtExceptionHandler().equals(this))
			Thread.setDefaultUncaughtExceptionHandler(mDefaultUEH);
	}

	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}
	
	/**
	 * Call this method after creation to start protecting all code thereafter.
	 * @param bOnlyIfDebuggable - if TRUE, it will only register the handler if app is debuggable.
	 * @Returns Return this object to allow chaining.
	 */
	public ReportAnExceptionHandler setup(boolean bOnlyIfDebuggable) {
		Context theContext = getContext();
		if (theContext==null)
			throw new NullPointerException();
		if (bOnlyIfDebuggable) {
			if ((theContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=ApplicationInfo.FLAG_DEBUGGABLE) {
				try {
					String theVersionName = theContext.getPackageManager().getPackageInfo(theContext.getPackageName(),0).versionName;
					if (theVersionName!=null && !theVersionName.contains("beta"))
						return this;
				} catch (NameNotFoundException e) {
					//should never happen, if so, assume debuggable
				}
			}	
		}
		sendDebugReportToAuthor(); //in case a previous error did not get sent to the email app
		Thread.setDefaultUncaughtExceptionHandler(this);
		return this;
	}
	
	/**
	 * Call this method as a part of onDestroy to ensure the handler chain is restored 
	 * correctly and in a timely manner.
	 */
	public void cleanup() {
		restoreOriginalHandler();
	}
	
	/**
	 * Temporarily suspend report creation/submission w/o removing the handler. 
	 * Useful for launching external Activities which might generate exceptions we have no control over.
	 */
	public void suspend() {
		bEnabled = false;
	}

	/**
	 * Resume report creation/submissions.
	 */
	public void resume() {
		bEnabled = true;
	}
	
	/**
	 * If we are still the top most handler, submit our exception. We check to see if we are still
	 * the top most in case we called some other activity that generated an exception that we have no
	 * control over. No need to report someone else's problems that we cannot do anything about.
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (this==Thread.getDefaultUncaughtExceptionHandler() && bEnabled)
			submit(e);
		//whether we report the exception or not, call the next handler in the chain
		bubbleUncaughtException(t,e);
	}
	
	/**
	 * Send the Exception up the chain, skipping other handlers of this type so only 1 report is sent.
	 * 
	 * @param t - thread object
	 * @param e - exception being handled
	 */
	protected void bubbleUncaughtException(Thread t, Throwable e) {
		if (mDefaultUEH!=null) {
			if (mDefaultUEH instanceof ReportAnExceptionHandler)
				((ReportAnExceptionHandler)mDefaultUEH).bubbleUncaughtException(t,e);
			else
				mDefaultUEH.uncaughtException(t,e);
		}
	}
	
	/**
	 * If subactivities create their own report handler, report all Activities as a trace list.
	 * A separate line is included if a calling activity/package is detected with the Intent it supplied.
	 * 
	 * @param aTrace - pass in null to force a new list to be created
	 * @return Returns the list of Activities in the handler chain.
	 */
	public LinkedList<CharSequence> getActivityTrace(LinkedList<CharSequence> aTrace) {
		if (aTrace==null)
			aTrace = new LinkedList<CharSequence>();
		Activity theAct = getActivity();
		if (theAct!=null) {
			aTrace.add(theAct.getLocalClassName()+" ("+theAct.getTitle()+")");
			if (theAct.getCallingActivity()!=null || theAct.getCallingPackage()!=null) {
				String theTraceLine;
				if (theAct.getCallingActivity()!=null)
					theTraceLine = theAct.getCallingActivity().toString();
				else
					theTraceLine = theAct.getCallingPackage().toString();
				Intent theIntent = theAct.getIntent();
				if (theIntent!=null) {
					theTraceLine += " ("+theAct.getIntent().toString()+")";
					Bundle theExtras = theIntent.getExtras();
					if (theExtras!=null) {
						Iterator<String>theExtraList = theExtras.keySet().iterator();
						while (theExtraList.hasNext()) {
							String thekey = theExtraList.next();
							String theval = (theExtras.get(thekey)!=null)?theExtras.get(thekey).toString():"";
							theTraceLine += ", ["+thekey+"=>"+theval+"]";
						}
					}
				}
				aTrace.add(theTraceLine);
			}
		} else if (getContext()!=null) {
			aTrace.add(getContext().getClass().getName());
		} else {
			aTrace.add("null (this context has been destroyed already)");
		}
		if (mDefaultUEH!=null && mDefaultUEH instanceof ReportAnExceptionHandler) {
			aTrace = ((ReportAnExceptionHandler)mDefaultUEH).getActivityTrace(aTrace);
		}
		return aTrace;
	}	
	
	public String getDebugReport(Throwable aException) {
		String theErrReport = "";
		if (aException!=null) {
			theErrReport += Utils.getDebugHeader(getContext(),aException)+"\n";
			
			//activity trace
			List<CharSequence> theActivityTrace = getActivityTrace(null);
			if (theActivityTrace!=null && theActivityTrace.size()>0) {
				theErrReport += "--------- Activity Stack Trace ---------\n";
				for (int i=0; i<theActivityTrace.size(); i++) {
					theErrReport += Utils.formatStackTraceLine(i+1,theActivityTrace.get(i));
				}//for
				theErrReport += "----------------------------------------\n\n";
			}
			

			theErrReport += Utils.getDebugInstructionTrace(aException);				
		}
		theErrReport += Utils.getDeviceEnvironment(getContext());
		theErrReport += "END REPORT.";
		return theErrReport;
	}
	
	/**
	 * Write the given debug report to external storage.
	 * 
	 * @param aReport - the debug report
	 */
	protected void saveDebugReport(String aReport) {
		Context theContext = getContext();
		if (theContext!=null) {
			//save report to file
			try {
				FileOutputStream theFile = theContext.openFileOutput(ExceptionReportFilename, Context.MODE_PRIVATE);
				theFile.write(aReport.getBytes());
				theFile.close();
			} catch (Exception e) {
				//exception during report needs to be ignored, do not wish to start infinite loop
			} catch (Error err) {
				//error during report needs to be ignored, do not wish to start infinite loop
			}
		}
	}
	
	/**
	 * Read in saved debug report and send to email app.
	 */
	public void sendDebugReportToAuthor() {
		Context theContext = getContext();
		if (theContext==null)
			return;
		String theLine = "";
		String theTrace = "";
		try {
			BufferedReader theReader = new BufferedReader(
					new InputStreamReader(theContext.openFileInput(ExceptionReportFilename)));
			while ((theLine = theReader.readLine())!=null) {
				theTrace += theLine+"\n";
			}
			if (sendDebugReportToAuthor(theTrace)) {
				theContext.deleteFile(ExceptionReportFilename);
			}
		} catch (Exception e) {
			// avoid infinite recursion
		} catch (Error err) {
			// avoid infinite recursion
		}		
	}
	
	/**
	 * Send the given report to email app.
	 *
	 * @param aReport - the debug report to send
	 * @return Returns true if the email app was launched regardless if the email was sent.
	 */
	public Boolean sendDebugReportToAuthor(String aReport) {
		Context theContext = getContext();
		if (theContext!=null && aReport!=null) {
			Intent theIntent = new Intent(Intent.ACTION_SEND);
			String theSubject = Utils.getAppName(getContext())+" Exception Report";
			String mMsgBody;
			try {
				mMsgBody = (mMsgBodyResID!=0)?theContext.getString(mMsgBodyResID):MSG_BODY;
			} catch (Resources.NotFoundException e) {
				mMsgBody = MSG_BODY; 
			}
			String theBody = "\n"+mMsgBody+"\n\n"+aReport+"\n\n"+mMsgBody+"\n\n";
			theIntent.putExtra(Intent.EXTRA_EMAIL,new String[] {MSG_SENDTO});
			theIntent.putExtra(Intent.EXTRA_TEXT, theBody);
			theIntent.putExtra(Intent.EXTRA_SUBJECT, theSubject);
			theIntent.setType("message/rfc822");
			//theIntent.setType("vnd.android.cursor.dir/email");
			List<?> theList = theContext.getPackageManager().queryIntentActivities(theIntent,0);
			Boolean hasSendRecipients = (theList!=null && theList.size()>0);
			if (hasSendRecipients) {
				theContext.startActivity(theIntent);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	@Override
	public void run() {
		sendDebugReportToAuthor();
	}
	
	/**
	 * Create an exception report and start an email with the contents of the report.
	 *
	 * @param e - the exception
	 */
	public void submit(Throwable e) {
		String theErrReport = getDebugReport(e);
		saveDebugReport(theErrReport);
		Activity theAct = getActivity();
		if (theAct!=null) {
			//try to send file contents via email (need to do so via the UI thread)
			theAct.runOnUiThread(this);
		}
	}
	
	static public class Utils {
		private Utils() {}; //do not instantiate
		
		/**
		 * Format used to display the stack number at beginning of line.
		 */
		static public NumberFormat TraceStackNumberFormat = new DecimalFormat("#0.");
		
		/**
		 * Return the application's friendly name.
		 * @param aContext - the context.
		 * @return Returns the application name as defined by the android:name attribute.
		 */
		static public CharSequence getAppName(Context aContext) {
			return aContext.getString(aContext.getApplicationInfo().labelRes);
		}
		
		/**
		 * Return a string containing the device environment.
		 * @param aContext - the context.
		 * @return Returns a string with the device info used for debugging.
		 */
		static public String getDeviceEnvironment(Context aContext) {
			if (aContext==null)
				return "";
			//app environment
			PackageInfo pi;
			try {
				pi = aContext.getPackageManager().getPackageInfo(aContext.getPackageName(),0);
			} catch (NameNotFoundException nnfe) {
				//doubt this will ever run since we want info about our own package
				pi = new PackageInfo();
				pi.packageName = "com.unknown.404";
				pi.versionName = "Unknown.NaN";
				pi.versionCode = 69;
			}
			Resources theResources = aContext.getResources();
			SimpleDateFormat theDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_zzz");
			String s = "-------- Environment --------\n";
			s += "Time\t= "+theDateFormat.format(new Date())+"\n";
			s += "Device\t= "+Build.FINGERPRINT+"\n";
			s += "Make\t= "+Build.MANUFACTURER+"\n";
			s += "Model\t= "+Build.MODEL+"\n";
			s += "Product\t= "+Build.PRODUCT+"\n";
			s += "App\t\t= "+pi.packageName+", version "+pi.versionName+" (build "+pi.versionCode+")\n";
			s += "Locale\t= "+theResources.getConfiguration().locale.getDisplayName()+"\n";
			s += "Res\t\t= "+theResources.getDisplayMetrics().toString()+"\n";
			s += "-----------------------------\n";
			return s;
		}
		
		/**
		 * @param aContext - the context.
		 * @param aException - the exception.
		 * @return Returns the package name and exception message.
		 */
		static public String getDebugHeader(Context aContext, Throwable aException) {
			String theResult = "";
			if (aContext!=null && aException!=null) {
				theResult += getAppName(aContext)+" generated the following exception:\n";
				theResult += aException.toString()+"\n";
			}
			return theResult;
		}
		
		static public CharSequence formatStackTraceLine(int aLineNum, CharSequence aLine) {
			return TraceStackNumberFormat.format(aLineNum)+"\t"+aLine+"\n";
		}
		
		static public String getDebugInstructionTrace(Throwable aException) {
			String theResult = "";
			if (aException!=null) {
				//stack trace
				StackTraceElement[] theStackTrace = aException.getStackTrace();
				if (theStackTrace!=null && theStackTrace.length>0) {
					theResult += "-------- Instruction Stack Trace -------\n";
					for (int i=0; i<theStackTrace.length; i++) {
						theResult += formatStackTraceLine(i+1,theStackTrace[i].toString());
					}
					theResult += "----------------------------------------\n\n";
				}
				//if the exception was thrown in a background thread inside
				//AsyncTask, then the actual exception can be found with getCause
				Throwable theCause = aException.getCause();
				if (theCause!=null) {
					theResult += "----------- Cause -----------\n";
					theResult += theCause.toString() + "\n\n";
					theStackTrace = theCause.getStackTrace();
					if (theStackTrace!=null && theStackTrace.length>0) {
						for (int i=0; i<theStackTrace.length; i++) {
							theResult += formatStackTraceLine(i+1,theStackTrace[i].toString());
						}
					}
					theResult += "-----------------------------\n";
				}
			}
			return theResult;
		}
		
		static public String getDebugReport(Context aContext, Throwable aException) {
			String theErrReport = "";
			if (aException!=null) {
				theErrReport += getDebugHeader(aContext,aException)+"\n";
				theErrReport += getDebugInstructionTrace(aException)+"\n";				
			}
			theErrReport += getDeviceEnvironment(aContext)+"\n";
			theErrReport += "END REPORT.";
			return theErrReport;
		}
		
		/**
		 * Submits the Iterator objects.toString() to the Log.d() method.
		 * @param aTag - tag used in Log.d().
		 * @param aTrace - interator to dump to the log.
		 */
		static public void logDebugTrace(String aTag, Iterator<?> aTrace) {
			while (aTrace!=null && aTrace.hasNext()) {
				Log.d(aTag,aTrace.next().toString());
			}
		}
		
		/**
		 * Submits the Bundle keys and objects.toString() to the Log.d() method.
		 * @param aTag - tag used in Log.d().
		 * @param aBundle - bundle to dump to the log.
		 */
		static public void logDebugBundle(String aTag, Bundle aBundle) {
			if (aBundle==null) {
				Log.d(aTag,"is null");
			}
			ArrayList<String> theEntries = new ArrayList<String>(aBundle.size());
			Iterator<String> theKeys = aBundle.keySet().iterator();
			String theKey;
			while (theKeys.hasNext()) {
				theKey = theKeys.next();
				theEntries.add(theKey+"="+aBundle.get(theKey).toString());
			}
			logDebugTrace(aTag,theEntries.iterator());
		}
		
	}

}

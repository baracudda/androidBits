package com.blackmoonit.androidbits.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.net.WebUtils;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.Locale;

/**
 * Exception report delivery via email and user interaction.  Avoids giving an app the
 * permission to access the Internet. Weak reference used to access the Activity so that in
 * case the handler stays around long after the activity that created it, only a tiny memory leak
 * will result.
 *
 * UPDATE 2014.07: Android SDK has progressed to a point where referencing library resource strings
 * is safe and easy to do.  Code revamp removed static strings and referenced Context.getString()
 * instead. A benefit to updating this code is that different apps can send emails to different
 * addresses instead of relying on one statically defined email address.
 *
 * @author Ryan Fischbach
 *
 *  Source has been released to the public as is and without any warranty.
 */
public class ReportAnExceptionHandler implements Thread.UncaughtExceptionHandler, Runnable {
	/**
	 * Old exception handler we need to save and put back on finalize().
	 */
	protected final Thread.UncaughtExceptionHandler mDefaultUEH;
	/**
	 * Context to use for strings and intents.
	 */
	protected WeakReference<Context> mContext;
	/**
	 * If FALSE, logging will be disabled.
	 */
	protected boolean bEnabled = true;
	/**
	 * Used in case Context disappears so we can log what went wrong (hopefully).
	 */
	protected String mContextClassName = null;
	/**
	 * Constructor may dictate that we purely log rather than email/delete the log.
	 */
	protected boolean bSendLogOnlyIfNotDebuggable = true;
	/**
	 * If TRUE, will attempt to email the log at app start and when unhandled error is thrown.
	 */
	protected boolean bSendLog = true;

	/**
	 * Construct the exception handler object.
	 * @param aContext - context to use.
	 * @param bEmailOnlyIfNotDebuggable - if TRUE, the log will only be emailed if app is !debuggable.
	 */
	public ReportAnExceptionHandler(Context aContext, boolean bEmailOnlyIfNotDebuggable) {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		mContext = new WeakReference<Context>(aContext);
		if (aContext!=null) {
			mContextClassName = aContext.getClass().getName();
		}
		bSendLogOnlyIfNotDebuggable = bEmailOnlyIfNotDebuggable;
	}

	/**
	 * Creates an unhandled exception report handler only for non-debug builds.
	 * @param aContext - context to use.
	 */
	public ReportAnExceptionHandler(Context aContext) {
		this(aContext, true);
	}

	public Context getContext() {
		return mContext.get();
	}

	/**
	 * Call this method at the end of the protected code, usually in {@link #finalize()}.
	 */
	protected void restoreOriginalHandler() {
		if (Thread.getDefaultUncaughtExceptionHandler().equals(this))
			Thread.setDefaultUncaughtExceptionHandler(mDefaultUEH);
	}

	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}

	/**
	 * Used internally during #setup() to see if bSendLog should be true/false.
	 */
	protected void checkSendLog() {
		if (bSendLogOnlyIfNotDebuggable) {
			ApplicationInfo myAppInfo = getContext().getApplicationInfo();
			if (myAppInfo!=null &&
					(myAppInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)==ApplicationInfo.FLAG_DEBUGGABLE) {
				bSendLog = false;
			}
		}
	}

	/**
	 * Call this method after creation to start protecting all code thereafter.
	 * @Returns Return this object to allow chaining.
	 */
	public ReportAnExceptionHandler setup() {
		checkSendLog();
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
	 * Return the filename to use for exception reports.
	 * @return Returns the filename.
	 */
	protected String getExceptionReportFilename() {
		return "AppStackTraces.txt";
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
		Context theContext = getContext();
		if (theContext==null) {
			if (mContextClassName!=null)
				aTrace.add("null (was "+mContextClassName+")");
			else //no context to load strings, must use fixed string here.
				aTrace.add("null (this context has been destroyed already)");
		} else if (theContext!=null && theContext instanceof Activity) {
			Activity theAct = (Activity) theContext;
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
		} else if (theContext instanceof Service) {
			Service theService = (Service) theContext;
			aTrace.add("Service "+theContext.getClass().getName()+" of "+theService.getApplication().getPackageName());
		} else {
			aTrace.add(theContext.getClass().getName()+" of "+theContext.getApplicationContext().getPackageName());
		}
		if (mDefaultUEH!=null && mDefaultUEH instanceof ReportAnExceptionHandler) {
			aTrace = ((ReportAnExceptionHandler)mDefaultUEH).getActivityTrace(aTrace);
		}
		return aTrace;
	}

	/**
	 * Given an Exception, construct a string of the stack and cause traces.
	 * @param aContext - the context to use.
	 * @param anActivityTrace - list of activity/service/context trace strings.
	 * @return Returns the string of the activity trace separated by newlines.
	 */
	public String getDebugActivityTrace(Context aContext, List<CharSequence> anActivityTrace) {
		String theResult = "";
		Context theContext = aContext;
		List<CharSequence> theActivityTrace = anActivityTrace;
		if (theActivityTrace!=null && theActivityTrace.size()>0) {
			theResult += theContext.getString(R.string.postmortem_report_act_trace_header)+"\n";
			for (int i=0; i<theActivityTrace.size(); i++) {
				theResult += Utils.formatStackTraceLine(i+1,theActivityTrace.get(i));
			}//for
			theResult += theContext.getString(R.string.postmortem_report_act_trace_footer)+"\n";
			theResult += "\n";
		}
		return theResult;
	}

	/**
	 * Compiles the debug information and returns it as a string. The report
	 * consists of {@link Utils#getDebugHeader(android.content.Context,Throwable)} followed by
	 * {@link #getDebugActivityTrace(android.content.Context, java.util.List)} followed by
	 * {@link Utils#getDebugInstructionTrace(android.content.Context,Throwable)} followed by
	 * {@link Utils#getDeviceEnvironment(android.content.Context)}.
	 * @param aException - the exception to report.
	 * @return Returns the exception report as a string.
	 * @see Utils#getDebugHeader(android.content.Context,Throwable)
	 * @see #getDebugActivityTrace(android.content.Context, java.util.List)
	 * @see Utils#getDebugInstructionTrace(android.content.Context, Throwable)
	 * @see Utils#getDeviceEnvironment(android.content.Context)
	 */
	public String getDebugReport(Throwable aException) {
		Context theContext = getContext();
		String theErrReport = "";
		if (aException!=null && theContext!=null) {
			theErrReport += Utils.getDebugHeader(getContext(),aException)+"\n";
			theErrReport += getDebugActivityTrace(theContext, getActivityTrace(null))+"\n";
			theErrReport += Utils.getDebugInstructionTrace(theContext, aException)+"\n";
		}
		theErrReport += Utils.getDeviceEnvironment(theContext)+"\n";
		if (theContext!=null)
			theErrReport += theContext.getString(R.string.postmortem_report_footer_text);
		return theErrReport;
	}

	/**
	 * Write the given debug report to external storage.
	 * @param aReport - the debug report
	 */
	protected void saveDebugReport(String aReport) {
		Context theContext = getContext();
		if (theContext!=null) {
			//save report to file
			String theFilename = getExceptionReportFilename();
			try {
				File theDebugFile = new File(theFilename);
				if (theDebugFile.length() > 50000) {
					theDebugFile.delete();
				}
				FileOutputStream theFile = theContext.openFileOutput(theFilename, Context.MODE_APPEND);
				theFile.write(aReport.getBytes());
				theFile.close();
			} catch (Exception e) {
				//exception during report needs to be ignored, do not wish to start infinite loop
			} catch (Error err) {
				//error during report needs to be ignored, do not wish to start infinite loop
			}
		} else {
			//cannot commit suicide in Java, so just cleanup our instance (de-handle handlers)
			cleanup();
		}
	}

	/**
	 * Read in saved debug report and send to email app.
	 */
	protected void sendDebugReportToAuthor() {
		Context theContext = getContext();
		if (theContext!=null && this.bSendLog) {
			String theLine = "";
			String theTrace = "";
			String theFilename = getExceptionReportFilename();
			try {
				BufferedReader theReader = new BufferedReader(new InputStreamReader(
						theContext.openFileInput(theFilename)));
				while ((theLine = theReader.readLine())!=null) {
					theTrace += theLine+"\n";
				}
				if (sendDebugReportToAuthor(theTrace)) {
					theContext.deleteFile(theFilename);
				}
			} catch (Exception e) {
				// avoid infinite recursion
			} catch (Error err) {
				// avoid infinite recursion
			}
		}
	}

	/**
	 * Send the given report to email app via Intent mechanism.
	 * @param aReport - the debug report to send
	 * @return Returns TRUE if no context to use, no report to send, or
	 * if the email app was launched regardless if the email was sent.
	 */
	public boolean sendDebugReportToAuthor(String aReport) {
		Context theContext = getContext();
		if (theContext!=null && this.bSendLog && aReport!=null) {
			Intent theIntent = WebUtils.newEmailIntent(theContext.getString(R.string.postmortem_report_email_recipients));

			if (!(theContext instanceof Activity))
				theIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			String theSubject = theContext.getString(R.string.postmortem_report_email_subject,
					Utils.getAppName(getContext()));

			String theMsg = theContext.getString(R.string.postmortem_report_email_msg);
			theIntent.putExtra(Intent.EXTRA_SUBJECT, theSubject);

			String theBody = "\n"+theMsg+"\n\n"+aReport+"\n\n"+theMsg+"\n\n";
			theIntent.putExtra(Intent.EXTRA_TEXT, theBody);

			List<?> theList;
			try {
				theList = theContext.getPackageManager().queryIntentActivities(theIntent, 0);
			} catch (Exception ex) {
				return true; //log file too big, will crash no matter what, delete it!
			} catch (Error err) {
				return true; //log file too big, will crash no matter what, delete it!
			}
			boolean hasSendRecipients = (theList!=null && theList.size()>0);
			if (hasSendRecipients) {
				theContext.startActivity(theIntent);
				return true;
			} else {
				return false;
			}
		} else {
			//return TRUE if we were supposed to send a log, else FALSE (so its not deleted)
			return this.bSendLog;
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
		Context theContext = getContext();
		if (theContext instanceof Activity) {
			//try to send file contents via email (need to do so via the UI thread)
			((Activity) theContext).runOnUiThread(this);
		} else {
			run();
		}
	}

	/**
	 * Utility function used by the report class, but may be useful for other reasons.
	 */
	static public class Utils {
		private Utils() {}; //do not instantiate

		/**
		 * Format used to display the stack number at beginning of line.
		 */
		static public final NumberFormat TraceStackNumberFormat = new DecimalFormat("#0.");

		/**
		 * Return the application's friendly name.
		 * @param aContext - the context to use.
		 * @return Returns the application name as defined by the android:label attribute.
		 */
		static public CharSequence getAppName(Context aContext) {
			return aContext.getString(aContext.getApplicationInfo().labelRes);
		}

		/**
		 * Return a string containing the device environment.
		 * @param aContext - the context to use.
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
			SimpleDateFormat theDateFormat = new SimpleDateFormat(
					aContext.getString(R.string.postmortem_report_env_ts_format),Locale.US);
			String s = aContext.getString(R.string.postmortem_report_env_header)+"\n";
			s += aContext.getString(R.string.postmortem_report_env_time,	theDateFormat.format(new Date()))+"\n";
			s += aContext.getString(R.string.postmortem_report_env_device,	Build.FINGERPRINT)+"\n";
			s += aContext.getString(R.string.postmortem_report_env_make,	Build.MANUFACTURER)+"\n";
			s += aContext.getString(R.string.postmortem_report_env_model,	Build.MODEL)+"\n";
			s += aContext.getString(R.string.postmortem_report_env_product,	Build.PRODUCT)+"\n";
			s += aContext.getString(R.string.postmortem_report_env_app,
					pi.packageName, pi.versionName, pi.versionCode )+"\n";
			s += aContext.getString(R.string.postmortem_report_env_locale,
					theResources.getConfiguration().locale.getDisplayName() )+"\n";
			s += aContext.getString(R.string.postmortem_report_env_display,
					theResources.getDisplayMetrics().toString() )+"\n";
			s += aContext.getString(R.string.postmortem_report_env_footer)+"\n";
			return s;
		}

		/**
		 * Construct the report header string.
		 * @param aContext - the context to use.
		 * @param aException - the exception from which to format a message.
		 * @return Returns the AppName and exception message.
		 */
		static public String getDebugHeader(Context aContext, Throwable aException) {
			String theResult = "";
			if (aContext!=null && aException!=null) {
				theResult = aContext.getString(R.string.postmortem_report_header_text,
						getAppName(aContext), aException.toString() );
				theResult += "\n";
			}
			return theResult;
		}

		/**
		 * Return a string formatted like a stack trace debug printout.
		 * @param aLineNum - line number.
		 * @param aLine - text of the line.
		 * @return Return the string formatted like a single line in a stack trace.
		 */
		static public CharSequence formatStackTraceLine(int aLineNum, CharSequence aLine) {
			return TraceStackNumberFormat.format(aLineNum)+"\t"+aLine+"\n";
		}

		/**
		 * Given an Exception, construct a string of the stack and cause traces.
		 * @param aContext - the context to use.
		 * @param aException - an exception to trace out.
		 * @return Returns the string of the stack trace separated by newlines.
		 */
		static public String getDebugInstructionTrace(Context aContext, Throwable aException) {
			String theResult = "";
			if (aException!=null) {
				//stack trace
				StackTraceElement[] theStackTrace = aException.getStackTrace();
				if (theStackTrace!=null && theStackTrace.length>0) {
					theResult += aContext.getString(R.string.postmortem_report_stack_trace_header)+"\n";
					for (int i=0; i<theStackTrace.length; i++) {
						theResult += formatStackTraceLine(i+1,theStackTrace[i].toString());
					}
					theResult += aContext.getString(R.string.postmortem_report_stack_trace_footer)+"\n";
					theResult += "\n";
				}
				//if the exception was thrown in a background thread inside
				//AsyncTask, then the actual exception can be found with getCause
				Throwable theCause = aException.getCause();
				if (theCause!=null) {
					theResult += aContext.getString(R.string.postmortem_report_cause_trace_header)+"\n";
					theResult += theCause.toString() + "\n\n";
					theStackTrace = theCause.getStackTrace();
					if (theStackTrace!=null && theStackTrace.length>0) {
						for (int i=0; i<theStackTrace.length; i++) {
							theResult += formatStackTraceLine(i+1,theStackTrace[i].toString());
						}
					}
					theResult += aContext.getString(R.string.postmortem_report_cause_trace_footer)+"\n";
					theResult += "\n";
				}
			}
			return theResult;
		}

		/**
		 * Compiles the debug information and returns it as a string. The report
		 * consists of {@link #getDebugHeader(android.content.Context,Throwable)} followed by
		 * {@link #getDebugInstructionTrace(android.content.Context,Throwable)} followed by
		 * {@link #getDeviceEnvironment(android.content.Context)}.
		 * @param aContext - the context to use.
		 * @param aException - the exception to report.
		 * @return Returns the exception report as a string.
		 * @see #getDebugHeader(android.content.Context,Throwable)
		 * @see #getDebugInstructionTrace(android.content.Context, Throwable)
		 * @see #getDeviceEnvironment(android.content.Context)
		 */
		static public String getDebugReport(Context aContext, Throwable aException) {
			String theErrReport = "";
			if (aException!=null) {
				theErrReport += getDebugHeader(aContext,aException)+"\n";
				theErrReport += getDebugInstructionTrace(aContext,aException)+"\n";
			}
			theErrReport += getDeviceEnvironment(aContext)+"\n";
			theErrReport += aContext.getString(R.string.postmortem_report_footer_text);
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
				Log.d(aTag,"= null");
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

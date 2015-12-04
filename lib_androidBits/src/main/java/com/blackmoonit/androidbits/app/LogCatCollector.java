package com.blackmoonit.androidbits.app;

import android.os.Build;

import com.blackmoonit.androidbits.concurrent.ThreadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes logcat commands and collects it's output.
 *
 * @author Kevin Gaudin, modifications by Ryan Fischbach
 */
public class LogCatCollector {

	/**
	 * Number of latest lines kept from the logcat output.
	 */
	protected int mTailCount = 100;

	protected int mReadBufferSize = 2048;

	/**
	 * LogCat arguments besides how many lines to collect.
	 */
	public List<String> mLogCatArguments = new ArrayList<String>();

	public LogCatCollector() {
	}

	public LogCatCollector(int aTailCount) {
		this();
		setTailCount(aTailCount);
	}

	public LogCatCollector setTailCount(int aLineCount) {
		if (aLineCount>0) {
			mTailCount = aLineCount;
		}
		return this;
	}

	public LogCatCollector setReadBufferSize(int aSize) {
		if (aSize>0) {
			mReadBufferSize = aSize;
		}
		return this;
	}

	public LogCatCollector addArg(String aArg) {
		mLogCatArguments.add(aArg);
		return this;
	}

	public LogCatCollector showTimestamps() {
		addArg("-v").addArg("time");
		return this;
	}

	/**
	 * Executes the logcat command with arguments taken from {@link #mLogCatArguments}
	 * @return A {@link String} containing the latest lines of the output from "main" buffer.
	 *   Default is 100 lines. You should be aware that increasing this value causes a longer
	 *   report generation time and a bigger footprint on the device data
	 *   plan consumption.
	 */
	public String collectLogCat() {
		return collectLogCat(null);
	}

	/**
	 * Executes the logcat command with arguments taken from {@link #mLogCatArguments}
	 * @param bufferName
	 *   The name of the buffer to be read: "main" (default), "radio" or "events".
	 * @return A {@link String} containing the latest lines of the output from "main" buffer.
	 *   Default is 100 lines. You should be aware that increasing this value causes a longer
	 *   report generation time and a bigger footprint on the device data
	 *   plan consumption.
	 */
	public String collectLogCat(String bufferName) {
		final int myPid = android.os.Process.myPid();
		String myPidStr = Integer.toString(myPid) + "):";

		final List<String> commandLine = new ArrayList<String>();
		commandLine.add("logcat");
		if (bufferName != null) {
			commandLine.add("-b");
			commandLine.add(bufferName);
		}

		// "-t n" argument has been introduced in FroYo (API level 8). For
		// devices with lower API level, we will have to emulate its job.
		final int tailCount;
		final List<String> logcatArgumentsList = new ArrayList<String>(mLogCatArguments);
		final int tailIndex = logcatArgumentsList.indexOf("-t");
		if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
			tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
				logcatArgumentsList.remove(tailIndex + 1);
				logcatArgumentsList.remove(tailIndex);
				logcatArgumentsList.add("-d");
			}
		} else {
			logcatArgumentsList.add("-t");
			logcatArgumentsList.add(String.valueOf(mTailCount));
			tailCount = mTailCount;
		}

		final List<String> logcatBuf = new ArrayList<String>(tailCount);
		commandLine.addAll(logcatArgumentsList);
		BufferedReader bufferedReader = null;
		try {
			final Process process = Runtime.getRuntime().exec(
					commandLine.toArray(new String[commandLine.size()])
			);
			bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()), mReadBufferSize
			);

			// Dump stderr to null
			new ThreadTask(new Runnable() {
				public void run() {
					try {
						InputStream stderr = process.getErrorStream();
						byte[] dummy = new byte[mReadBufferSize];
						while (stderr.read(dummy) >= 0)
							;
					} catch (IOException e) {
						//do nothing
					}
				}
			}).start();

			String line;
			do {
				line = bufferedReader.readLine();
				if (line != null && line.contains(myPidStr)) {
					logcatBuf.add(line + "\n");
				}
			} while (line!=null);

		} catch (IOException e) {
			return getClass().getSimpleName()+".collectLogCat could not retrieve data. "
					+ e.getMessage();
		} finally {
			if (bufferedReader!=null) try {
				bufferedReader.close();
			} catch (IOException e) {
				return getClass().getSimpleName()+".collectLogCat could not close buffer. "
						+ e.getMessage();
			}
		}

		return logcatBuf.toString();
	}
}

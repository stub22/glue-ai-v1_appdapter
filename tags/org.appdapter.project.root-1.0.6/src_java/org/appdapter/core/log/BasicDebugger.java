/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicDebugger implements Loggable {

	static Logger theFallbackLogger = LoggerFactory.getLogger(BasicDebugger.class);

	private enum MsgKind {

		INFO,
		WARN,
		ERROR
	}
	private Logger myLogger;
	private int myDebugImportanceThreshold = IMPO_NORM;

	/*
	 * Null tests are not working for diversion to System.out because: SLF4J: Failed to load class
	 * "org.slf4j.impl.StaticLoggerBinder". SLF4J: Defaulting to no-operation (NOP) logger implementation SLF4J: See
	 * http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
	 *
	 */
	protected Logger getLogger() {
		if (myLogger == null) {
			myLogger = getLoggerForClass(this.getClass());
		}
		return myLogger;
	}

	public static Logger getLoggerForClass(Class c) {
		Logger result = LoggerFactory.getLogger(c);
		if (result == null) {
			result = theFallbackLogger;
		}
		return result;
	}

	public void useLoggerForClass(Class c) {
		Logger l = getLoggerForClass(c);
		setLogger(l);
	}

	public synchronized void setLogger(Logger l) {
		myLogger = l;
	}

	protected boolean isLoggerUsable() {
		Logger l = getLogger();
		return ((l != null) && !(l instanceof NOPLogger));
	}

	/**
	 * More "urgent" debug has higher level (numerically), so high threshold (numerically) means *less* debug output.
	 */
	public void setDebugImportanceThreshold(int thresh) {
		myDebugImportanceThreshold = thresh;
	}

	public boolean checkDebugImportance(int importance) {
		return (importance >= myDebugImportanceThreshold);
	}

	/**
	 *
	 * @param importance must be >= current debug importance threshold.
	 * @param timeStampFlag
	 * @param oldStamp
	 * @param msgObjs - will be converted to strings only if importance threshold passed.
	 * @return
	 */
	public Long logInfoEvent(int importance, boolean timeStampFlag, Long oldStamp, String formatSpec, Object... msgObjs) {
		Long resultStamp = null;
		if (checkDebugImportance(importance)) {
			String formattedMsg = String.format(formatSpec, msgObjs);
			String tsString = "";
			if (timeStampFlag) {
				long stamp = System.currentTimeMillis();
				long fullSec = stamp / 1000;
				long milSec = stamp - fullSec * 1000;
				tsString = String.format("[ts=%,d.%3d]", fullSec, milSec);
				if (oldStamp != null) {
					long elapsedMilSec = stamp - oldStamp;
					tsString = tsString + "[el=" + elapsedMilSec + "ms]";
				}
				resultStamp = stamp;
			}
			String formatted = "[imp=" + importance + "]" + tsString + "=" + formattedMsg;
			if (isLoggerUsable()) {
				Logger log = getLogger();
				log.info(formatted);
			} else {
				System.out.println("[sys.out," + getClass().getSimpleName() + ".lie]" + formatted);
			}
		}
		return resultStamp;
	}

	@Override public void logInfo(int importance, String msg) {
		logInfoEvent(importance, false, null, "%s", msg);
	}

	@Override public void logInfo(String msg) {
		logInfo(IMPO_NORM, msg);
	}

	protected void logWithException(MsgKind mk, String msg, Throwable t) {
		if (isLoggerUsable()) {
			Logger l = getLogger();
			if (mk == MsgKind.ERROR) {
				l.error(msg, t);
			} else {
				l.warn(msg, t);
			}
		} else {
			if (msg != null) {
				// Better to not use STDERR, because then the sequence is disrupted.
				System.out.println(mk.name() + ": " + msg);
			}
			if (t != null) {
				switch (mk) {
					case ERROR:
						t.printStackTrace(System.out);
						break;
					case WARN:
						System.out.println("Warning Exception: " + t);
						break;
				}
			}
		}
	}

	@Override public void logError(String msg, Throwable t) {
		logWithException(MsgKind.ERROR, msg, t);
	}

	@Override public void logWarning(String msg, Throwable t) {
		logWithException(MsgKind.WARN, msg, t);
	}

	@Override public void logError(String msg) {
		logError(msg, null);
	}

	@Override public void logWarning(String msg) {
		logWarning(msg, null);
	}

	public void logDebug(String msg) {
		logInfo(IMPO_LO, msg);
	}
}

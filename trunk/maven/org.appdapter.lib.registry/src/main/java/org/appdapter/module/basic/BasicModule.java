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
package org.appdapter.module.basic;

import org.appdapter.api.module.Modulator;
import org.appdapter.api.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BasicModule implements Module {
	
	static Logger thefalbackLogger = LoggerFactory.getLogger(BasicModule.class);
	
	private		Modulator		myModulator;
	private		State			myState = State.PRE_INIT;
	
	private		boolean			myStopRequestedFlag = false;
	
	private		Logger			myLogger;
	private		int				myDebugImportanceThreshold = IMPO_NORM;
	
	public static int			IMPO_NORM		= 0;
	public static int			IMPO_LO			= -10;
	public static int			IMPO_LOLO		= -100;
	public static int			IMPO_MIN		= Integer.MIN_VALUE;
	public static int			IMPO_HI			= 10;
	public static int			IMPO_HIHI		= 100;
	public static int			IMPO_MAX		= Integer.MAX_VALUE;
	
	protected Logger getLogger() {
		if (myLogger == null) {
			myLogger = LoggerFactory.getLogger(this.getClass());
			if (myLogger == null) {
				myLogger = thefalbackLogger;
			}
		}
		return myLogger;
	}
	public synchronized void setLogger(Logger l) {
		myLogger = l;
	}

	/*
	 * More "urgent" debug has higher level, 
	 * so high threshold means less debug output.
	 */
	public void setDebugImportanceThreshold(int thresh) {
		myDebugImportanceThreshold = thresh;
	}
	public boolean checkDebugImportance(int importance) {
		return (importance >= myDebugImportanceThreshold);
	}

	public void logDebug(int importance, String msg) {
		if (checkDebugImportance(importance)) {
			String formatted = "[imp=" + importance +"] " + msg;
			Logger log = getLogger();
			if (log != null) {
				log.info(formatted);
			} else {
				System.out.println("[System.out-BasicModule.logDebug]" + formatted);	
			}
		}
	}

	@Override public State getState() {
		return myState;
	}
	@Override public void markStopRequested() {
		myStopRequestedFlag = true;
	}

	@Override public boolean isStopRequested() {
		return myStopRequestedFlag;
	}
	@Override public Modulator getModulator() {
		return myModulator;
	}

	@Override public synchronized void setModulator(Modulator m) {
		myModulator = m;
	}
	protected void notifyStateViolation(String detectingMethod, String expectedStateDesc, boolean throExcept) { 
		String msg = "[" + detectingMethod + "] found illegal state [" + myState + " instead of expected [" + expectedStateDesc + "]";
		logDebug(IMPO_HI, msg);
		if (throExcept) {
			throw new RuntimeException(msg);
		}
	}
	/**
	 * Ensure we are in an allowed state, will notifyStateViolation if not.
	 * 
	 */
	protected void verifyStoredState(String checkingMethod, boolean throExcept, State... allowedStates) {
		for (State s : allowedStates) {
			if (myState == s) {
				// TODO - let logDebug format the string, only after importance check.
				logDebug(IMPO_LO, "[" + checkingMethod + "] verified storedState: " + s);
				return;
			}
		}
		notifyStateViolation(checkingMethod, allowedStates.toString(), throExcept);
	}	

	
	@Override public synchronized void failDuringInitOrStartup() {
		verifyStoredState("failDuringInitOrStartup", true, State.PRE_INIT, State.IN_INIT, State.WAIT_TO_START, State.IN_START);
		myState = State.POST_STOP_OR_FAILED_STARTUP;
	}
		
		
	/*
	 * Optionally verifies that pre-state is PRE_INIT.
	 */
	protected synchronized void enterBasicInitModule(boolean verifyPreInitState) {
		if (verifyPreInitState) { 
			verifyStoredState("basicPreInitModule", true, State.PRE_INIT);
		}
		myState = State.IN_INIT;
	}
	protected synchronized void exitBasicInitModule(boolean verifyInInitState) { 
		if (verifyInInitState) {
			verifyStoredState("basicPostInitModule", true, State.IN_INIT);
		}
		myState = State.WAIT_TO_START;
	}

	protected synchronized void enterBasicStart() {
		verifyStoredState("enterBasicStart", true, State.WAIT_TO_START);
		myState = State.IN_START;
	}
	protected synchronized void exitBasicStart() {
		verifyStoredState("exitBasicStart", true, State.IN_START);
		myState = State.WAIT_TO_RUN_OR_STOP;
	}

	protected synchronized void enterBasicRunOnce() {
		verifyStoredState("enterBasicRunOnce", true, State.WAIT_TO_RUN_OR_STOP);
		myState = State.IN_RUN;
	}
	protected synchronized void exitBasicRunOnce() {
		verifyStoredState("exitBasicRunOnce", true, State.IN_RUN);
		myState = State.WAIT_TO_RUN_OR_STOP;
	}

	protected synchronized void enterBasicStop() {
		verifyStoredState("enterBasicStop", true, State.WAIT_TO_RUN_OR_STOP);
		myState = State.IN_STOP;
	}
	protected synchronized void exitBasicStop() {
		verifyStoredState("exitBasicStop", true, State.IN_STOP);
		myState = State.POST_STOP_OR_FAILED_STARTUP;
	}

	protected synchronized void enterBasicReleaseModule() {
		verifyStoredState("enterBasicReleaseModule", true, State.POST_STOP_OR_FAILED_STARTUP);
	}
	protected synchronized void exitBasicReleaseModule() {
		verifyStoredState("exitBasicReleaseModule", true, State.POST_STOP_OR_FAILED_STARTUP);
	}
	

	@Override public String toString() { 
		return "[class=" + getClass().getSimpleName() + ", state=" + myState + ", stopRQ=" + myStopRequestedFlag + "]";
	}

	
}

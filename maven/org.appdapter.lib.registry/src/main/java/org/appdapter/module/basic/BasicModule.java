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

import org.appdapter.api.module.Module;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BasicModule<Ctx> extends KnownComponentImpl implements Module<Ctx> {

	private		Ctx				myContext;
	private		State			myState = State.PRE_INIT;

	private		boolean			myStopRequestedFlag = false;



	@Override public State getState() {
		return myState;
	}
	/**
	 * Correct way for application to ask the module to stop.
	 * @return
	 */
	@Override public void markStopRequested() {
		myStopRequestedFlag = true;
	}

	@Override public boolean isStopRequested() {
		return myStopRequestedFlag;
	}
	@Override public Ctx getContext() {
		return myContext;
	}

	@Override public synchronized void setContext(Ctx m) {
		myContext = m;
	}
	protected void notifyStateViolation(String detectingMethod, String expectedStateDesc, boolean throExcept) {
		String msg = "[" + detectingMethod + "] found illegal state [" + myState + " instead of expected [" + expectedStateDesc + "]";
		logInfo(IMPO_HI, msg);
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
				// TODO - let logInfoEvent format the string, only after importance check.
				logInfo(IMPO_LO, "[" + checkingMethod + "] verified storedState: " + myState);
				return;
			}
		}
		notifyStateViolation(checkingMethod, allowedStates.toString(), throExcept);
	}


	@Override public synchronized void failDuringInitOrStartup() {
		verifyStoredState("failDuringInitOrStartup", true, State.PRE_INIT, State.IN_INIT, State.WAIT_TO_START, State.IN_START);
		myState = State.FAILED_STARTUP;
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
		myState = State.POST_STOP;
	}

	protected synchronized void enterBasicReleaseModule() {
		verifyStoredState("enterBasicReleaseModule", true, State.POST_STOP, State.FAILED_STARTUP);
	}
	protected synchronized void exitBasicReleaseModule() {
		verifyStoredState("exitBasicReleaseModule", true, State.POST_STOP, State.FAILED_STARTUP);
	}

	@Override public String getFieldSummary() {
		return super.getFieldSummary() + ", state=" + myState + ", stopRQ=" + myStopRequestedFlag;
	}


}

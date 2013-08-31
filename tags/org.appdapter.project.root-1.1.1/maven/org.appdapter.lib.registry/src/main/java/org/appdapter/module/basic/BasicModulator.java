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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.api.module.Modulator;
import org.appdapter.api.module.Module;
import org.appdapter.api.module.Module.State.*;
import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicModulator<Ctx> extends BasicDebugger implements Modulator<Ctx> {
	// static Logger theLogger = LoggerFactory.getLogger(BasicModulator.class);
	private		List<Module<Ctx>>		myModuleList = new ArrayList<Module<Ctx>>();
	private		Ctx						myDefaultCtx;
	protected	boolean					myAutoDetachOnFinishFlag;
	
	public BasicModulator(Ctx defCtx, boolean autoDetachOnFinish) {
		myDefaultCtx = defCtx;
		myAutoDetachOnFinishFlag = autoDetachOnFinish;
	}
	
	protected static boolean isModuleInActionState(Module.State modState) {
		if (modState == null) {
			return false;
		}

		if (modState == Module.State.PRE_INIT || modState == Module.State.WAIT_TO_START || modState == Module.State.WAIT_TO_RUN_OR_STOP || modState == Module.State.POST_STOP
				|| modState == Module.State.FAILED_STARTUP)
			return false;
		if (modState == Module.State.IN_INIT || modState == Module.State.IN_START || modState == Module.State.IN_RUN || modState == Module.State.IN_STOP)
			return true;
		throw new RuntimeException("Module in unknown state: " + modState);

	}
	
	protected void setDefaultContext(Ctx ctx) {
		myDefaultCtx = ctx;
	}
	@Override public synchronized void attachModule(Module<Ctx> m) {
		Ctx prevCtx = m.getContext();
		if (prevCtx != null) {
			throw new RuntimeException("[" + this + "] cannot attach module [" + m 
							+ "] with existing context [" + prevCtx + "]");
		}
		Module.State modState = m.getState();
		if(isModuleInActionState(modState)) {
			throw new RuntimeException("Modulator[" + this + "] cannot attach module [" + m 
							+ "] in action state [" + modState + "]");
		}
		m.setContext(myDefaultCtx);
		myModuleList.add(m);
	}

	@Override public synchronized void detachModule(Module<Ctx> m) {
		/*
		Modulator prevMod = m.getContext();
		if (prevMod != this) {
			throw new RuntimeException("Modulator[" + this + "] cannot detach from module [" + m + 
							"] with different modulator [" + prevMod);
		}
		 * 
		 */
		if (!myModuleList.contains(m)) {
			throw new RuntimeException("[" + this + "] cannot detach from module [" + m + 
							"], it is not currently attached!");
		}
		Module.State modState = m.getState();
		if(isModuleInActionState(modState)) {
			throw new RuntimeException("Modulator[" + this + "] cannot detach module [" + m 
							+ "] in action state [" + modState + "]");
		}
		m.setContext(null);
		myModuleList.remove(m);
	}

	@Override public int getAttachedModuleCount() {
		return myModuleList.size();
	}
	/*
	 * 
	 */
	@Override public synchronized void processOneBatch() {
		// process lifecycle in reverse chronological order, to ensure that a particular module can advance
		// no more than one state per batch.
		
		//TODO - verify that this thread is not already inside a callback of one of our own modules
		
		dumpModules();
		processFinishedModules();
		processStoppingModules();
		processRunningModules();
		processStartingModules();
		processInitingModules();
	}
	protected synchronized List<Module<Ctx>> getModulesMatchingStates(Module.State... matchStates) { 
		List<Module<Ctx>> matches = new ArrayList<Module<Ctx>>();
		for (Module cand : myModuleList) {
			Module.State candState = cand.getState();
			for (Module.State ms : matchStates) { 
				if (candState == ms) {
					matches.add(cand);
					break;
				}
			}
		}
		return matches;
	}
	protected synchronized List<Module<Ctx>> getModulesNotMatchingStates(Module.State... excludedStates) { 
		List<Module<Ctx>> matches = new ArrayList<Module<Ctx>>();
		for (Module cand : myModuleList) {
			Module.State candState = cand.getState();
			boolean matchedExcluded = false;
			for (Module.State ms : excludedStates) { 
				if (candState == ms) {
					matchedExcluded = true;
					break;
				}
			}
			if (!matchedExcluded) {
				matches.add(cand);
			}
		}
		return matches;
	}
	protected List<Module<Ctx>> getUnfinishedModules() { 
		return getModulesNotMatchingStates(Module.State.POST_STOP, Module.State.FAILED_STARTUP);
	}
	protected List<Module<Ctx>> getFinishedModules() { 
		return getModulesMatchingStates(Module.State.POST_STOP, Module.State.FAILED_STARTUP);
	}	
	protected void processFinishedModules() { 
		List<Module<Ctx>> finishedModules = getModulesMatchingStates(Module.State.POST_STOP, Module.State.FAILED_STARTUP);
		for (Module fm : finishedModules) {
			try {
				fm.releaseModule();
			} catch (Throwable t) {
				// TODO: make sure we can get module description without further exceptions.
				logError("Exception while releasing module [" + fm + "]", t);
			} finally {
				if (myAutoDetachOnFinishFlag) {
					detachModule(fm);
				}
			}
		}
	}

	protected void processStoppingModules() { 
		List<Module<Ctx>> modulesEligibleToStop = getModulesMatchingStates(Module.State.WAIT_TO_RUN_OR_STOP);
		for (Module mes : modulesEligibleToStop) {
			try {
				if (mes.isStopRequested()) {
					mes.stop();
				}
			} catch (Throwable t) {
				// TODO: make sure we can get module description without further exceptions.
				logError("Exception while stopping module [" + mes + "]", t);
				// stop should not throw.  If it does throw, and state was not updated to
				// POST_STOP_OR_FAILED_STARTUP, then it will be allowed to continue trying
				// to run, and stop.
			} 
		}
	}
	protected void processRunningModules() { 
		List<Module<Ctx>> modulesEligibleToRun = getModulesMatchingStates(Module.State.WAIT_TO_RUN_OR_STOP);
		for (Module<Ctx> mer : modulesEligibleToRun) {
			try {
				mer.runOnce();
			} catch (Throwable t) {
				// runOnce should not throw.  But when it does, there is no administrative effect - the
				// module continues in its state machines.	
				logError("Exception while running module [" + mer + "]", t);
			}
		}
	}
	protected void processStartingModules() { 
		List<Module<Ctx>> modulesEligibleToStart = getModulesMatchingStates(Module.State.WAIT_TO_START);
		for (Module mes : modulesEligibleToStart) {
			try {
				mes.start();
			} catch (Throwable t) {
				logWarning("Exception while starting module [" + mes + "], marking module failed state", t);
				logWarning("Marking module failed state");
				mes.failDuringInitOrStartup();
			}
		}
	}
	protected void processInitingModules() { 
		List<Module<Ctx>> modulesEligibleToInit = getModulesMatchingStates(Module.State.PRE_INIT, null);
		for (Module mei : modulesEligibleToInit) {
			try {
				mei.initModule();
			} catch (Throwable t) {
				logWarning("Exception while initing module [" + mei + "], marking module failed state", t);
				mei.failDuringInitOrStartup();
			}
		}
	}
	
	public void dumpModules() { 
		if (checkDebugImportance(IMPO_LO)) {
			logInfo(IMPO_LO, "Module Dump: [" +  myModuleList + "]");
		}
	}
	
}

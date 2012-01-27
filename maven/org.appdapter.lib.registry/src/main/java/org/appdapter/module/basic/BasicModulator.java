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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicModulator implements Modulator {
	static Logger theLogger = LoggerFactory.getLogger(BasicModulator.class);
	private List<Module>	myModuleList = new ArrayList<Module>();

	protected static boolean isModuleInActionState(Module.State modState) { 
		if (modState == null) {
			return false;
		}
		switch (modState) {
			case PRE_INIT:
			case WAIT_TO_START:
			case WAIT_TO_RUN_OR_STOP:
			case POST_STOP_OR_FAILED_STARTUP:
				return false;
			
			case IN_INIT:
			case IN_START:
			case IN_RUN:
			case IN_STOP:
				return true;
			
			default:
				throw new RuntimeException("Module in unknown state: " + modState);
		}
		
	}
	@Override public synchronized void attachModule(Module m) {
		Modulator prevMod = m.getModulator();
		if (prevMod != null) {
			throw new RuntimeException("Modulator[" + this + "] cannot attach module [" + m 
							+ "] with existing modulator [" + prevMod + "]");
		}
		Module.State modState = m.getState();
		if(isModuleInActionState(modState)) {
			throw new RuntimeException("Modulator[" + this + "] cannot attach module [" + m 
							+ "] in action state [" + modState + "]");
		}
		m.setModulator(this);
		myModuleList.add(m);
	}

	@Override public synchronized void detachModule(Module m) {
		Modulator prevMod = m.getModulator();
		if (prevMod != this) {
			throw new RuntimeException("Modulator[" + this + "] cannot detach from module [" + m + 
							"] with different modulator [" + prevMod);
		}
		Module.State modState = m.getState();
		if(isModuleInActionState(modState)) {
			throw new RuntimeException("Modulator[" + this + "] cannot attach module [" + m 
							+ "] in action state [" + modState + "]");
		}
		m.setModulator(null);
		myModuleList.remove(m);
	}

	@Override public int getAttachedModuleCount() {
		return myModuleList.size();
	}

	@Override public synchronized void processOneBatch() {
		// process lifecycle in reverse chronological order, to ensure that a particular module can advance
		// no more than one state per batch.
		dumpModules();
		processFinishedModules();
		processStoppingModules();
		processRunningModules();
		processStartingModules();
		processInitingModules();
	}
	protected List<Module> getModulesMatchingStates(Module.State... matchStates) { 
		List<Module> matches = new ArrayList<Module>();
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
	protected void processFinishedModules() { 
		List<Module> finishedModules = getModulesMatchingStates(Module.State.POST_STOP_OR_FAILED_STARTUP);
		for (Module fm : finishedModules) {
			try {
				fm.releaseModule();
			} catch (Throwable t) {
				// TODO: make sure we can get module description without further exceptions.
				theLogger.error("Exception while releasing module [" + fm + "]", t);
			} finally {
				myModuleList.remove(fm);
			}
		}
	}

	protected void processStoppingModules() { 
		List<Module> modulesEligibleToStop = getModulesMatchingStates(Module.State.WAIT_TO_RUN_OR_STOP);
		for (Module mes : modulesEligibleToStop) {
			try {
				if (mes.isStopRequested()) {
					mes.stop();
				}
			} catch (Throwable t) {
				// TODO: make sure we can get module description without further exceptions.
				theLogger.error("Exception while stopping module [" + mes + "]", t);
				// stop should not throw.  If it does throw, and state was not updated to
				// POST_STOP_OR_FAILED_STARTUP, then it will be allowed to continue trying
				// to run, and stop.
			} 
		}
	}
	protected void processRunningModules() { 
		List<Module> modulesEligibleToRun = getModulesMatchingStates(Module.State.WAIT_TO_RUN_OR_STOP);
		for (Module mer : modulesEligibleToRun) {
			try {
				mer.runOnce();
			} catch (Throwable t) {
				// runOnce should not throw.  When it does, there is no administrative effect.
				theLogger.error("Exception while running module [" + mer + "]", t);
			}
		}
	}
	protected void processStartingModules() { 
		List<Module> modulesEligibleToStart = getModulesMatchingStates(Module.State.WAIT_TO_START);
		for (Module mes : modulesEligibleToStart) {
			try {
				mes.start();
			} catch (Throwable t) {
				theLogger.warn("Exception while starting module [" + mes + "], marking module failed state", t);
				theLogger.warn("Marking module failed state");
				mes.failDuringInitOrStartup();
			}
		}
	}
	protected void processInitingModules() { 
		List<Module> modulesEligibleToInit = getModulesMatchingStates(Module.State.PRE_INIT, null);
		for (Module mei : modulesEligibleToInit) {
			try {
				mei.initModule();
			} catch (Throwable t) {
				theLogger.warn("Exception while initing module [" + mei + "], marking module failed state", t);
				mei.failDuringInitOrStartup();
			}
		}
	}
	
	public void dumpModules() { 
		theLogger.info("Modules: " + myModuleList);
	}
	
}

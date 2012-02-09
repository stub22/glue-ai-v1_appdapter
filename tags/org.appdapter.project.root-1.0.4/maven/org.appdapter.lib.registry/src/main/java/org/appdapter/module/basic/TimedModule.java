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
import org.appdapter.module.basic.BasicModule;

/**
 * Here we log the timing of each callback exec, and also make the parent modulator's 
 * presence explicit in each callback.  We also supply a naive "completed run count".
 * @author Stu B. <www.texpedient.com>
 */
public abstract class TimedModule<Mlator extends Modulator> extends BasicModule<Mlator> {

	private		long	myCompletedRunCount = 0;
	
	protected	int		myRunDebugModulus = 5;
	
	public long getCompletedRunCount() { 
		return myCompletedRunCount;
	}
	protected abstract void doInit(Mlator mlator);
	
	@Override public synchronized void initModule() {
		enterBasicInitModule(true);
		Mlator mlator = getParentModulator();
		Long beginStamp = logInfoEvent(IMPO_NORM, true, null, ".doInit()-BEGIN");
		doInit(mlator);
		logInfoEvent(IMPO_NORM, true, beginStamp, ".doInit()-END");
		exitBasicInitModule(true);
	}
	
	protected abstract void doStart(Mlator mlator);
	
	@Override public synchronized void start() {
		enterBasicStart();
		Mlator mlator = getParentModulator();
		Long beginStamp = logInfoEvent(IMPO_NORM, true, null, ".doStart()-BEGIN");
		doStart(mlator);
		logInfoEvent(IMPO_NORM, true, beginStamp, ".doStart()-END");
		exitBasicStart();
	}
	
	protected abstract void doRunOnce(Mlator mlator, long runSeqNum);

	@Override public synchronized void runOnce() {
		enterBasicRunOnce();
		Mlator mlator = getParentModulator();
		// Our importance is usually "LO", but once every myRunDebugModulus runs, the importance goes up to "NORM".
		int msgImportance = ((myCompletedRunCount % myRunDebugModulus) == 0) ? IMPO_NORM : IMPO_LO;
		Long startStamp = logInfoEvent(msgImportance, true, null, ".doRunOnce(seqNum=%d)-BEGIN", myCompletedRunCount);
		doRunOnce(mlator, myCompletedRunCount);
		logInfoEvent(msgImportance, true, startStamp, ".doRunOnce(seqNum=%d)-END", myCompletedRunCount);
		++myCompletedRunCount;
		exitBasicRunOnce();
	}

	protected abstract void doStop(Mlator mlator);
	
	@Override public synchronized void stop() {
		enterBasicStop();
		Mlator mlator = getParentModulator();
		Long beginStamp = logInfoEvent(IMPO_NORM, true, null, ".doStop()-BEGIN");
		doStop(mlator);
		logInfoEvent(IMPO_NORM, true, beginStamp, ".doStop()-END");
		exitBasicStop();
	}

	protected abstract void doRelease(Mlator mlator);
	
	@Override public synchronized void releaseModule() {
		enterBasicReleaseModule();
		Mlator mlator = getParentModulator();
		Long beginStamp = logInfoEvent(IMPO_NORM, true, null, ".doRelease()-BEGIN");
		doRelease(mlator);
		logInfoEvent(IMPO_NORM, true, beginStamp, ".doRelease()-END");
		exitBasicReleaseModule();
	}
	
	@Override public String getDescription() { 
		String basicDesc = super.getDescription();
		return basicDesc + "[compRunCnt=" + getCompletedRunCount() + "]";
	}


	
}

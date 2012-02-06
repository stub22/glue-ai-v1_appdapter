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

/**
 * @author Stu B. <www.texpedient.com>
 */
public class NullModule<PM extends Modulator>  extends BasicModule<PM> {
	@Override public synchronized void initModule() {
		enterBasicInitModule(true);
		logInfo(IMPO_NORM, "initModule");
		exitBasicInitModule(true);
	}
	
	@Override public synchronized void start() {
		enterBasicStart();
		logInfo(IMPO_NORM, "start");
		exitBasicStart();
	}

	@Override public synchronized void runOnce() {
		enterBasicRunOnce();
		logInfo(IMPO_NORM, "runOnce");
		exitBasicRunOnce();
	}

	@Override public synchronized void stop() {
		enterBasicStop();
		logInfo(IMPO_NORM, "stop");
		exitBasicStop();
	}

	@Override public synchronized void releaseModule() {
		enterBasicReleaseModule();
		logInfo(IMPO_NORM, "releaseModule");
		exitBasicReleaseModule();
	}


	
}

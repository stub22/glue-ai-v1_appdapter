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
package org.appdapter.registry.test;

import org.appdapter.api.module.Modulator;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.module.basic.BasicModulator;
import org.appdapter.module.basic.BasicModule;
import org.appdapter.module.basic.EmptyTimedModule;
import org.appdapter.module.basic.NullModule;
import org.appdapter.module.basic.TimedModule;
import org.appdapter.osgi.registry.RegistryServiceFuncs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicModuleTestOne extends BasicDebugger {

	static class Ctx {
		public String myData = "Yes, please!";
	}
//	static Logger theLogger = LoggerFactory.getLogger(BasicModuleTestOne.class);

	public static class PowerModule extends NullModule<Ctx> {

		@Override public synchronized void runOnce() {
			enterBasicRunOnce();
			Long runStart = logInfoEvent(IMPO_NORM, true, null, "runOnce", "BEGIN");
			Ctx c = getContext();
			logInfoEvent(IMPO_NORM, true, runStart, "runOnce", "");
			exitBasicRunOnce();
		}
	}

	public void processBatches(Modulator mu, int count) {
		for (int i = 0; i < count; i++) {
			mu.processOneBatch();
		}
	}

	public void syncTest() {
		final Modulator mu = new BasicModulator<Ctx>(new Ctx(), true);

		processBatches(mu, 5);
		PowerModule pm1 = new PowerModule();
		pm1.setDebugImportanceThreshold(BasicModule.IMPO_MIN);
		mu.attachModule(pm1);
		EmptyTimedModule etm = new EmptyTimedModule();
		mu.attachModule(etm);
		processBatches(mu, 10);
		pm1.markStopRequested();
		processBatches(mu, 5);
		try {
			logInfo ("Expecting exception as we try to detach a module that was already auto-detached");
			mu.detachModule(pm1);
		} catch (RuntimeException re) {
			logInfo("Caught expected exception: " + re);
		}
		processBatches(mu, 5);
	}

	public void asyncTest() throws Throwable {

		final Modulator mu = new BasicModulator(new Ctx(), false);
		Thread runner = new Thread(new Runnable() {

			@Override public void run() {
				while (true) {
					try {
						mu.processOneBatch();
					} catch (Throwable t) {
						logError("Caught", t);
					}
				}
			}
		});

		runner.start();
		PowerModule pm1 = new PowerModule();
		pm1.setDebugImportanceThreshold(BasicModule.IMPO_MIN);
		mu.attachModule(pm1);
		Thread.sleep(200);
		pm1.markStopRequested();
		Thread.sleep(200);
		mu.detachModule(pm1);
		Thread.sleep(200);
	}
	public void runTest() { 
		logInfo("------------BasicModuleTestOne-----------");

		VerySimpleRegistry vsr = RegistryServiceFuncs.getTheWellKnownRegistry(getClass());

		try {
			logInfo("========================================");
			logInfo("Starting syncTest()");
			logInfo("========================================");
			syncTest();
			logInfo("========================================");
			logInfo("Finished syncTest(), starting asyncTest()");
			logInfo("========================================");
			asyncTest();
			logInfo("========================================");
			logInfo("Finished asyncTest()");
			logInfo("========================================");
			

		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		BasicModuleTestOne bmto = new BasicModuleTestOne();
		bmto.runTest();
	}
}

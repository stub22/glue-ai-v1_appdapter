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
import org.appdapter.module.basic.BasicModulator;
import org.appdapter.module.basic.BasicModule;
import org.appdapter.module.basic.NullModule;
import org.appdapter.osgi.registry.RegistryServiceFuncs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicModuleTestOne {

	static Logger theLogger = LoggerFactory.getLogger(BasicModuleTestOne.class);

	public static class PowerModule extends NullModule {

		@Override public synchronized void runOnce() {
			enterBasicRunOnce();
			logDebug(IMPO_NORM, "PowerModule runOnce body");
			exitBasicRunOnce();
		}
	}

	public static void log(String msg) {
		System.out.println(msg);
	}

	public static void processBatches(Modulator mu, int count) {
		for (int i = 0; i < count; i++) {
			mu.processOneBatch();
		}
	}

	public static void syncTest() {
		final Modulator mu = new BasicModulator();

		processBatches(mu, 5);
		PowerModule pm1 = new PowerModule();
		pm1.setDebugImportanceThreshold(BasicModule.IMPO_MIN);
		mu.attachModule(pm1);
		processBatches(mu, 10);
		pm1.markStopRequested();
		processBatches(mu, 5);
		mu.detachModule(pm1);
		processBatches(mu, 5);
	}

	public static void asyncTest() throws Throwable {

		final Modulator mu = new BasicModulator();
		Thread runner = new Thread(new Runnable() {

			@Override public void run() {
				while (true) {
					try {
						mu.processOneBatch();
					} catch (Throwable t) {
						log("Caught " + t.toString());
					}
				}
			}
		});

		runner.start();
		PowerModule pm1 = new PowerModule();
		pm1.setDebugImportanceThreshold(BasicModule.IMPO_MIN);
		mu.attachModule(pm1);
		Thread.sleep(20);
		pm1.markStopRequested();
		Thread.sleep(20);
		mu.detachModule(pm1);
		Thread.sleep(20);
	}

	public static void main(String[] args) {
		theLogger.info("------------BasicModuleTestOne-----------");

		VerySimpleRegistry vsr = RegistryServiceFuncs.getTheWellKnownRegistry();

		try {
			syncTest();
			theLogger.info("========================================");
			asyncTest();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
}

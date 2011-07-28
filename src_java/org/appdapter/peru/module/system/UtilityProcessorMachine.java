/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package org.appdapter.peru.module.system;


import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.name.CoreAddress;

import org.appdapter.peru.core.machine.ProcessorMachine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public class UtilityProcessorMachine extends ProcessorMachine {
	
	private static Log 		theLog = LogFactory.getLog(UtilityProcessorMachine.class );	
	
	private static Address	IA_DELAY = new CoreAddress("peruser:builtin/DELAY");
	private static Address	PA_DELAY_MSEC = new CoreAddress("peruser:prop/delayMsec");

	public synchronized Object process(Address instructAddr, Object input) throws Throwable {
		Object resultO = input;
		if (instructAddr.equals(IA_DELAY)) {
			Config	cc = getCurrentConfig();
			String delayMsecString = cc.getSingleString(IA_DELAY, PA_DELAY_MSEC);
			int delayMsec = Integer.parseInt(delayMsecString);
			threadDelay(delayMsec);
		} else {
			throw new Exception ("UtilityProcessorMachine received bad instruction: " + instructAddr);
		}
		return resultO;
	}
	
	public void threadDelay(int delayMsec) throws Throwable {
		// Should this be sending a "delay" command to a CommandMachine, or invoking "delay()" on a MethodMachine() ?
		theLog.info("threadDelay sleeping for " + delayMsec + " milliseconds");
		Thread.sleep(delayMsec);
	}
	
}		

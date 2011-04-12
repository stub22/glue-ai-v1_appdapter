package net.peruser.module.system;

import net.peruser.core.environment.Environment;

import net.peruser.core.config.Config;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;

import net.peruser.core.machine.ProcessorMachine;
import net.peruser.core.process.Data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author      Stu Baurmann
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

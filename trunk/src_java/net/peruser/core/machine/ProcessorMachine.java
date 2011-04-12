package net.peruser.core.machine;

import net.peruser.core.environment.Environment;

import net.peruser.core.config.Config;

import net.peruser.core.name.Address;

import net.peruser.core.process.Processor;
import net.peruser.core.process.Data;

// import net.peruser.core.document.Doc;

/**
 * ProcessorMachine binds the sophisticated contract of Processor to the rudimentary machinery of "AbstractMachine". 
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class ProcessorMachine extends AbstractMachine implements Processor {
	
	private		Address		currentNominalRootAddress;
	
	protected Address getCurrentNominalRootAddress() {
		return currentNominalRootAddress;
	}

	public Data process(Address instructionRoot, Data input, Environment world) throws Throwable {
		Data resultD = null;
		if (instructionRoot == null) {
			raiseProcessingException("process", "instructionRoot=null",  " Check src attribute in the sitemap transformer.");
		}
		if (input == null) {
			raiseProcessingException("process", "input=null",  " NONE");
		}
		if (world == null) {
			raiseProcessingException("process", "world=null",  " NONE");
		}
		
		setCurrentEnvironment(world);
		resultD = (Data) process(instructionRoot, input);
		return resultD;
	}
	
	public void create(Environment world, Data optionalExtraData) throws Throwable {
		setCurrentEnvironment(world);
	}
	
	public void destroy(Environment world) throws Throwable {
		setCurrentEnvironment(world);
	}
	
	public void reconfigure(Config c, Environment world, Address nominalRootAddr) throws Throwable {
		currentNominalRootAddress = nominalRootAddr;
		setCurrentEnvironment(world);
		setCurrentConfig(c);
	}
	
	public Data getStatusData(Environment world) throws Throwable {
		Data resultD = null;
		setCurrentEnvironment(world);
		return resultD;
	}
	
	protected void raiseProcessingException(String function, String problem, String suggestion) throws Throwable {
		String className = this.getClass().getName();
		String message = className + "[" + currentNominalRootAddress + "]." + function + " encountered '" + problem + "'; suggestion: " + suggestion;
		throw new Exception(message);
	}
	
}		

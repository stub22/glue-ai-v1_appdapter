package net.peruser.core.machine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.peruser.core.command.Command;
import net.peruser.core.command.AbstractCommand;

import net.peruser.core.document.Doc;

import net.peruser.core.config.Config;
import net.peruser.core.config.MutableConfig;

import net.peruser.core.name.Address;

import net.peruser.core.environment.Environment;

// import static net.peruser.core.vocabulary.SubstrateAddressConstants.instructionAddress;
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.opConfigRefPropAddress;

// BAD to import bindings in core!

import net.peruser.binding.dom4j.Dom4jDoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Skeletal implementation of a processing queue.<br/>
 * CommandMachine is a Machine that processes Commands in sequence.<br/> 
 * Calling the machine-level "process" method results in a new command being 
 * instantiated, scheduled and then executed. 
 * <p>Past commands are stored in a history list.</p>
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class CommandMachine extends DocProcessorMachine {
	
	private static Log 		theLog = LogFactory.getLog(CommandMachine.class);	
		
	private		Command			myCurrentCommand;
	private		Queue			myFutureCommands;
	private		List			myPastCommands;
	
	public CommandMachine() {
		myFutureCommands = new LinkedList();
		myPastCommands = new LinkedList();
	}
	/**  
	 * Sometimes the configPath tells us a lot, and sometimes it tells us less.
	 */
	public void setup(String configPath, Environment env) throws Throwable {
		 super.setup(configPath, env);
		 // Now, how far do we go towards instantiating a config and a configInstanceAddress?
	 }
	 
	protected synchronized boolean scheduleCommand (Command c) {
		return myFutureCommands.offer(c);
	}
	protected synchronized Object executeCurrentCommand (Object input) throws Throwable {
		Object result = null;
		if (myCurrentCommand != null) {
			result = myCurrentCommand.work(input);
			myCurrentCommand.close();
			myPastCommands.add(myCurrentCommand);
		}
		return result;
	}
	/**
	  *  @return   true if able to advance pointer (a command was waiting), false if the command Queue is empty.
	  */
	protected synchronized boolean advanceCommandPointer() {
		myCurrentCommand = (Command) myFutureCommands.poll();
		return (myCurrentCommand != null);
	}

	/**
	 *
	 * Sometimes the input mutates our config (maybe changes the configInstanceAddress),
	 * and sometimes it doesn't.
	 * <p> This method remains naive and more of a unitTest than a real robust "process()" impl.</p>
	 */
	public synchronized Object runCommandIfMachineCurrentlyEmpty(Command c, Object input) throws Throwable {
		Object result = null;
		if (myFutureCommands.peek() == null) {
			scheduleCommand(c);
			advanceCommandPointer();	
			result = executeCurrentCommand(input);
		}
		return result;
	}
	
	/**
	 *  Creates a clone of the machine's config model, so that no permanent changes result from our
	 *  config-override process.  Then applies any configuration overrides contained in inputDoc, 
	 * then creates a Command based on that configuration.
	 */
	
	protected Command produceNextCommand (Address instructAddr, Doc inputDoc) throws Throwable {
		Command result = null;
		
		Environment env = getCurrentEnvironment();
		
		Config curConfig = getCurrentConfig();
		MutableConfig commandConf = curConfig.makeMutableCloneConfig(env);
		commandConf.applyOverrides(inputDoc);
		
		// Note the references to SubstrateAddressConstant fields in the arguments:
		
		// Address commandAddress = commandConf.getSingleAddress(instructAddr, opConfigRefPropAddress);
		// theLog.debug("Found command address: " + commandAddress);
		// No more indirection!
		Address commandAddress = instructAddr;
		result = AbstractCommand.instantiateAndConfigure(env, commandConf, commandAddress);

		return result;
	}

	/**
	 *  Currently works only if the machine input queue is empty when process() is called!
	 */	
	protected Doc processDoc(Address instructAddr, Doc inputDoc) throws Throwable{
		Command nextCommand = produceNextCommand(instructAddr, inputDoc);
		Doc outputDoc = (Doc) runCommandIfMachineCurrentlyEmpty(nextCommand, inputDoc);
		return outputDoc;
	}
	
	
}		

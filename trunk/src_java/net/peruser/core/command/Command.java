package net.peruser.core.command;

import net.peruser.core.config.Config;

import net.peruser.core.document.Doc;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;
 
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.*;


/** A command represents a one-time, coarse-grained, configured request to perform some action.
 *  <br/>Commands are usually invoked from {@link net.peruser.core.machine.CommandMachine}s, and
 *  which usually rely on {@link net.peruser.core.operation.Operation}s in their implementation.
 *  <br/>
 *  <br/>Commands have a strict usage pattern.  Commands may NOT persist across multiple calls to configure() or execute().
 *  <br/>Rather, configure(), execute(), and close() must each be called exactly once (and in that order!) in the lifecycle of
 *  the command.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Command {
	/**
	  * Set up a virgin command to enable processing of a single execute() call.
	  */
	public void configure (Environment env, Config config, Address configInstanceAddress) throws Throwable;
	
	/**
	  * Execute the one and only "work" operation that this command will ever perform.
	  * May have side effects, but only on objects reachable from env, config, configInstanceAddress, and input.
	  */
	public Object work(Object input) throws Throwable;
	
	/**
	  *  Release any resources held by this command.  (These may have been left open after work(),
	  *  when supporting a dynamic ResultDoc).  Relationship to the ResultDoc from execute() is caller's
	  *  responsibility - see documentation in Command-implementing classes for details.
	  */
	public void close() throws Throwable;
}

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


package com.appdapter.peru.core.command;

import com.appdapter.peru.core.config.Config;

import com.appdapter.peru.core.document.Doc;

import com.appdapter.peru.core.environment.Environment;

import com.appdapter.peru.core.name.Address;
 
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.*;


/** A command represents a one-time, coarse-grained, configured request to perform some action.
 *  <br/>Commands are usually invoked from {@link net.peruser.core.machine.CommandMachine}s, and
 *  which usually rely on {@link net.peruser.core.operation.Operation}s in their implementation.
 *  <br/>
 *  <br/>Commands have a strict usage pattern.  Commands may NOT persist across multiple calls to configure() or execute().
 *  <br/>Rather, configure(), execute(), and close() must each be called exactly once (and in that order!) in the lifecycle of
 *  the command.
 *
 * @author      Stu B. <www.texpedient.com>
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

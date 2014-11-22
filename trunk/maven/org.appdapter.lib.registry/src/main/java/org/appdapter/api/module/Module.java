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
package org.appdapter.api.module;

/**
 * A Module has 5 ActionMethods = [initModule(), start(), runOnce(), stop(), releaseModule()]
 * and 8 formal states.
 * 
 * The application work of the module should be performed in small chunks by its runOnce() method,
 * which is called repeatedly (by some framework) until the module is stopped.
 * 
 * A Module has a ParentModulator which it may use to perform any system interactions, as
 * permitted by the type interface of the PM.
 * 
 * Normally, the methods of a Module are only invoked by its ParentModulator.  The
 * Module is not allowed to directly depend on this fact, but the Module is allowed
 * to make calls on its ParentModulator, which in general is a thread-safer behavior 
 * if we know we the current thread already holds the lock on that Modulator.
 * 		 
 * @author Stu B. <www.texpedient.com>
 */
public interface Module<Ctx> {
	public enum State {
		// [initial]				=> PRE_INIT,
		
		/**
		 * This state applies if and only if no "action" methods of the Module interface have yet been called on this object.

		 * The module is not required to reliably return this state.
		 * But, convention would have us set this value in Module constructor.
		 * IN_INIT, POST_STOP_OR_FAILED_STARTUP
		 */
		
		PRE_INIT,
		
		/**
		 *  This state applies if and only if the initModule() method is currently executing.
		 *   Module is not required to reliably return this state.   It may return null or 
		 *   PRE_INIT instead.
		 * 
		 * 	 Allowed xitions out = WAIT_TO_START
		 */
		
		IN_INIT,
		
		/** First state the module must reliably return.
		 * 
		 *  Allowed xitions out = {IN_START or POST_STOP_OR_FAILED_STARTUP}
		 */
		WAIT_TO_START,			
		
		/**
		 * This state applies if and only if the start() method is currently executing.
		 * Allowed xitions out = {WAIT_TO_RUN_OR_STOP, POST_STOP, FAILED_STARTUP}
		 */
		IN_START,				
		/**
		 *  Allowed xitions out =  {IN_RUN or IN_STOP}
		 */
		WAIT_TO_RUN_OR_STOP,	
		/**
		 * This state applies if and only if the run() method is currently executing.
		 * Allowed xitions out =  {WAIT_TO_RUN_OR_STOP}
		 * run() is not allowed to "fail" directly, must signal own stop RQ instead.
		 */
		IN_RUN,					
		/**
		 * This state applies if and only if the stop() method is currently executing.
		 * Allowed xitions out = {POST_STOP}
		 */
		IN_STOP,				

		/**
		 * Module is eligible to have releaseState() called, which is the only eligible method.
		 * The Module is required to reliably return this state value until the releaseState()
		 * method begins.  No further Xitions allowed.
		 */
		POST_STOP, 
		/**
		 * Module is eligible to have releaseState() called, which is the only eligible method.
		 * The Module is required to reliably return this state value until the releaseState()
		 * method begins.  No further Xitions allowed.
		 */		
		FAILED_STARTUP		
	}
	
	/**
	 * @return Current state, subject to synchronization behavior of this module.
	 */
	public State getState();
	
	/* Can be called anytime that the module is not already in another method.
	 * So impl must be synchronized, and Module methods cannot cache the modulator 
	 * must always check it.
	 * All other calls to action methods:  [initModule, start, runOnce, stop, releaseModule]
	 * must be "made by" the Modulator.
	 * 
	 */
	public void setContext(Ctx pm);
	
	/**
	 * The Module must have a valid parentModulator whenever it is inside a module action callback.
	 * @return Current Modulator, subject to synchronization behavior of this module.
	 */
	
	public Ctx getContext();
	
	/**  Modulator must be set when initModule is called.  initModule is called exactly
	 *  once, by the modulator, and can be First method called on Module, and called only once. 
	 *		Can only be called by the modulatorModule 
	 * Upon return, must be in WAIT_TO_START, or FAILED_STARTUP.
	 */
	
	public void initModule();	

	/** Required pre-state is WAIT_TO_START.   
	 * During-state is IN_START 
	 * Upon return, state is WAIT_TO_RUN_OR_STOP, or FAILED_STARTUP.
	 */
	
	public void start();	

	/** Required pre-state is WAIT_TO_RUN_OR_STOP.   
	 * During-state is IN_RUN.  
	 * Upon return, state is WAIT_TO_RUN_OR_STOP. */
	
	public void runOnce();		
	
	/** Required pre-state is WAIT_TO_RUN_OR_STOP.   
	 * During-state is IN_STOP. 
	 * Upon return, state is POST_STOP */
	
	public void	stop();		

	/** Required pre-state is POST_STOP or FAILED_STARTUP.  Module is no longer usable as soon as call begins.   
	 * Should not throw.   
	 */
	
	public void releaseModule(); 
	/**
	 * Correct way for application to ask the module to stop.
	 * May be called at any time to indicate that the module should stop() as soon as possible.
	 * 
	 */
	public void markStopRequested();
	/*
	 *  Has stop ever been requested for this module?
	 */
	public boolean isStopRequested();
	
	public void failDuringInitOrStartup();
	
}

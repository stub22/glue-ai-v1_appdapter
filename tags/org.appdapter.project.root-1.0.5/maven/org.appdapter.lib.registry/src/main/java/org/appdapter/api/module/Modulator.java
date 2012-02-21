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
 * @author Stu B. <www.texpedient.com>
 */
public interface Modulator {
	

	/*
	 *  A module may be attached to only one Modulator, called its ParentModulator, and must 
	 *	be detached from old Modulator before attached to a new one.  Attach+Detach may occur 
	 * at any time that it is NOT in an action module.
	 * 
	 *  attachModule is responsible for setting the module's modulator to itself.
	 */
	
	public void attachModule(Module m);
	
	/*
	 *  A module may be attached or detached at any time that it is NOT in an action module.
	 *  detachModule is responsible for setting the module's modulator to null.
	 */
	
	public void detachModule(Module m);
	
	public int getAttachedModuleCount();
	
	/* All action method calls to Modules must take place during a call to processOneBatch.
	 * A Modulator must be able to do all its work simply by being asked to processOneBatch
	 * repeatedly.  
	 * 
	 * A child module of this Modulator may not call processOneBatch from any of its work
	 * methods, although in principle that child module can run its own modulators, and
	 * process batches on them.
	 */
	public void processOneBatch();
		
}

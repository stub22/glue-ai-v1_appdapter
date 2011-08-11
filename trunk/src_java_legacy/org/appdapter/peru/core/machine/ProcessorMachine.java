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

package org.appdapter.peru.core.machine;

import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.name.Address;

import org.appdapter.peru.core.process.Processor;
import org.appdapter.peru.core.process.Data;

// import net.peruser.core.document.Doc;

/**
 * ProcessorMachine binds the sophisticated contract of Processor to the rudimentary machinery of "AbstractMachine". 
 *
 * @author      Stu B. <www.texpedient.com>
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

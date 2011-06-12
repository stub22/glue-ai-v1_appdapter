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

package com.appdapter.peru.core.machine;

import com.appdapter.peru.core.environment.Environment;
import com.appdapter.peru.core.config.Config;

// import net.peruser.core.document.Doc;

/**
 * AbstractMachine provides rudimentary bookeeping for certain Machine assets.
 * TODO:  Make "Observable" for monitoring/administration.
 * TODO:  Add some nice logging.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractMachine implements Machine {
	private		Environment		myCurrentEnvironment;
	private		Config			myCurrentConfig;
	
	protected void	setCurrentEnvironment(Environment e) {
		myCurrentEnvironment = e;
	}
	protected void	setCurrentConfig(Config c) throws Throwable {
		myCurrentConfig = c;
	}
	public Environment getCurrentEnvironment() {
		return myCurrentEnvironment;
	}
	public Config getCurrentConfig() {
		return myCurrentConfig;
	}
	
	/**
	  *		Sets the environment only, does not do anything about config+instruction.
	  */
	public void setup(String configPath, Environment env) throws Throwable {
		myCurrentEnvironment = env;
	}

}		

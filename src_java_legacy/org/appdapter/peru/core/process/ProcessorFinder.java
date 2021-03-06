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

package	org.appdapter.peru.core.process;

import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.environment.Environment;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public interface ProcessorFinder {
	/* Maximum flexibility, minimum convenience */
	public Processor findProcessor(Environment world, Class processorClass, String cuteName, Address processorAddress, 
	  			Data optionalInputData) throws Throwable;

	/* Maximum convenience for quick tests, minimum flexibility. */  
	public Processor findProcessor(Environment world, String processorClassFQN, String cuteName, Address processorAddress,
				Data optionalInputData) throws Throwable;
  
}
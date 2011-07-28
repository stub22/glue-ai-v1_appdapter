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
import org.appdapter.peru.core.handle.Handle;
import org.appdapter.peru.core.handle.HandleDirectory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public abstract class AbstractProcessorFinder implements ProcessorFinder {

	private static Log 		theLog = LogFactory.getLog(AbstractProcessorFinder.class);
		
	/** */
	public Processor findProcessor(Environment world, String processorClassFQN, String cuteName, 
				Address addr, Data optionalExtraData) throws Throwable {
		
		Class processorClass = Class.forName(processorClassFQN);
		return findProcessor(world, processorClass, cuteName, addr, optionalExtraData);
	}
	/** *
	*/
	protected ProcessorHandle lookupExistingProcessorHandle(Environment env, Address addr) throws Throwable {
		ProcessorHandle result = null;
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		Handle eh = envPrimaryHD.getHandleForAddress(addr);
		result = (ProcessorHandle) eh;
		return result;
	}
	
	protected ProcessorHandle registerProcessorHandle(Environment env, Processor p,  String cuteName, Address addr) 
					throws Throwable {
		ProcessorHandle resultH = null; 
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		resultH = new ProcessorHandle(cuteName, addr, p);
		envPrimaryHD.attachHandle(resultH);
		return resultH;
	}
	protected Processor instantiateProcessor(Environment env, Class pClass, Data optionalExtraData) throws Throwable {
		Processor resultP = (Processor) pClass.newInstance();
		resultP.create(env, optionalExtraData);
		return resultP;
	}
}
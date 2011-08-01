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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Objects of this class are stateless internally, but they do read/write processorHandles in the world directory.
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public class CoreProcessorFinder extends AbstractProcessorFinder {
	private static Logger 		theLogger = LoggerFactory.getLogger(CoreProcessorFinder.class);
	/* Finds or instantiates a processor, but does not configure it or otherwise mess with it */
	public Processor findProcessor(Environment env, Class pClass, String cuteName, Address addr,
					Data optionalExtraData) throws Throwable {
	
		ProcessorHandle resultPH = null;
		Processor		resultP;
		resultPH = lookupExistingProcessorHandle(env, addr);
		String message = "HUH";

		if (resultPH != null) {
			resultP = resultPH.getProcessor();
			message = "found existing processor";
		} else {
			resultP = instantiateProcessor(env, pClass, optionalExtraData);
			resultPH = registerProcessorHandle(env, resultP, cuteName, addr);
			message = "instantiated new processor";
		}
		String stats = " at [" + resultPH + "]=[" + resultP + "]";
		theLogger.info(message + " " + stats);		
		return resultP;
	}

}
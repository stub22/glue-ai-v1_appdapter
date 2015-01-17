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

import org.appdapter.peru.core.document.Doc;
import org.appdapter.peru.core.document.DocFactory;

import org.appdapter.peru.core.name.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static net.peruser.core.vocabulary.SubstrateAddressConstants.instructionAddress;
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.opConfigRefPropAddress;

// BAD to import bindings in core!


/**
 * Skeletal implementation of a processing queue.<br/>
 * CommandMachine is a Machine that processes Commands in sequence.<br/> 
 * Calling the machine-level "process" method results in a new command being 
 * instantiated, scheduled and then executed. 
 * <p>Past commands are stored in a history list.</p>
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class DocProcessorMachine extends ProcessorMachine {
	
	private static Logger 		theLogger = LoggerFactory.getLogger(DocProcessorMachine.class);	

	public Object process(Address instructAddr, Object input) throws Throwable {
		theLogger.info(" process() input: " + input);
		Object output = null;
		
		Doc inputDoc = DocFactory.makeDocFromObject(input, true);
		
		Doc outputDoc = processDoc(instructAddr, (Doc) input);
		output = outputDoc;
		//	org.w3c.dom.Document outDocW3C = outputDoc.getW3CDOM();
		//	output = outDocW3C;
		theLogger.info(" process() output: " + output);
		return output;
	}
	
	protected abstract Doc processDoc(Address instructAddr, Doc inputDoc) throws Throwable;
	
	
}		

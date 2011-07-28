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

package org.appdapter.peru.test.binding.jena;

import org.appdapter.peru.core.environment.Environment;
import org.appdapter.peru.binding.console.ConsoleEnvironment;
import org.appdapter.peru.binding.jena.JenaConfiguredCommandMachine;
import org.appdapter.peru.binding.jena.JenaKernel;

import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.name.CoreAddress;

import org.appdapter.peru.core.document.Doc;

import org.appdapter.peru.binding.dom4j.Dom4jDoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import static org.appdapter.peru.test.data.TestDataConstants.ModelMachineUnitTestConstants.*;


/**  ModelMachineTest is broken in Peruser 2.1.1
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class ModelMachineTest {
	private static Log 		theLog = LogFactory.getLog(ModelMachineTest.class );
	
	public static void main(String[] args) {
		theLog.info("ModelMachineTest - gears are spinning up!");
		String commandPath =  MMT_defaultCommandPath;
		String paramURL = MMT_defaultParamURL;
		String kernelBootURL = null;
		try {
			
			if (args.length >= 1) {
				commandPath = args[0];
			}
			if (args.length >= 2) {
				paramURL = args[1];
			}
			if (args.length >= 3) {
				kernelBootURL = args[2];
			}			
				
			JenaConfiguredCommandMachine  modelMachine = new JenaConfiguredCommandMachine();
			Environment env = new ConsoleEnvironment();
			
			org.appdapter.peru.core.config.Config conf = null;
			
			if (kernelBootURL != null) {
				// Boot the kernel, AND assume that the commandPath is an assembler URI to be looked up in that kernel.
				String kernelCuteName = "modelMachine_test_kernel";
				String kernelPubName = "jena_kernel:mm_test_kernel_at_" + kernelBootURL;
				Address kernelPubAddress = new CoreAddress(kernelPubName);
				
				// Constructing the kernel automatically places it in the environment.
				JenaKernel jk = new JenaKernel(env, kernelBootURL, kernelCuteName, kernelPubAddress);
				// conf = modelMachine.buildConfigUsingJenaAssembler(commandPath, env);
				modelMachine.setupUsingJenaAssembler(commandPath, env);
			} else {
				// Got less than 3 cmdLine args.
				// So, assume that commandPath is a URL whose contents (a model) should be used to 
				// configure the machine (bypassing any kernel).
				// conf = modelMachine.buildConfigUsingDirectStream(commandPath, env);
				modelMachine.setupUsingDirectStream(commandPath, env);
			}
			// This method is protected in JCCM (signature defined at AbstractMachine)
			// modelMachine.setCurrentConfig(conf);
			
			Address		instructAddr = null;
			
			Doc inDoc = Dom4jDoc.readFromURL(paramURL);
			// Run the request twice
			Doc rdoc = (Doc) modelMachine.process(instructAddr, inDoc);

			rdoc.writePretty(System.out);
			Doc r2doc = (Doc) modelMachine.process(instructAddr, inDoc);
			r2doc.writePretty(System.out);			

		} catch (Throwable t) {
			theLog.error("ModelMachineTest caught: ", t);
		}

		theLog.info("ModelMachineTest - gears are spinning down!");
	}
}
		
	/*		
	public static Document createDom4jDocument (ProjectedNode pn, List fieldPropertyUriList, int childLevelMax) throws Throwable {
		Element pnElement = createDom4jElement(pn, fieldPropertyUriList, childLevelMax);
		Document doc = DocumentHelper.createDocument(pnElement);
		return doc;
	}
	public static void writeProjectedNodeXmlDump(OutputStream outStream, ProjectedNode pn, List fieldPropertyUriList, int childLevelMax) throws Throwable {
		Document pnDocument = createDom4jDocument(pn, fieldPropertyUriList, childLevelMax);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(outStream, format);
		writer.write(pnDocument);			
		}
	}
	*/
	/*
	public static class OperationParam {
		// Are we actually REPLACING any existing value, or merely augmenting it?
		private	boolean		myStrictOverrideFlag = false;	
		private	Address		myThingAddress, myFieldAddress;
		private List		myValues;
		
		public OperationParam (Address thingAddress, Address fieldAddress, boolean strictFlag) {
			myThingAddress = thingAddress;
			myFieldAddress = fieldAddress;
			myStrictOverrideFlag = strictFlag;
			myValues = new List();
		}
		public Address getThingAddress() {
			return myThingAddress;
		}
		public Address getFieldAddress() {
			return myFieldAddress;
		}
		public boolean matchesAddresses (Address thingAddress, Address fieldAddress) {
			return (myThingAddress.equals(thingAddress) && myFieldAddress.equals(fieldAddress))
		}
		public void addValue (Object v) {
			myValues.add(v);
		}
		public boolean isSingleValued() {
			return (myValues.size() == 1);
		}
		public Address getSingleAddressValue () {
			return (Address) myValues.get(0);
		}
		public String getSingleStringValue () {
			return (String) myValues.get(0);
		}
	}
	*/

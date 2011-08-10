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

import java.util.Iterator;


import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.rdf.model.Model;

import org.appdapter.peru.binding.jena.JenaKernel;
import org.appdapter.peru.binding.jena.SPARQL_Utils;

import org.appdapter.peru.core.environment.Environment;
import org.appdapter.peru.binding.console.ConsoleEnvironment;
import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.name.CoreAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**  We need to test the heck out of the FileManager, Assembler, and possibly OntDocumentManager,
 *   until all is completely understood..
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class AssemblerTest_peru {
	private static Logger 		theLogger = LoggerFactory.getLogger(AssemblerTest_peru.class);


	
	public static void main(String[] args) {
		System.out.println("AssemblerTest - inflating!");		
		String 	bootDataURL = null;
		
		Model targetModel = null;
		
        try {
			if (args.length >= 1) {
				bootDataURL = args[0]; 
			}	
            Model assemblerSpecModel = null ;
			
			Environment consoleTestEnv = new ConsoleEnvironment();		
			
			// Relying on boot information is always a fallback position
			
			String kernelCuteName = "assembler_test_kernel";
			String kernelPubName = "jena_kernel:assembler_test_kernel_at_" + bootDataURL;
				
			Address kernelAddress = new CoreAddress(kernelPubName);
			
			JenaKernel 	jenaKernel = new JenaKernel(consoleTestEnv, bootDataURL, kernelCuteName, kernelAddress);
			jenaKernel.dumpDebug();
			// String queryResultXML = runQueryOverDataset (jenaKernel, "my_cute_combo_dataset", AT_cameraComboDatasetURI, AT_cameraQueryLoc);
			
			String datasetCuteName = "my_cute_census_dataset";
			String namespaceURI = "http://www.peruser.net/2008/census#";
			String datasetURI = namespaceURI + "census_combo_dataset_001";
			String queryLoc = "file:app/census/sparql/census_query_001.sparql";
			String queryResultXML = runQueryOverDataset (jenaKernel, datasetCuteName, datasetURI, queryLoc);
						
			// Model keysModel = AssemblerUtils.getAssembledModel(jenaKernel, AT_cameraKeysModelURI);
			
			theLogger.debug("Query Result XML: " + queryResultXML);
			
			jenaKernel.dumpDebug();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("AssemblerTest - deflating!");
	}
	public static String runQueryOverDataset(JenaKernel jenaKernel, String datasetCuteName, String datasetAssemblyURI, String queryPath)
				throws Throwable {
		Dataset	originalDataset = jenaKernel.assembleAndAttachDataset(datasetCuteName, datasetAssemblyURI);
		DataSource comboDataset = SPARQL_Utils.makeIndependentDataSourceFromDataset(originalDataset);
		
		theLogger.debug("Combo Dataset: " + comboDataset);
		Iterator dnit = comboDataset.listNames();
		while (dnit.hasNext()) {
			String	modelName = (String) dnit.next();
			theLogger.debug("found model name: " + modelName);
		}
		theLogger.debug("Query URL: " + queryPath);
		
		String queryResultXML = SPARQL_Utils.runQueryOverDataset(queryPath, comboDataset);		
		
		return queryResultXML;
	}
}					


			/*
			Model testModel = AssemblerUtils.getAssembledModel(jenaKernel, assemblerResURI);
			theLog.debug("testModel=" + testModel);
			Model comboModel = AssemblerUtils.getAssembledModel(jenaKernel, assemblerResURI);
			*/
			// pickyComboAs
						
/*
			SAXReader reader = new SAXReader();
			Document deltaDoc = reader.read(AT_deltaDocPath);
			
			Map namespaceMap = new HashMap();
			namespaceMap.put("pmd", "http://www.peruser.net/model_description");
			XPath modelDescXPath = deltaDoc.createXPath("//pmd:model_delta_set/pmd:model_delta");
			modelDescXPath.setNamespaceURIs(namespaceMap);
			List results = modelDescXPath.selectNodes(deltaDoc);
			for (Iterator iter = results.iterator(); iter.hasNext(); ) {
				Element modelE = (Element) iter.next();
				String format = modelE.valueOf("@format");
				String targetName = modelE.valueOf("@target");
				System.out.println("Found model with target " + targetName + " and format " + format);
				if (format.equals("RDF/XML")) {
					Element modelRootElement = modelE.element("RDF");
					String modelXML = modelRootElement.asXML();
					System.out.println("Model text dump:\n===========================\n " + modelXML + "\n============================");
					StringReader mxsr = new StringReader(modelXML);
					String modelBaseURI = null;
					Model instructiveModel = ModelFactory.createDefaultModel();
					instructiveModel.read(mxsr, modelBaseURI, format);
					
					targetModel.add(instructiveModel);
					//mergeModelIntoDataSource(ds, inputName, instructiveModel);
				}
			}
			*/			
			/*
			String queryFileURL = "substrate/sparql/test/softy/fcq_01.sparql";
			String modelURL = "substrate/sparql/test/softy/facet_config_01.ttl";
			String modelBaseURI = "http://www.peruser.net/phonyBaseURI#";
			String modelFormat = "N3";
			
			String resultXML = SPARQL_Utils.executeQueryFromFiles (queryFileURL, modelURL, modelFormat, modelBaseURI);

			System.out.println(resultXML);
	*/



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

package com.appdapter.peru.module.selector;

import java.io.FileInputStream;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


import com.appdapter.peru.core.name.Address;

import com.appdapter.peru.core.name.CoreAddress;

import com.appdapter.peru.binding.jena.SPARQL_Utils;

import com.appdapter.peru.core.config.Config;

import com.appdapter.peru.core.document.Doc;

import com.appdapter.peru.binding.dom4j.Dom4jDoc;


import com.appdapter.peru.core.environment.Environment;

import com.appdapter.peru.core.machine.DocProcessorMachine;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**  This class implements a "simple" form of SPARQL query.  <br/>
 * <ol>
 *		<li>The query text  from a </li>
 * </ol>
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class SimpleQueryMachine extends DocProcessorMachine {
	private static Log 		theLog = LogFactory.getLog(SimpleQueryMachine.class);

	/** Provides a simplified pathway for configuration, suitable in unit testing situations.
	 *  <br/>Not used in cocoon-deployed situations.
	 * 
	 */
	public void setup(String configPath, Environment env) throws Throwable {
		 super.setup(configPath, env);

	}
	/**
	 * In a cocoon environment, the input doc will already have been 
	 */
		
	protected  Doc processDoc(Address instructAddr, Doc input) throws Throwable {
		Doc resDoc = null;

		Environment env = getCurrentEnvironment();		

		Config curConfig = getCurrentConfig();

/* Note that in a CocoonConsolidatedConfig constructed by PeruserCocoonKernel, the sitemap parameters are to be found at instructAddr
 */
 
 		theLog.info("***************SimpleQueryMachine.processDoc() got instructAddress: " + instructAddr);
		 
		Address qfa = new CoreAddress("peruser_uri_scheme:queryFile");
		Address dfia = new CoreAddress("peruser_uri_scheme:dataFile");
		Address dfoa = new CoreAddress("peruser_uri_scheme:dataFormat");
		Address dbua = new CoreAddress("peruser_uri_scheme:dataBaseURI");

		String conf_queryFile = curConfig.getSingleString(instructAddr, qfa);
		 
		String conf_dataFile = curConfig.getSingleString(instructAddr, dfia);
		 
		String conf_dataFormat  = curConfig.getSingleString(instructAddr, dfoa);
		 
		String conf_dataBaseURI  = curConfig.getSingleString(instructAddr, dbua);
		 
		theLog.info("***************SimpleQueryMachine.processDoc() found queryFile param: " + conf_queryFile);

		String modelURL = env.resolveFilePath(conf_dataFile);
		String queryFileURL = env.resolveFilePath(conf_queryFile);
		String modelBaseURI = conf_dataBaseURI;
		String modelFormat = conf_dataFormat;
		
		// First, read whatever RDF data is in the input
		Model inputModel = produceModelFromInstructions(input);
		// Now add data from the model referred to by our sitemap params
		FileInputStream	modelInputStream = new FileInputStream(modelURL);
		inputModel.read(modelInputStream, modelBaseURI, modelFormat);
		
		String resultXML = SPARQL_Utils.executeQueryFromFile (queryFileURL, inputModel);
		// System.out.println("resultXML=" + resultXML);
		Document resultDoc4J = DocumentHelper.parseText(resultXML);
		
		resDoc = new Dom4jDoc (resultDoc4J);

		return resDoc;
	}
	/**
	 *  Looks in the instruction doc for embedded RDF/XML models matching the XPath address //pmd:model_set/pmd:model, where
	 *  pmd = http://www.peruser.net/model_description.  All such contents are collected into a single jena "Model" and returned. 
	 */
			
	protected static Model produceModelFromInstructions(Doc instructionDoc) throws Throwable {
		Model instructiveModel = ModelFactory.createDefaultModel();
		Document	d4jDoc = ((Dom4jDoc) instructionDoc).getDom4jDoc();
		Map namespaceMap = new HashMap();
		namespaceMap.put("pmd", "http://www.peruser.net/model_description");
		
		XPath modelDescXPath = d4jDoc.createXPath("//pmd:model_set/pmd:model");
		modelDescXPath.setNamespaceURIs(namespaceMap);
		List results = modelDescXPath.selectNodes(d4jDoc);
		for (Iterator iter = results.iterator(); iter.hasNext(); ) {
			Element modelE = (Element) iter.next();
			String format = modelE.valueOf("@format");
			theLog.info("Found model of format: " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLog.debug("Model dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				instructiveModel.read(mxsr, modelBaseURI, format);
			}
		}
		return instructiveModel;
	}

		
}		

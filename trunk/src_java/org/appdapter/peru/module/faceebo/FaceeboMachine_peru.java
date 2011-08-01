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

package org.appdapter.peru.module.faceebo;



import java.io.FileInputStream;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


import org.appdapter.peru.core.name.Address;

import org.appdapter.peru.binding.jena.SPARQL_Utils;

import org.appdapter.peru.core.document.Doc;

import org.appdapter.peru.binding.dom4j.Dom4jDoc;


import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.core.machine.AbstractMachine;

import org.apache.avalon.framework.parameters.Parameters;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class FaceeboMachine_peru extends AbstractMachine {
	private static Logger 		theLogger = LoggerFactory.getLogger(FaceeboMachine_peru.class);
	// See http://excalibur.apache.org/apidocs/org/apache/avalon/framework/parameters/Parameters.html
	private		Parameters		myCrutchParameters;
	
	public void setCrutchParameters(Parameters p) {
		myCrutchParameters = p;
	}
	private String getCrutchParameterString(String name) throws Throwable{
		return myCrutchParameters.getParameter(name);
	}
	/**  
	 * 
	 */
	public void setup(String configPath, Environment env) throws Throwable {
		 super.setup(configPath, env);

	}
	/**
	 *
	 */

	public  Object process(Address instructAddr, Object input) throws Throwable {
		return processDoc(instructAddr, (Doc) input);
	}

		
	public  Doc processDoc(Address instructAddr, Doc input) throws Throwable {
		
		Environment env = getCurrentEnvironment();
		Doc resDoc = null;
		
		String queryFileURL = env.resolveFilePath(getCrutchParameterString("queryFile"));
		String modelURL = env.resolveFilePath(getCrutchParameterString("dataFile"));
		String modelFormat =  getCrutchParameterString("dataFormat");
		String modelBaseURI = getCrutchParameterString("dataBaseURI");
		
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
			theLogger.info("Found model of format: " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLogger.debug("Model dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				instructiveModel.read(mxsr, modelBaseURI, format);
			}
		}
		return instructiveModel;
	}
}		

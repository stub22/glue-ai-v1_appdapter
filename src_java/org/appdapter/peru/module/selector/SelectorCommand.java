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

package org.appdapter.peru.module.selector;

import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.appdapter.peru.binding.jena.ModelUtils;
import org.appdapter.peru.binding.jena.ReasonerUtils;
import org.appdapter.peru.binding.jena.SPARQL_Utils;

import org.appdapter.peru.core.command.DocCommand;

import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.document.Doc;
import org.appdapter.peru.binding.dom4j.Dom4jDoc;

import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import static org.appdapter.peru.module.selector.SelectorAddressConstants.*;


/**  SelectorCommand implements SPARQL (and eventually other, similar) query mechanisms for peruser Machines.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class SelectorCommand extends DocCommand  {
	private static Log 		theLog = LogFactory.getLog(SelectorCommand.class);

	private	DataSource			myDataset;
	private	String			myQueryFilePath;

	public Doc workDoc(Doc instructionDoc) throws Throwable {		
		augmentDataSourceFromInstructions(myDataset, instructionDoc);
		
		SPARQL_Utils.dumpDatasetNames(myDataset);

		String resultXML = SPARQL_Utils.runQueryOverDataset (myQueryFilePath, myDataset);
		Document resultDoc4J = DocumentHelper.parseText(resultXML);
		// theLog.debug("workDoc() resDoc=" + resultDoc4J.asXML());
		Doc resDoc = new Dom4jDoc (resultDoc4J);
		return resDoc;
	}

	public void configure (Environment env, Config configImpl, Address configInstanceAddress)
				throws Throwable {
		theLog.debug("SelectorCommand.configure() - START");
		super.configure(env, configImpl, configInstanceAddress);
		Config opConf = configImpl;

		Address commandConfig = configInstanceAddress;
		
		Address queryPlace =  opConf.getSingleAddress(commandConfig, queryFilePlacePropAddress);
		String queryFilePath = getMappedPlaceURL(queryPlace);
		myQueryFilePath = queryFilePath;
		
		// if (commandConfig  isA DirectModelSelectorCommandConfig
		/* DatasetFactory      public static Dataset make(Dataset ds,
                           com.hp.hpl.jena.rdf.model.Model defaultModel)
public DataSet							   
		*/
		
		DataSource datasrc = DatasetFactory.create();
		
		List inputModelConfigs = opConf.getFieldValues(commandConfig, inputModelPropAddress);
		Iterator imcit = inputModelConfigs.iterator(); 
		while (imcit.hasNext()) {
			Address inputModelConfig = (Address) imcit.next();
			Address inferenceConfig = opConf.getOptionalAddress(inputModelConfig, inferenceConfigPropAddress);
			
			OntModelSpec ontModelSpec = ReasonerUtils.lookupOntModelSpec(opConf, inferenceConfig);
			
			Address modelPlace = opConf.getSingleAddress(inputModelConfig, modelPlacePropAddress);
			
			String modelFilePath = getMappedPlaceURL(modelPlace);
			
			Model baseModel = ModelUtils.loadJenaModelUsingJenaFileManager(myEnvironment, modelFilePath);
			Model queryModel = baseModel;
			if (ontModelSpec != null) {
				OntModel ontModel = ModelFactory.createOntologyModel(ontModelSpec, baseModel);
				queryModel = ontModel;
			}
			// This is the URI that must be matched to the GRAPH name in the SPARQL query
			// If it's absent, then we're dumping the model into the "default graph".
			String inputNameURI = null;
			Address inra =  opConf.getOptionalAddress(inputModelConfig, inputNameRefPropAddress);
			if (inra != null) {
				Address inputNameResolvedAddress = resolveRef(inra);
				inputNameURI = inputNameResolvedAddress.getResolvedPath();
			}

			SPARQL_Utils.mergeModelIntoDataSource(datasrc, inputNameURI, queryModel);
		}
		SPARQL_Utils.ensureDefaultModelNotNull(datasrc);

		 
		
		myDataset = datasrc;
		theLog.debug("SelectorCommand.configure() - END");
	}

	
	protected static void augmentDataSourceFromInstructions(DataSource ds, Doc instructionDoc) throws Throwable {
		Document	d4jDoc = ((Dom4jDoc) instructionDoc).getDom4jDoc();
		Map namespaceMap = new HashMap();
		namespaceMap.put("pmd", "http://www.peruser.net/model_description");
		XPath modelDescXPath = d4jDoc.createXPath("//pmd:model_set/pmd:model");
		modelDescXPath.setNamespaceURIs(namespaceMap);
		List results = modelDescXPath.selectNodes(d4jDoc);
		for (Iterator iter = results.iterator(); iter.hasNext(); ) {
			Element modelE = (Element) iter.next();
			String format = modelE.valueOf("@format");
			String inputName = modelE.valueOf("@name");
			theLog.debug("Found model with name " + inputName + " and format " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLog.debug("Model text dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				Model instructiveModel = ModelFactory.createDefaultModel();
				instructiveModel.read(mxsr, modelBaseURI, format);
				SPARQL_Utils.mergeModelIntoDataSource(ds, inputName, instructiveModel);
			}
		}
	}
}

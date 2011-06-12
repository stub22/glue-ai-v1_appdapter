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

package com.appdapter.peru.module.absorber;


import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.util.FileManager;





import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;







import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.dom4j.io.SAXReader;

/**  AbsorberTest_peru is our unit test for the peruser "Absorber" model updater.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class AbsorberTest_peru {
	public static void main(String[] args) {
		System.out.println("AbsorberTest - inflating!");		
			// String 	inputDataFile = args[0];
		String	assemblerFile = "app/picky/rdf/picky_sql_model_assembly.ttl";
		String  assemblerResURI = "http://www.peruser.net/picky#camera_features_001_db_model";
		String  deltaDocPath = "file:app/picky/test_xml/amazonCameraFeatureModel_bigger_sample.xml";
			
		Model targetModel = null;
		
        try {
            Model assemblerSpecModel = null ;
      
			assemblerSpecModel = FileManager.get().loadModel(assemblerFile) ;
  
			Resource dbModelAssemblerDesc = assemblerSpecModel.createResource(assemblerResURI);
			// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
			targetModel = (Model)Assembler.general.openModel(dbModelAssemblerDesc, Mode.ANY);			

			SAXReader reader = new SAXReader();
			Document deltaDoc = reader.read(deltaDocPath);

			// This query does not get the benefit of inference...
			// dbModelAssemblerDesc = GraphUtils.getResourceByType(spec, JA.RDBModel) ;
			
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
			/*
			String queryFileURL = "substrate/sparql/test/softy/fcq_01.sparql";
			String modelURL = "substrate/sparql/test/softy/facet_config_01.ttl";
			String modelBaseURI = "http://www.peruser.net/phonyBaseURI#";
			String modelFormat = "N3";
			
			String resultXML = SPARQL_Utils.executeQueryFromFiles (queryFileURL, modelURL, modelFormat, modelBaseURI);

			System.out.println(resultXML);
	*/
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("AbsorberTest - deflating!");
	}

}

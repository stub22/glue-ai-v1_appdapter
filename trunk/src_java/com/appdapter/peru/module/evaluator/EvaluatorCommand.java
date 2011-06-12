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

package com.appdapter.peru.module.evaluator;

import java.io.FileInputStream;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.w3c.IsaViz.fresnel.FSLHierarchyStore;
import org.w3c.IsaViz.fresnel.FSLJenaEvaluator;
import org.w3c.IsaViz.fresnel.FSLNSResolver;
import org.w3c.IsaViz.fresnel.FSLPath;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class EvaluatorCommand {

	
		
	/**
	 * @return			
	*/
	public static List query(Model jenaModel,  Map prefixMap, String fslQueryString) {
		FSLNSResolver nsr = new FSLNSResolver();

		Iterator keys = prefixMap.keySet().iterator();
		while (keys.hasNext()) {
			String 	abbrev = (String) keys.next();
			String  uri = (String) prefixMap.get(abbrev);
			nsr.addPrefixBinding(abbrev, uri);
		}
	
		FSLHierarchyStore fhs = new FSLHierarchyStore();
		FSLJenaEvaluator fje = new FSLJenaEvaluator(nsr, fhs);
		fje.setModel(jenaModel);

		FSLPath p = FSLPath.pathFactory(fslQueryString, nsr, FSLPath.NODE_STEP);
		/***** From the Fresnel docs:
				The result Vector contains as many Vectors as there are path instances in 
	 * 			the graph that match the FSL expression. Each one of these Vectors is 
	 * 			contains a sequence of alternating nodes and arcs which are respectively 
	 * 			instances of com.hp.hpl.jena.rdf.model.Resource or 
	 * 			com.hp.hpl.jena.rdf.model.Literal for the nodes, and 
	 * 			com.hp.hpl.jena.rdf.model.Statement for the arcs.	
	 
	 	****/
		
		Vector pathInstances = fje.evaluatePath(p);
		
		return pathInstances;
	}
	

	
	public static OntModel readModel(String mpath) throws Throwable {
		FileInputStream	modelInputStream = new FileInputStream(mpath);
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(modelInputStream, null);
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, baseModel);
		return ontModel;
	}
		/*
		FileReader	qfr = new FileReader(qpath);
		
		BufferedReader qbr = new BufferedReader(qfr);
		StringWriter qsw = new StringWriter();
		String line;
		while ((line = qbr.readLine()) != null) {
			qsw.write(line + LF);
		}
		qbr.close();
		String queryText = qsw.toString();
		
		System.out.println("=====================================================================================");
		System.out.println(queryText);
		System.out.println("=====================================================================================");
		
		ResultBindingImpl argBinding = new ResultBindingImpl();
		Resource fres = ontModel.getResource(flavorURI);
		argBinding.add("flavor", fres);
		
		Document doc = queryModel (ontModel, queryText, argBinding);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(doc);
	}*/


}

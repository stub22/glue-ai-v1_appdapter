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

package com.appdapter.peru.test.module.evaluator;


import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import com.hp.hpl.jena.ontology.OntModel;

import com.appdapter.peru.module.evaluator.EvaluatorCommand;


/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class EvaluatorTest {

	public static final String LF = System.getProperty("line.separator" );
	
	public static void main(String[] args) {
		System.out.println("EvaluatorTest - inflating!");
		String mpath = "app/toolchest/rdf/toolchestCommands.owl";
		String fslPath = "cmd:TestDataPlace/cmd:locationPath/text()";
		
		try {
		
			Map	nsMap = new HashMap();
			nsMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			nsMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			nsMap.put("cmd",  "http://www.peruser.net/2007/command#");	
		
			OntModel om = EvaluatorCommand.readModel(mpath);
			List results = EvaluatorCommand.query(om, nsMap, fslPath);
			
			System.out.println("Got " + results.size() + " results");
			Iterator pii = results.iterator(); 
			
			while (pii.hasNext()) {
				List resultTermList = (List) pii.next();
				System.out.println("Got " + resultTermList.size() + " terms");
				Iterator rti = resultTermList.iterator();
				while (rti.hasNext()) {
					Object term = rti.next();
					System.out.println (term.toString());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("EvaluatorTest - deflating!");
	}
	

}

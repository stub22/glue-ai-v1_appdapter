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

package org.appdapter.peru.binding.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * JenaModelBackedConfig extends JenaModelAwareConfig with the assumption
 * that configuration should be stored in a simple (non-inferenced,
 * non-ontologized) jena model, constructed when the Config is constructed.
 * <br/>
 * That's it, for now.
 * 
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class JenaModelBackedConfig extends JenaModelAwareConfig {
	
	public JenaModelBackedConfig (Model someModel) throws Throwable {
		Model ourModel  = ModelFactory.createDefaultModel();
		// We copy EVERYTHING from the defaults model, which is overkill.  Revisit later.
		ourModel.add(someModel, false);
		
		ourModel.setNsPrefixes(someModel);
		
		setActiveJenaModel(ourModel);
		
		// System.out.println("Dumping prefixes for opConfig model");
		// dumpPrefixes(ourModel, System.out);
	}
}


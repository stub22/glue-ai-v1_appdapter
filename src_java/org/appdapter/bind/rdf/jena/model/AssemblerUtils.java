/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.model;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.AssemblerHelp;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class AssemblerUtils {
	static Logger theLogger = LoggerFactory.getLogger(AssemblerUtils.class);


	public static Set<Object>	buildAllRootsInModel(Assembler jenaAssembler, Model jenaModel, Mode jenaAssemblyMode) {
		Set<Object> results = new HashSet<Object>();
		Set<Resource> aroots = AssemblerHelp.findAssemblerRoots(jenaModel);
		theLogger.info("Found " + aroots.size() + " assembler-roots in model");
		for (Resource aroot : aroots) {
			Object result = jenaAssembler.open(jenaAssembler, aroot, jenaAssemblyMode);
			results.add(result);
		}
		return results;
	}
	public static Set<Object> buildAllObjectsInRdfFile(String rdfURL) {
		Model	loadedModel =  FileManager.get().loadModel(rdfURL);
		Set<Object> results = buildAllRootsInModel(Assembler.general, loadedModel, Mode.DEFAULT);
		return results;
	}
	public static void registerClassLoader(ClassLoader cl) {
		FileManager.get().addLocatorClassLoader(cl); 
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.binding.jena.model;

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
 *
 * @author winston
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
}

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


import javax.servlet.http.HttpServlet;



// See note below under loadJenaModelUsingJenaFileManager()
// import com.hp.hpl.jena.sparql.util.RelURI;

import com.hp.hpl.jena.rdf.model.Model;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;


import org.appdapter.peru.core.environment.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static net.peruser.test.data.TestDataConstants.ModelUtilsUnitTestConstants.*;

/* Got these refs from Manifest.java */

/** 
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class ModelUtils extends JenaModelUtils
{
	private static Logger 		theLogger = LoggerFactory.getLogger(ModelUtils.class);
		
	public static String getServletContextPath (HttpServlet servlet, String relativePath) {
		return null;
	}

	static public Model loadJenaModelUsingJenaFileManager (Environment env, String relativePath) throws Throwable {
		theLogger.debug("******************   ModelUtils.loadJenaModelUsingJenaFileManager() - relativePath:  " + relativePath);
		/* This is similar to the code in jena's Manifest.java */
	    Model loadedModel = null;
		JenaKernel	jk = JenaKernel.getDefaultKernel (env);
		loadedModel = jk.getBestModelForLocation(relativePath);
		//  This (normally commented out) line will serialize the model, which is expensive if the model is not tiny.
		//  theLog.debug("******************   ModelUtils.loadJenaModelUsingJenaFileManager() - loadedModel:  " + loadedModel);		
		return loadedModel;
		/*
		RelURI disappeared between Jena 2.5.2 and 2.5.4.  Appears that FileManager.mapURI may be the closest replacement.
        String fullPath = RelURI.resolve(relativePath) ;
        logDebug("JFM resolved " + relativePath + " to " + fullPath) ;
		FileManager globalFileManager = FileManager.get();
        loadedModel = globalFileManager.loadModel(fullPath) ;
		return loadedModel;
		*/
	}

	
	/*
	        StmtIterator sIter = model.listStatements(null, RDF.type, type) ;
        if ( ! sIter.hasNext() )
            return null ;
        Resource r = sIter.nextStatement().getSubject() ;
	
	
	*/

	  /*
	public static OntDocumentManager TEST_getOntDocumentManager () {
		OntDocumentManager odm = OntDocumentManager.getInstance();

		odm.addAltEntry(dhuOntURI, dhuOntURL);
		odm.addAltEntry(dhdOntURI, dhdOntURL);
		odm.addAltEntry(subOntURI, subOntURL);
		
		return odm;
	}
	*/
	
/**
	Ignores a lot of important issues, such as what type the input model is, whether it contains reified statements, etc.
 */


}


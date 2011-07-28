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

// import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;



import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DataSource;

/** 
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class AssemblerUtils {
	private static Log 		theLog = LogFactory.getLog(AssemblerUtils.class );
	
	/**
	 *
	 */
	public static Model getAssembledModel (JenaKernel jk, String modelDescURI) throws Throwable {
		Resource desc = jk.findAssemblyResourceForFullURI(modelDescURI, false);
		// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
		Model result = (Model)Assembler.general.openModel(desc, Mode.ANY);
		return result;
	}

	public static Dataset getAssembledDataset (JenaKernel jk, String datasetDescURI) throws Throwable {
		
		/** 
	 	 * Note that schema for RDFDataset must be loaded to indicate that it is an assemblable "Object" 
		 * ja:RDFDataset  a rdfs:Class; rdfs:subClassOf ja:Object.
		 */
		Resource desc = jk.findAssemblyResourceForFullURI(datasetDescURI, false);
		// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
		DataSource ds = (DataSource) Assembler.general.open(desc); // , Mode.ANY);
		return ds;
	}	
	 
}


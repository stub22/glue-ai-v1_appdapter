/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.matdat

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel}

import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};
import com.hp.hpl.jena.query.{Dataset, DatasetFactory, DataSource};
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory};

import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

import com.hp.hpl.jena.rdf.listeners.{ObjectListener};

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.bind.rdf.jena.model.{ModelStuff, JenaModelUtils, JenaFileManagerUtils};
// import org.appdapter.bind.rdf.jena.query.{JenaArqQueryFuncs, JenaArqResultSetProcessor};

import org.appdapter.core.store.{Repo, BasicQueryProcessorImpl, BasicRepoImpl, QueryProcessor};

import org.appdapter.impl.store.{DirectRepo, QueryHelper, ResourceResolver};
import org.appdapter.help.repo.InitialBindingImpl
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */


abstract class SheetRepo(directoryModel : Model) extends DirectRepo(directoryModel) {

    /**  For All Subclasses    */
	def loadFileModelsIntoMainDataset(clList : java.util.List[ClassLoader]) = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val msqText = """
			select ?repo ?repoPath ?model ?modelPath
				{
					?repo  a ccrt:FileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:FileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
				}
		"""
		
		val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);		
		import scala.collection.JavaConversions._;
		while (msRset.hasNext()) {
			val qSoln : QuerySolution = msRset.next();
			
			val repoRes : Resource = qSoln.getResource("repo");
			val modelRes : Resource = qSoln.getResource("model");
			val repoPath_Lit : Literal = qSoln.getLiteral("repoPath")
			val modelPath_Lit : Literal = qSoln.getLiteral("modelPath")
			val dbgArray = Array[Object](repoRes, repoPath_Lit, modelRes, modelPath_Lit);
			getLogger.warn("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);
			
			val rPath  = repoPath_Lit.getString();
			val mPath = modelPath_Lit.getString();
			
			getLogger().warn("Ready to read from [{}] / [{}]", rPath, mPath);
			val rdfURL = rPath + mPath;
			
			import com.hp.hpl.jena.util.FileManager;
			val jenaFileMgr = JenaFileManagerUtils.getDefaultJenaFM
			JenaFileManagerUtils.ensureClassLoadersRegisteredWithJenaFM(jenaFileMgr, clList)
			try {
				val fileModel =  jenaFileMgr.loadModel(rdfURL);
					
				getLogger.warn("Read fileModel: {}" ,  fileModel)
				val graphURI = modelRes.getURI();
				mainDset.replaceNamedModel(graphURI, fileModel)
			} catch {
				case except => getLogger().error("Caught error loading file {}", rdfURL, except)
			}
		}		
	}
	
	def loadSheetModelsIntoMainDatasetByPath(clList : java.util.List[ClassLoader]) = {
	    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
	
	    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
	
	    val msqText = """
				select ?container ?key ?sheet ?name 
					{
						?container  a ccrt:CsvFilesRepo; ccrt:key ?key.
						?sheet a ccrt:CsvFilesSheet;
	      					ccrt:sheetPath ?name; ccrt:repo ?container.
					}
			"""
	
	    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
	    import scala.collection.JavaConversions._;
	    while (msRset.hasNext()) {
	      val qSoln: QuerySolution = msRset.next();
	
	      val containerRes: Resource = qSoln.getResource("container");
	      val sheetRes: Resource = qSoln.getResource("sheet");
	      val sheetNum_Lit: Literal = qSoln.getLiteral("name")
	      val sheetLocation_Lit: Literal = qSoln.getLiteral("key")
	      getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", name=" + sheetNum_Lit + ", key=" + sheetLocation_Lit)
	
	      val sheetNum = sheetNum_Lit.getString();
	      val sheetLocation = sheetLocation_Lit.getString();
	      var sheetModel: Model = null;
	      sheetModel = CsvFilesSheetRepo.readModelSheet(sheetLocation, sheetNum, nsJavaMap, clList);
	      getLogger.debug("Read sheetModel: {}", sheetModel)
	      val graphURI = sheetRes.getURI();
	      mainDset.replaceNamedModel(graphURI, sheetModel)
    }
  }

}

object SheetRepo extends BasicDebugger {
	
}
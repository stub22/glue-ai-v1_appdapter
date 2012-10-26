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


class SheetRepo(directoryModel : Model) extends DirectRepo(directoryModel) {

	def loadSheetModelsIntoMainDataset() = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val msqText = """
			select ?container ?key ?sheet ?num 
				{
					?container  a ccrt:GoogSheetRepo; ccrt:key ?key.
					?sheet a ccrt:GoogSheet; ccrt:sheetNumber ?num; ccrt:repo ?repo.
				}
		"""
		
		val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);		
		import scala.collection.JavaConversions._;
		while (msRset.hasNext()) {
			val qSoln : QuerySolution = msRset.next();
			
			val containerRes : Resource = qSoln.getResource("container");
			val sheetRes : Resource = qSoln.getResource("sheet");
			val sheetNum_Lit : Literal = qSoln.getLiteral("num")
			val sheetKey_Lit : Literal = qSoln.getLiteral("key")
			getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", num=" + sheetNum_Lit + ", key=" + sheetKey_Lit)
			
			val sheetNum = sheetNum_Lit.getInt();
			val sheetKey = sheetKey_Lit.getString();
			val sheetModel : Model = SemSheet.readModelGDocSheet(sheetKey, sheetNum, nsJavaMap);
			getLogger.debug("Read sheetModel: {}" ,  sheetModel)
			val graphURI = sheetRes.getURI();
			mainDset.replaceNamedModel(graphURI, sheetModel)
		}		
	}
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

}

object SheetRepo extends BasicDebugger {
	
	def readDirectoryModelFromGoog(sheetKey : String, namespaceSheetNum : Int, dirSheetNum : Int) : Model = { 
		getLogger.debug("readDirectoryModelFromGoog - start")
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, namespaceSheetNum, None);
		getLogger.debug("Made Namespace Sheet URL: " + namespaceSheetURL);
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		getLogger.debug("Got NS map: " + nsJavaMap)		
		val dirModel : Model = SemSheet.readModelGDocSheet(sheetKey, dirSheetNum, nsJavaMap);
		dirModel;
	}
	private def loadTestSheetRepo() : SheetRepo = {
		val nsSheetNum = 9;
		val dirSheetNum = 8;

		val dirModel : Model = readDirectoryModelFromGoog(SemSheet.keyForBootSheet22, nsSheetNum, dirSheetNum) 
		val sr = new SheetRepo(dirModel)
		sr.loadSheetModelsIntoMainDataset()
		val clList = new java.util.ArrayList[ClassLoader];
		sr.loadFileModelsIntoMainDataset(clList)
		sr
	}
	import scala.collection.immutable.StringOps
	
	def main(args: Array[String]) : Unit = {
		
		// Find a query with this info
		val querySheetQName = "ccrt:qry_sheet_22";
		val queryQName = "ccrt:find_lights_99"

		// Plug a parameter in with this info
		val lightsGraphVarName = "qGraph"
		val lightsGraphQName = "ccrt:lights_camera_sheet_22"	
		
		// Run the resulting fully bound query, and print the results.
		
		val sr : SheetRepo = loadTestSheetRepo()
		val qib = sr.makeInitialBinding
		
		qib.bindQName(lightsGraphVarName, lightsGraphQName)
		
		val solnJavaList : java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

		println("Found solutions: " + solnJavaList)
	}
}
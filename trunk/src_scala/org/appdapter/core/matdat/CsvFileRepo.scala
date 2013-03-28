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
import org.appdapter.help.repo.InitialBindingImpl;

import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory}
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory, QuerySolution};
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

import org.appdapter.impl.store.{DirectRepo, QueryHelper, ResourceResolver};

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */


class CsvFileRepo(directoryModel : Model) extends DirectRepo(directoryModel) {

	def loadSheetModelsIntoMainDataset() = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
				
		val msqText = """
			select ?repo ?repoPath ?model ?modelPath
				{
					?repo  a ccrt:CsvFileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:CsvFileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
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
			
			try {
				val sheetModel : Model = CsvFileRepo.readModelGDocSheet(rPath, rdfURL, nsJavaMap);
				getLogger.debug("Read sheetModel: {}" ,  sheetModel)
				val graphURI = rdfURL;//sheetRes.getURI();
				mainDset.replaceNamedModel(graphURI, sheetModel)
			} catch {
				case except => getLogger().error("Caught error loading file {}", rdfURL, except)
			}
		}	
	}
	def loadFileModelsIntoMainDataset(clList : java.util.List[ClassLoader]) = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val msqText = """
			select ?repo ?repoPath ?model ?modelPath
				{
					?repo  a ccrt:CsvFileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:CsvFileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
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

object CsvFileRepo extends BasicDebugger {
	
	def readDirectoryModelFromGoog(sheetKey : String, namespaceSheetURL : String, classLoaders : Object) : Model = { 
		getLogger.debug("readDirectoryModelFromGoog - start")
		getLogger.debug("Made Namespace Sheet URL: " + namespaceSheetURL);
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		getLogger.debug("Got NS map: " + nsJavaMap)		
		val dirModel : Model = readModelGDocSheet(sheetKey, namespaceSheetURL, nsJavaMap);
		dirModel;
	}
	private def loadTestCsvFileRepo() : CsvFileRepo = {

		val dirModel : Model = readDirectoryModelFromGoog(CsvFileRepo.keyForBootSheet22, null, null) 
		val sr = new CsvFileRepo(dirModel)
		sr.loadSheetModelsIntoMainDataset()
		val clList = new java.util.ArrayList[ClassLoader];
		sr.loadFileModelsIntoMainDataset(clList)
		sr
	}
	import scala.collection.immutable.StringOps
	
	def main2(args: Array[String]) : Unit = {
		
		// Find a query with this info
		val querySheetQName = "ccrt:qry_sheet_22";
		val queryQName = "ccrt:find_lights_99"

		// Plug a parameter in with this info
		val lightsGraphVarName = "qGraph"
		val lightsGraphQName = "ccrt:lights_camera_sheet_22"	
		
		// Run the resulting fully bound query, and print the results.
		
		val sr : CsvFileRepo = loadTestCsvFileRepo()
		val qib = sr.makeInitialBinding
		
		qib.bindQName(lightsGraphVarName, lightsGraphQName)
		
		val solnJavaList : java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

		println("Found solutions: " + solnJavaList)
	}
	
	val keyForBootSheet22 = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc";

	def readModelGDocSheet(sheetKey: String, sheetPath : String,  nsJavaMap : java.util.Map[String, String]) : Model = {
		val tgtModel : Model = ModelFactory.createDefaultModel();
		
		tgtModel.setNsPrefixes (nsJavaMap)
		
		val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
		
		processSheet (getReader(sheetPath), modelInsertProc.processRow);
		getLogger.debug("tgtModel=" + tgtModel)
		tgtModel;
	}
		
	def getReader(fileRef: String): Reader = {	  
	   null;
	}
	
	def processSheet(rawReader : Reader, processor : MatrixRow => Unit) { 
		
		val csvr : CSVReader = new CSVReader(rawReader);
		
		var done = false;
		while (!done) {
			val rowArray  : Array[String] = csvr.readNext();
			val matrixRow = new MatrixRowCSV(rowArray);
			if (rowArray != null) {
				processor(matrixRow);
			} else {
				done = true;
			}
		}
		csvr.close();
		rawReader.close();
	}
		
	def main(args: Array[String]) : Unit = {
	  	println("SemSheet test ");
		
		val namespaceSheetNum = 9;
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(keyForBootSheet22, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		// val namespaceMapProc = new MapSheetProc(1);
		// MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
		// namespaceMapProc.getJavaMap
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		
		println("Got NS map: " + nsJavaMap)
		
		val dirSheetNum = 8;
		val dirModel : Model = readModelGDocSheet(keyForBootSheet22, null, nsJavaMap);
		
		val queriesSheetNum = "myfile.csv";
		val queriesModel : Model = readModelGDocSheet(keyForBootSheet22, queriesSheetNum, nsJavaMap);		
		
		val tqText = "select ?sheet { ?sheet a ccrt:CsvFileRepo }";
		
		val trset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, tqText);
		val trxml = QueryHelper.buildQueryResultXML(trset);
		
		println("Got repo-query-test result-XML: \n" + trxml);
		
		val qqText = "select ?qres ?qtxt { ?qres a ccrt:SparqlQuery; ccrt:queryText ?qtxt}";

		val qqrset : ResultSet = QueryHelper.execModelQueryWithPrefixHelp(queriesModel, qqText);
		val qqrsrw = ResultSetFactory.makeRewindable(qqrset);
		// Does not disturb the original result set
		val qqrxml = QueryHelper.buildQueryResultXML(qqrsrw);

		import scala.collection.JavaConversions._;	
			
		
		println("Got query-query-test result-XML: \n" + qqrxml);
		qqrsrw.reset();
		val allVarNames : java.util.List[String] = qqrsrw.getResultVars();
		println ("Got all-vars java-list: " + allVarNames);
		while (qqrsrw.hasNext()) {
			val qSoln : QuerySolution = qqrsrw.next();
			for (val n : String <- allVarNames ) {
				val qvNode : RDFNode = qSoln.get(n);
				println ("qvar[" +  n + "]=" + qvNode);
			}
			
			val qtxtLit : Literal = qSoln.getLiteral("qtxt")
			val qtxtString = qtxtLit.getString();
			val zzRset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, qtxtString);
			val zzRSxml = QueryHelper.buildQueryResultXML(zzRset);
			println ("Query using qTxt got: " + zzRSxml)
			
	//		logInfo("Got qsoln" + qSoln + " with s=[" + qSoln.get("s") + "], p=[" + qSoln.get("p") + "], o=[" 
	//						+ qSoln.get("o") +"]");
		}		
		
		/**
		 *     		Set<Object> results = buildAllRootsInModel(Assembler.general, loadedModel, Mode.DEFAULT);
		 * 
		 */
	}	
}
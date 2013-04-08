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


import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.lang.StringBuffer;
import java.io.FileInputStream;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;
import org.apache.poi.hssf.usermodel.{HSSFWorkbook,HSSFRow,HSSFCell};


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

/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 * 
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single XLSX Spreadsheet than several .csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */


class XLSXSheetRepo(directoryModel : Model) extends SheetRepo(directoryModel) {

	def loadSheetModelsIntoMainDataset() = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val msqText = """
			select ?container ?key ?sheet ?num 
				{
					?container  a ccrt:XLSXRepo; ccrt:key ?key.
					?sheet a ccrt:XLSXSheet; ccrt:sheetNumber ?num; ccrt:repo ?container.
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
			val sheetModel : Model = XLSXSheetRepo.readModelSheet(sheetKey, sheetNum, nsJavaMap);
			getLogger.debug("Read sheetModel: {}" ,  sheetModel)
			val graphURI = sheetRes.getURI();
			mainDset.replaceNamedModel(graphURI, sheetModel)
		}		
	}
}

object XLSXSheetRepo extends BasicDebugger {
	
	def readModelSheet(sheetKey: String, sheetNum : Int,  nsJavaMap : java.util.Map[String, String]) : Model = {
		val tgtModel : Model = ModelFactory.createDefaultModel();
		
		tgtModel.setNsPrefixes (nsJavaMap)
		
		val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
		val is = new FileInputStream(sheetKey);
		val workbook = new HSSFWorkbook(is);
		var sheet = workbook.getSheetAt(sheetNum);
		var i = 0;
		val strBuff : StringBuffer = new StringBuffer();
		for (i <- 0  to sheet.getLastRowNum()) {
	
		  val row : HSSFRow = sheet.getRow(i);
	
		  var j = 0;
		    for (j <- 0 to row.getLastCellNum()) {
		        val cell : HSSFCell = row.getCell(j);
		        strBuff.append(cell.getStringCellValue() + ",");
		    }
		    strBuff.append('\n');
		}
		val reader : Reader = new StringReader(strBuff.toString());
		MatrixData.processSheetR (reader, modelInsertProc.processRow);
		getLogger.debug("tgtModel=" + tgtModel)
		tgtModel;
	}
	  	
	def readDirectoryModelFromXLSX(sheetKey : String, namespaceSheetNum : Int, dirSheetNum : Int) : Model = { 
		getLogger.debug("readDirectoryModelFromXLSX - start")
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, namespaceSheetNum, None);
		getLogger.debug("Made Namespace Sheet URL: " + namespaceSheetURL);
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		getLogger.debug("Got NS map: " + nsJavaMap)		
		val dirModel : Model = readModelSheet(sheetKey, dirSheetNum, nsJavaMap);
		dirModel;
	}
	private def loadTestXLSXSheetRepo() : XLSXSheetRepo = {
		val nsSheetNum = 9;
		val dirSheetNum = 8;

		val dirModel : Model = readDirectoryModelFromXLSX(SemSheet.keyForXLSXBootSheet22, nsSheetNum, dirSheetNum) 
		val sr = new XLSXSheetRepo(dirModel)
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
		
		val sr : XLSXSheetRepo = loadTestXLSXSheetRepo()
		val qib = sr.makeInitialBinding
		
		qib.bindQName(lightsGraphVarName, lightsGraphQName)
		
		val solnJavaList : java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

		println("Found solutions: " + solnJavaList)
	}
	
				
	val nsSheetNum22 = 9;
	val dirSheetNum22 = 8;
	
	def testSemSheet(args: Array[String]) : Unit = {
	  	println("SemSheet test ");
		val keyForXLSXBootSheet22 = SemSheet.keyForXLSXBootSheet22;
		val namespaceSheetNum = nsSheetNum22;
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(keyForXLSXBootSheet22, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		// val namespaceMapProc = new MapSheetProc(1);
		// MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
		// namespaceMapProc.getJavaMap
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		
		println("Got NS map: " + nsJavaMap)
		
		val dirSheetNum = 8;
		val dirModel : Model = readModelSheet(keyForXLSXBootSheet22, dirSheetNum, nsJavaMap);
		
		val queriesSheetNum = 12;
		val queriesModel : Model = readModelSheet(keyForXLSXBootSheet22, queriesSheetNum, nsJavaMap);		
		
		val tqText = "select ?sheet { ?sheet a ccrt:XLSXSheet }";
		
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
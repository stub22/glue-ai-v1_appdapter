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

import org.appdapter.bind.rdf.jena.model.{ModelStuff, JenaModelUtils};
import org.appdapter.bind.rdf.jena.query.{JenaArqQueryFuncs, JenaArqResultSetProcessor};

import org.appdapter.core.store.{Repo, BasicQueryProcessorImpl, BasicRepoImpl, QueryProcessor};

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */



class RepoPrintinListener(val prefix: String) extends ObjectListener {
	 override def added(x : Object) : Unit = {
		 println(prefix + " added: " + x);
	 }
	 override def removed(x : Object) : Unit = {
		 println(prefix + " removed: " + x);
	 }
	 
}

class SheetRepo(val myDirectoryModel : Model) extends BasicRepoImpl {
	override def  makeMainQueryDataset() : Dataset = {
		val ds : Dataset = DatasetFactory.create() // becomes   createMem() in later Jena versions.
		ds;
	}
	override def  getGraphStats() : java.util.List[Repo.GraphStat] = new java.util.ArrayList();
	
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
			println("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", num=" + sheetNum_Lit + ", key=" + sheetKey_Lit)
			
			val sheetNum = sheetNum_Lit.getInt();
			val sheetKey = sheetKey_Lit.getString();
			val sheetModel : Model = SemSheet.readModelGDocSheet(sheetKey, sheetNum, nsJavaMap);
			println("Read sheetModel: " + sheetModel)
			val graphURI = sheetRes.getURI();
			mainDset.replaceNamedModel(graphURI, sheetModel)
		}		
	}
	def findSingleQuerySolution(parsedQQ : Query, qInitBinding : QuerySolution) : Option[QuerySolution] = {
		val solnJavaList : java.util.List[QuerySolution] = findAllSolutions(parsedQQ, qInitBinding);
		if (solnJavaList.ne(null)) {
			if (solnJavaList.size() == 1) {
				return Some(solnJavaList.get(0))
			}
		} 
		None; 
	}
	def parseQueryText(queryText : String) : Query = {
		JenaArqQueryFuncs.parseQueryText(queryText, myDirectoryModel);
	}
	def bindQueryVarToQName(qSoln : QuerySolutionMap, vName : String, resQName : String) : Unit = {
		val expandedURI = myDirectoryModel.expandPrefix(resQName)
		val dirResource = myDirectoryModel.createResource(expandedURI)
		qSoln.add(vName, dirResource)
	}
	def getQueryText(querySourceGraphQName : String, queryParentQName : String) : String = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val qInitBinding = new QuerySolutionMap()
		bindQueryVarToQName(qInitBinding, "g", querySourceGraphQName)
		bindQueryVarToQName(qInitBinding, "qRes", queryParentQName)

		val msqText = """
			SELECT ?g ?qRes ?queryTxt WHERE {
				GRAPH ?g {
					?qRes  a ccrt:SparqlQuery ; ccrt:queryText ?queryTxt .			
				}
			}
		"""
		
		val parsedQQ = parseQueryText(msqText);
		
		println ("parsedQQ: " + parsedQQ)
		val possSoln : Option[QuerySolution] = findSingleQuerySolution(parsedQQ, qInitBinding);
		val qText : String = if (possSoln.isDefined) {
			val qSoln = possSoln.get;
			val qtxt_Lit : Literal = qSoln.getLiteral("queryTxt");
			qtxt_Lit.getString()
		} else "";

		qText
	}

}

object SheetRepo {
	def readDirectoryModelFromGoog(sheetKey : String, namespaceSheetNum : Int, dirSheetNum : Int) : Model = { 
		println("readDirectoryModelFromGoog - start")
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		println("Got NS map: " + nsJavaMap)		
		val dirModel : Model = SemSheet.readModelGDocSheet(sheetKey, dirSheetNum, nsJavaMap);
		dirModel;
	}
	def loadTestSheetRepo() : SheetRepo = {
		val nsSheetNum = 9;
		val dirSheetNum = 8;

		val dirModel : Model = readDirectoryModelFromGoog(SemSheet.keyForBootSheet22, nsSheetNum, dirSheetNum) 
		val sr = new SheetRepo(dirModel)
		sr.loadSheetModelsIntoMainDataset()
		sr
	}
	
	def main(args: Array[String]) : Unit = {
		val sr : SheetRepo = loadTestSheetRepo()
		val qText = sr.getQueryText("ccrt:qry_sheet_22", "ccrt:find_humanoids_99")
		println("Found query text: " + qText)
		
		val parsedQ = sr.parseQueryText(qText);
		val solnJavaList : java.util.List[QuerySolution] = sr.findAllSolutions(parsedQ, null);
		println("Found solutions: " + solnJavaList)
	}
}